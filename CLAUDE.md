# CLAUDE.md - chaekpool-api

## Project

책풀(chaekpool) API - 책을 고르고, 리뷰를 쓰고, 공유하는 기록형 SNS의 백엔드

**Tech Stack:**

- Spring Boot 4.0.2 / Kotlin 2.3.10 / Java 25
- Build: Gradle 9.3.1 (Kotlin DSL)
- Database: PostgreSQL 18.2 + jOOQ 3.19.29
- Cache: Valkey 9.0.2 (Redis)
- Infra: spring-boot-docker-compose (Docker Compose 자동 실행)
- Test: Kotest 6.1.0 + MockK 1.14.9 + Testcontainers 2.0.3

---

## Build & Run

### 빌드 및 실행

```bash
./gradlew build              # 전체 빌드
./gradlew compileKotlin      # 컴파일만
./gradlew bootRun            # 앱 실행 (port 8080)
./gradlew test               # 전체 테스트
./gradlew jooqGenerate       # jOOQ 코드 생성 (Flyway 마이그레이션 후 자동)
```

### 로컬 인프라 실행

```bash
# local 프로필로 앱 실행 시 Docker Compose 자동 실행 (spring-boot-docker-compose)
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun

# 수동 실행 (Docker Compose만)
docker compose -f docker-compose.local.yml up -d

# 종료
docker compose -f docker-compose.local.yml down

# 종료 + 데이터 초기화 (볼륨 삭제)
docker compose -f docker-compose.local.yml down -v
```

### 환경 변수

- `.env.local` 파일 직접 참고 (프로젝트 루트에 위치)
- `.env.local` 참고하여 환경 설정 (개발 환경이므로 민감정보 없음, 커밋 대상)
- **프로덕션 환경**: 반드시 환경변수로 주입, 파일 커밋 금지
- **주의**: .env.local 수정 후 민감정보 포함 여부 확인 필수

---

## Architecture

### 모듈 구조

```
io.chaekpool/
├── auth/                    # 인증/인가 (OAuth, JWT, Token)
│   ├── annotation/          # @AccessUserId, @RefreshUserId, @AccessToken, @RefreshToken
│   ├── dto/                 # AuthResponse
│   ├── swagger/             # Swagger UI 카카오 OAuth 인증 (local/dev 전용)
│   │                        # SwaggerAuthController (팝업 OAuth → postMessage → JWT)
│   ├── oauth2/              # Kakao OAuth 로그인
│   │   ├── client/          # KakaoAuthClient, KakaoApiClient (Feign)
│   │   ├── config/          # KakaoAuthProperties, OAuth2FeignConfig
│   │   ├── controller/      # KakaoController (인가 리다이렉트, 콜백, OAuth 토큰 갱신)
│   │   ├── dto/             # KakaoAuthTokenResponse, KakaoAuthRefreshTokenResponse, KakaoApiAccountResponse 등
│   │   ├── exception/       # ProviderNotFoundException
│   │   ├── repository/      # ProviderAccountRepository (jOOQ)
│   │   └── service/         # KakaoService
│   └── token/               # JWT 토큰 관리
│       ├── config/          # JwtProperties
│       ├── controller/      # TokenController (refresh, rotate, logout)
│       ├── dto/             # TokenPair, TokenResponse
│       ├── entity/          # RefreshToken, TokenBlacklist (Redis)
│       ├── exception/       # InvalidToken, TokenExpired, TokenBlacklisted 등
│       ├── filter/          # JwtAuthenticationFilter
│       ├── provider/        # JwtProvider, CookieProvider (service/에서 분리)
│       ├── repository/      # RefreshTokenRepository, TokenBlacklistRepository
│       └── service/         # TokenService, TokenManager, BlacklistManager
├── common/                  # 공통 기능
│   ├── config/              # WebSecurityConfig, CorsProperties, MetricsConfig, JacksonConfig, OpenApiConfig, ApiResponseOperationCustomizer
│   ├── controller/          # CommonController (robots.txt)
│   ├── dto/                 # ErrorResponse, UserMetadata
│   ├── exception/           # ServiceException 계층, ErrorCodeAccessDeniedException, ErrorCodeBadCredentialsException
│   │   ├── internal/        # BadRequest, NotFound, Forbidden, Conflict, Unauthorized, InternalServerError
│   │   └── external/        # ExternalServiceException
│   ├── filter/              # AccessLogFilter, UserMetadataFilter/Context
│   ├── handler/             # GlobalExceptionHandler, ErrorCodeAccessDeniedHandler, ErrorCodeAuthenticationEntryPoint
│   ├── logger/              # SingleLineFeignLogger
│   ├── provider/            # CryptoProvider
│   ├── serializer/          # EncryptedStringSerializer, EncryptedStringDeserializer (@Component + DI)
│   └── util/                # AssertionExtension, UserMetadataExtractor
└── user/                    # 사용자 관리
    ├── controller/          # UserController
    ├── dto/                 # UserResponse
    ├── exception/           # UserNotFoundException
    ├── repository/          # UserRepository (jOOQ)
    └── service/             # UserService
```

### 테스트 구조

```
src/test/kotlin/
├── io/kotest/provided/
│   └── ProjectConfig.kt        # Kotest Spring Extension 글로벌 설정
└── io/chaekpool/
    ├── support/                 # TestcontainersConfig (PostgreSQL, Valkey)
    ├── auth/
    │   ├── oauth2/
    │   │   ├── dto/             # KakaoAuthTokenResponseTest (TC 통합 테스트)
    │   │   └── service/         # KakaoServiceTest
    │   └── token/
    │       ├── filter/          # JwtAuthenticationFilterTest
    │       ├── provider/        # JwtProviderTest, CookieProviderTest
    │       └── service/         # TokenServiceTest, BlacklistManagerTest
    ├── common/
    │   ├── filter/              # AccessLogFilterTest, UserMetadataFilterTest, UserMetadataContextTest
    │   ├── handler/             # GlobalExceptionHandlerTest
    │   ├── logger/              # SingleLineFeignLoggerTest
    │   ├── provider/            # CryptoProviderTest
    │   └── util/                # AssertionExtensionTest, UserMetadataExtractorTest, MaskingUtilTest, UUIDv7Test
    ├── user/
    │   └── service/             # UserServiceTest
    └── ChaekpoolApplicationTests.kt  # Spring Context 로드 테스트
```

### 주요 기술 스택

- **API 경로**: `/api/v1/...`
- **ID generate**: UUID version 7 (시간 기반 + 랜덤) - postgreSQL `DEFAULT uuidv7()`, kotlin `UUIDv7.generate()`
- **Database**: PostgreSQL 18.2 + jOOQ (Type-safe SQL)
- **Migration**: Flyway 11.14 (`src/main/resources/db/migration`)
- **Cache/Session**: Valkey 9.0.2 (Redis 호환) - RefreshToken, TokenBlacklist 저장
- **Monitoring**: Prometheus + Loki + Jaeger (OpenTelemetry OTLP) + Grafana
- **Profile**: `local` (Docker Compose — `docker-compose.local.yml`, spring-boot-docker-compose 자동 실행), `dev` (서버 배포)
- **API 문서**: springdoc-openapi (`local`/`dev`만 활성화, `persist-authorization: true`로 토큰 유지), 커스텀 UI:
  `/swagger-oauth2-ui/kakao.html` (카카오 OAuth 팝업 로그인 지원)

---

## Code Conventions

### 0. 설계 원칙

**KISS (Keep It Simple, Stupid)**

- **단순함 우선**: 복잡한 아키텍처보다 명확하고 이해하기 쉬운 코드 작성
- **과도한 추상화 지양**: 현재 필요한 기능에 집중, 미래 확장을 위한 과도한 설계 금지
- **가독성 > 간결성**: 한 줄로 줄이는 것보다 의도가 명확한 여러 줄의 코드 선호
- **적절한 수준**: 3번 반복되면 추상화 고려, 그 전까지는 중복 허용

**SOLID 원칙**

- **S**ingle Responsibility: 하나의 클래스/메서드는 하나의 책임만 가짐
  ```kotlin
  // ❌ Bad: 여러 책임
  class UserService {
      fun createUser() { ... }
      fun sendEmail() { ... }  // 이메일은 별도 서비스로
  }

  // ✅ Good: 단일 책임
  class UserService { fun createUser() }
  class EmailService { fun sendEmail() }
  ```

- **O**pen/Closed: 확장에는 열려있고 수정에는 닫혀있음
  ```kotlin
  // 인터페이스로 확장 가능하게 설계
  interface PaymentMethod { fun pay(amount: Long) }
  class CreditCard : PaymentMethod { ... }
  class BankTransfer : PaymentMethod { ... }
  ```

- **L**iskov Substitution: 하위 타입은 상위 타입으로 대체 가능해야 함
- **I**nterface Segregation: 불필요한 메서드 의존 금지, 인터페이스 분리
- **D**ependency Inversion: 구체 클래스가 아닌 인터페이스/추상화에 의존

**원칙 적용 시 유의사항**:

- 무조건적 적용 금지 - 현재 필요와 복잡도 고려
- KISS와 SOLID 균형 유지 - 과도한 SOLID는 복잡도 증가
- 실용주의: 작은 프로젝트는 간단하게, 큰 프로젝트는 견고하게

---

### 1. 기본 원칙

**Logging**

```kotlin
private val log = KotlinLogging.logger {}  // ✅ 권장
// LoggerFactory, LoggerDelegate 사용 금지 ❌
```

**DI (Dependency Injection)**

```kotlin
@Service
class UserService(  // ✅ 생성자 주입
    private val userRepository: UserRepository
)

// 필드 주입 금지 ❌
// @Autowired private lateinit var userRepository: UserRepository
```

**DTO Naming**

```kotlin
data class UserResponse(
    @param:JsonProperty("user_id") val userId: Long,
    @param:JsonProperty("nickname") val nickname: String
)  // ✅ snake_case로 JSON 매핑
```

**Properties**

```kotlin
@ConfigurationProperties(prefix = "auth.jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenValiditySeconds: Long,
    val refreshTokenValiditySeconds: Long
)  // ✅ data class + prefix
```

---

### 2. 예외 처리

**예외 계층 구조**

```
ServiceException (추상)
├── internal/
│   ├── BadRequestException
│   ├── NotFoundException
│   │   ├── UserNotFoundException (user/exception/)
│   │   └── ProviderNotFoundException (auth/oauth2/exception/)
│   ├── ForbiddenException
│   ├── ConflictException
│   ├── UnauthorizedException
│   └── InternalServerErrorException
├── external/
│   └── ExternalServiceException
│       ├── ExternalBadRequestException
│       ├── ExternalForbiddenException
│       └── ExternalUnauthorizedException
└── auth/token/exception/
    ├── InvalidTokenException (extends UnauthorizedException)
    │   ├── MissingClaimException
    │   ├── TokenExpiredException
    │   └── TokenNotFoundException
    └── TokenBlacklistedException (extends UnauthorizedException)
```

**ErrorResponse 형식**

```json
{
  "trace_id": "64f643976895e486ca47ab3456e06f4b",
  "span_id": "d99a14be3c3c3a5f",
  "status": 400,
  "data": {
    "code": "INVALID_INPUT",
    "message": "잘못된 요청입니다"
  }
}
```

**참고**:

- `trace_id`/`span_id`: OpenTelemetry 분산 추적용 식별자 (Jaeger 연동)
- `status`: HTTP 상태 코드
- `data`: 제네릭 래퍼 - 성공 시 실제 데이터, 실패 시 ErrorData
- ErrorData 구조: `{ code: String, message: String? }`

**구현 파일**:

- `src/main/kotlin/io/chaekpool/common/dto/ApiResponse.kt`
- `src/main/kotlin/io/chaekpool/common/dto/ErrorData.kt`

**에러 메시지 규칙**

- 모든 에러 메시지는 마침표(`.`)로 끝나지 않음
- 일관된 한국어/영어 메시지 형식 유지
- 예시:
    - ✅ "사용자를 찾을 수 없습니다" (O)
    - ❌ "사용자를 찾을 수 없습니다." (X)
    - ✅ "Invalid JWT token" (O)
    - ❌ "Invalid JWT token." (X)

**테스트 작성 규칙**

- 테스트 파일 명명: `???Test.kt` 형식 (JUnit 스타일 유지)
- Exception 테스트 시 **타입 → httpStatus → errorCode → message** 순서로 반드시 모두 검증

```kotlin
val exception = shouldThrow<UserNotFoundException> {
    userService.getUser(userId)
}
exception.shouldBeInstanceOf<NotFoundException>()
exception.httpStatus shouldBe HttpStatus.NOT_FOUND
exception.errorCode shouldBe "USER_NOT_FOUND"
exception.message shouldBe "사용자를 찾을 수 없습니다"
```

---

### 3. 유틸리티 및 확장 함수

**AssertionExtension.kt** - 검증 헬퍼

```kotlin
// Boolean 검증
condition.isTrueOrThrow { BadRequestException("조건 불만족") }

// Null 검증
value.notNullOrThrow { NotFoundException("값 없음") }

// 문자열 검증
input.hasTextOrThrow { BadRequestException("빈 문자열") }

// 컬렉션 검증
list.notEmptyOrThrow { BadRequestException("빈 리스트") }

// 조건부 검증
number.requireOrThrow({ it > 0 }) { BadRequestException("양수 필요") }
```

---

### 4. 필터 및 ThreadLocal

**필터 구현**

```kotlin
@Component
class CustomFilter : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            // ThreadLocal 사용
            context.set(value)
            filterChain.doFilter(request, response)
        } finally {
            context.clear()  // ✅ 반드시 cleanup
        }
    }
}
```

**실행 순서**

1. `AccessLogFilter` (HIGHEST_PRECEDENCE + 2)
2. `UserMetadataFilter` (순서 미지정)
3. `JwtAuthenticationFilter` (Spring Security FilterChain)

---

### 5. 커스텀 어노테이션

**정의**

```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AccessUserId
```

**Resolver 구현**

```kotlin
@Component
class AccessUserIdResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(AccessUserId::class.java)

    override fun resolveArgument(...): Any? {
        // JWT에서 userId 추출
    }
}
```

**어노테이션 배치 규칙**

**클래스/인터페이스 레벨**: 선언부 바로 위 (개행 없음)

```kotlin
@Service
class UserService(private val userRepository: UserRepository) { }

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
class AccessLogFilter : OncePerRequestFilter() { }

@ConfigurationProperties(prefix = "auth.jwt")
data class JwtProperties(...)
```

**파라미터 어노테이션**: `@param:` 접두사 (Jackson/Validation)

```kotlin
data class UserResponse(
    @param:JsonProperty("user_id") val userId: UUID,
    @param:JsonProperty("email") val email: String?
)
```

**메서드 파라미터**: 직접 적용

```kotlin
fun getUser(@AccessUserId userId: UUID): UserResponse
fun createUser(@Valid @RequestBody request: CreateUserRequest)
```

**필드 어노테이션**: 특별한 경우만 사용 (일반적으로 생성자 주입 선호)

```kotlin
// ❌ 필드 주입 금지
@Autowired
private lateinit var userRepository: UserRepository

// ✅ 생성자 주입
class UserService(private val userRepository: UserRepository)
```

---

### 6. Transaction 및 메서드 설계

**@Transactional 주의사항**

**Self-Invocation 문제**

```kotlin
// ❌ Bad: private 메서드는 프록시를 거치지 않아 @Transactional 무시됨
@Service
class UserService {
    @Transactional
    fun publicMethod() {
        privateMethod()  // Self-invocation: @Transactional 작동 안 함
    }

    @Transactional
    private fun privateMethod() { ... }
}

// ✅ Good: 별도 Service로 분리 또는 public으로 변경
@Service
class UserService(private val userHelper: UserHelper) {
    @Transactional
    fun publicMethod() {
        userHelper.transactionalMethod()  // 외부 호출: 정상 작동
    }
}

@Service
class UserHelper {
    @Transactional
    fun transactionalMethod() { ... }
}
```

**Transactional 범위 주의사항**:

- **Self-Invocation**: 같은 클래스 내 private/internal 메서드 호출 시 프록시를 거치지 않음
- **해결책**: 별도 Service로 분리, public 메서드로 변경, 또는 `TransactionalEventListener` 활용
- **읽기 전용**: 조회만 하는 경우 `@Transactional(readOnly = true)` 사용
- **최소 범위**: 트랜잭션은 필요한 최소 범위로만 설정

**메서드 길이 제한**

- **30 line 초과**: ⚠️ WARNING - 리팩토링 검토 필요 (사용자 판단 위임)
- **50 line 초과**: ❌ 불가 - 반드시 분리 필수
- **원칙**: 하나의 메서드는 하나의 책임만 (Single Responsibility)

**중첩 깊이 (Nesting Depth) 제한**

```kotlin
// ❌ Bad: 4 depth (불가)
fun process() {
    if (condition1) {           // depth 1
        for (item in items) {    // depth 2
            if (condition2) {    // depth 3
                when (type) {    // depth 4 - 금지!
                    ...
                }
            }
        }
    }
}

// ✅ Good: Early return으로 depth 감소
fun process() {
    if (!condition1) return     // depth 1

    for (item in items) {        // depth 1
        if (!condition2) continue // depth 2
        processItem(item)        // 별도 메서드로 분리
    }
}
```

**Depth 제한**:

- **3 depth**: ⚠️ WARNING - 리팩토링 권장 (사용자 판단 위임)
- **4 depth 이상**: ❌ 불가 - Early return, 메서드 분리, Guard clause 활용 필수

**코드 품질 개선 기법**:

- **Early Return**: 조건 불만족 시 즉시 반환
- **Guard Clause**: 예외 케이스 먼저 처리
- **Extract Method**: 복잡한 로직은 별도 메서드로 분리
- **Flatten Structure**: when/if 중첩 대신 sealed class/enum 활용

---

### 7. Redis 엔티티

```kotlin
@RedisHash(value = "auth:token:refresh", timeToLive = 604800)  // 7일
data class RefreshTokenEntity(
    @Id
    val jti: String,  // JWT ID (토큰 고유 식별자)

    @Indexed
    val userId: UUID,  // 사용자 UUID

    val ip: String?,
    val userAgent: String?,
    val device: String?,
    val platformType: String?
)
```

**Key Changes**:

- Entity name: `RefreshToken` → `RefreshTokenEntity`
- Hash key: `"refresh_token"` → `"auth:token:refresh"` (namespaced)
- ID field: `token` → `jti` (JWT standard claim)
- userId type: `Long` → `UUID`
- Removed: `createdAt` (Redis TTL handles expiration)
- Added: Security metadata fields (ip, userAgent, device, platformType)

**Reference**: `src/main/kotlin/io/chaekpool/auth/token/entity/RefreshTokenEntity.kt`

---

### 8. Feign 클라이언트

```kotlin
@FeignClient(
    name = "kakaoAuthClient",
    url = "https://kauth.kakao.com",
    configuration = [OAuth2FeignConfig::class]
)
interface KakaoAuthClient {
    @PostMapping(
        value = ["/oauth/token"],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun postOAuthToken(
        @RequestParam("grant_type") grantType: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("client_secret") clientSecret: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("code") code: String
    ): KakaoAuthTokenResponse
}
```

**Key Changes**:

- Method: `getToken` → `postOAuthToken` (명명 규칙 준수)
- Parameters: `@RequestBody` → `@RequestParam` (application/json OAuth2 표준)
- Configuration: `FeignConfig` → `OAuth2FeignConfig` (실제 클래스명)
- Content-Type: application/json (OAuth2 spec)

**Reference**: `src/main/kotlin/io/chaekpool/auth/oauth2/client/KakaoAuthClient.kt`

**OAuth2FeignConfig**

- `SingleLineFeignLogger`: 요청/응답을 한 줄로 로깅
- `FeignErrorDecoder`: HTTP 에러를 `ExternalServiceException`으로 변환

---

### 9. Security 설정

**특징**

- Stateless Session (세션 없음)
- CSRF, FormLogin, HttpBasic 비활성화
- JWT 기반 인증
- `/api/v1/auth/oauth2/*/authorize`, `/api/v1/auth/oauth2/*/callback`, `/api/v1/auth/token/refresh`,
  `/api/v1/auth/swagger/**`, `/swagger-oauth2-ui/**`, `/v3/api-docs/**`, `/swagger-ui/**` 공개, 나머지 인증 필요

**필터 체인**

```
AccessLogFilter
→ UserMetadataFilter
→ Spring Security FilterChain
  → JwtAuthenticationFilter
  → ExceptionTranslationFilter
  → AuthorizationFilter
```

---

## Testing

### 테스트 프레임워크

**Kotest 6.1.0 (BDD 스타일)**

```kotlin
class UserServiceTest : BehaviorSpec({
    lateinit var userRepository: UserRepository
    lateinit var userService: UserService

    beforeTest {
        userRepository = mockk()
        userService = UserService(userRepository)
    }

    Given("존재하는 userId가 주어졌을 때") {
        val userId = UUIDv7.generate()   // 순수 데이터: Given에 배치

        When("getUser를 호출하면") {
            Then("사용자 정보를 반환한다") {
                // Mock 설정 + 실행 + 검증: Then에 배치
                every { userRepository.findById(userId) } returns Users(...)

                val result = userService.getUser(userId)

                result.email shouldBe "test@example.com"
            }
        }
    }
})
```

**BDD Pattern (Best Practice)**:

- **Given**: 순수 테스트 데이터 준비 (`val token`, `val userId`, `MockHttpServletRequest` 등)
- **When → Then**: Mock 설정(`every`) + 실행(Act) + 검증(Assert)

**Reference**:

- Official: [Kotest Testing Styles](https://kotest.io/docs/framework/testing-styles.html)
- Tutorial: [Introduction to Kotest | Baeldung](https://www.baeldung.com/kotlin/kotest)

**BehaviorSpec 구조 (Given-When-Then)** - 블록별 배치 규칙

```kotlin
Given("테스트 전제조건") {
    // 순수 데이터만 (lateinit mock에 의존하지 않는 값)
    val token = "test-token"
    val userId = UUIDv7.generate()

    When("메서드를 호출하면") {
        Then("결과를 검증한다") {
            // Mock 설정 (every) + 실행 (Act) + 검증 (Assert)
            every { mock.method() } returns value

            val result = service.method()

            result shouldBe expected
            verify { mock.method() }
        }
    }
}
```

**블록별 배치 규칙**:

- **Given**: 순수 데이터 준비 - lateinit mock에 의존하지 않는 값 (`val`, DTO 생성 등)
- **When → Then**: Mock 설정(`every`) + 실행(Act) + 검증(Assert, `verify`)
- **⚠️ 주의**: `every { mock.method() }` 등 lateinit mock 의존 코드는 반드시 Then 내부에 배치 (Given/When에서는 `beforeTest` 실행 전이므로 초기화 안
  됨)

**MockK 1.14.9 (Mocking 규칙)**

```kotlin
// Mock 생성 (beforeTest에서)
beforeTest {
    repository = mockk()  // relaxed = true 사용 금지
    service = Service(repository)
}

// Mock 설정 + 실행 + 검증 (Then 블록에서)
every { repository.findById(1L) } returns user
val result = service.getUser(1L)
result shouldBe expected
verify(exactly = 1) { repository.findById(1L) }
```

**Testcontainers 2.0.3 (통합 테스트)**

```kotlin
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {
    @Bean
    @ServiceConnection
    fun postgres() = PostgreSQLContainer("postgres:18.2-alpine3.23")

    @Bean
    @ServiceConnection(name = "redis")
    fun valkey() = GenericContainer("valkey/valkey:9.0.2-alpine").withExposedPorts(6379)
}
```

### 테스트 실행

```bash
./gradlew test                                      # 전체 테스트
./gradlew test --tests "*UserServiceTest"          # 특정 테스트
./gradlew test --tests "*Test" --rerun-tasks       # 캐시 무시 재실행
```

---

## Commit Convention

**Angular-style (improved) - 한국어 사용**

### Format

```
<type>(<scope>): <한국어 요약>

- add: 파일명 (설명)
- modify: 파일명 (설명)
- delete: 파일명 (설명)

BREAKING CHANGE: 설명 (있을 경우만)
```

### Types

- `feat`: 새로운 기능
- `fix`: 버그 수정
- `refactor`: 리팩토링 (기능 변경 없음)
- `style`: 코드 포맷팅, 세미콜론 등
- `docs`: 문서 변경
- `test`: 테스트 추가/수정
- `chore`: 빌드, 설정 변경
- `perf`: 성능 개선
- `ci`: CI 설정 변경
- `build`: 빌드 시스템 변경

### Rules

1. **scope**: 소문자, 선택사항 (`auth`, `common`, `user`, `test` 등)
2. **제목**: 한국어, 명령형, 마침표 없음, 72자 이하
3. **본문**: 파일 단위 변경사항 (`add:`, `modify:`, `delete:`) + 로직 설명
4. **커밋 세분화**: 기능/로직/시간 기준으로 분리, 하나의 논리적 변경 = 하나의 커밋. 여러 작업이 누적된 경우 시간 순서대로 커밋을 나누어 진행
5. **Co-Authored-By 절대 금지**

### Examples

```
feat(auth): token refresh, rotate 기능 구현

- add: TokenService.kt (refresh, rotate 로직)
- modify: TokenController.kt (refresh 엔드포인트 추가)
- add: TokenPair.kt (access + refresh 토큰 쌍 DTO)
```

```
test(common): AssertionExtension 단위 테스트 추가

- add: AssertionExtensionTest.kt (5개 확장 함수 검증)
```

```
fix(common): UserMetadataFilter ThreadLocal 메모리 누수 수정

- modify: UserMetadataFilter.kt (finally 블록에 clear() 추가)
```

---

## Important Rules

### 🚨 절대 금지 사항

1. **Co-Authored-By 금지** - 커밋 메시지에 공동 작성자/협력자 표기 절대 추가 금지
2. **불필요한 주석 금지** - AI 생성 주석, 자명한 설명 주석 금지
3. **비밀 정보 커밋 금지** - 시크릿 키, 비밀번호, API 토큰 등 민감정보 절대 커밋 금지
4. **deprecated 무시 금지** - `@Suppress("DEPRECATION")` 대신 올바른 API로 마이그레이션
5. **버전 하드코딩 금지** - build.gradle.kts 상단에 `val` 변수로 버전 관리 (단, plugins 블록 제외)

### ✅ 필수 준수 사항

1. **기존 패턴 준수** - 새 코드는 반드시 위 컨벤션과 기존 코드 패턴 따름
2. **한국어 커밋** - 커밋 메시지 제목/본문 모두 한국어 사용
3. **테스트 작성** - 새 기능은 반드시 단위 테스트 포함
4. **테스트 연동 수정** - 코드 추가/수정/삭제 시 관련 테스트를 분석하여 전부 수정
5. **경고 제거** - 빌드 시 warning, deprecated 등 모든 경고 해결
6. **ThreadLocal cleanup** - ThreadLocal 사용 시 `try-finally`로 반드시 정리
7. **CLAUDE.md 동기화** - 모든 작업 과정 중 그리고 종료 시점에 CLAUDE.md와 실제 코드 간 불일치가 있으면 사용자에게 질문 후 최신 정보로 지속적으로 업데이트
8. **BP/레퍼런스 조사 (필수)** - 구현 전 **WebSearch 또는 mcp__fetch__fetch 도구 사용 필수**
    - 공식 문서 최신 버전 확인 (Spring Boot, Kotlin, Kotest 등)
    - Best Practice 검색 (예: "Kotlin service layer best practices 2026")
    - 레퍼런스 구현 확인 (GitHub 검색, Stack Overflow)
    - 보안 취약점 체크 (OWASP 가이드라인)
    - **구현 후가 아닌 설계 단계에서 조사 수행**
9. **모호한 사항 즉시 질의 (추측 금지)** - 불확실한 사항은 구현 전 **반드시** 사용자에게 질문
    - **네이밍**: 클래스/메서드/변수명이 애매할 때
    - **API 설계**: 엔드포인트 구조, 파라미터 형식, 응답 포맷
    - **에러 처리**: 어떤 Exception을 던질지, errorCode는 무엇인지
    - **테스트 범위**: 어디까지 테스트할지, 통합 vs 단위
    - **설정 값**: 기본값, TTL, pool size 등
    - **⚠️ 추측으로 진행 금지**: "아마 이럴 것 같다"는 금물, 반드시 확인
    - **AskUserQuestion 도구 적극 활용**

---

## Version Management

**build.gradle.kts 상단에서 중앙 관리**

```kotlin
val kotlinLoggingJvmVersion = "7.0.3"
val lokiLogbackAppenderVersion = "2.0.3"
val springCloudVersion = "2025.1.1"
val uaJavaVersion = "1.6.1"
val kotestVersion = "6.1.0"
val mockkVersion = "1.14.9"
val springmockkVersion = "5.0.1"
val springdocVersion = "3.0.1"
```

**단, plugins 블록은 하드코딩**

```kotlin
plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
}
```
