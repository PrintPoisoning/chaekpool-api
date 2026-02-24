# CLAUDE.md - chaekpool-api

## Project

책풀(chaekpool) API - 책을 고르고, 리뷰를 쓰고, 공유하는 기록형 SNS의 백엔드

**Tech Stack:**
- Spring Boot 4.0.2 / Kotlin 2.3.10 / Java 25
- Build: Gradle 9.3.1 (Kotlin DSL)
- Database: PostgreSQL 18.2 + jOOQ 3.19
- Cache: Valkey 9.0.2 (Redis)
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
# PostgreSQL, Valkey, Prometheus, Loki, Jaeger, Grafana 실행
docker compose --env-file .env.local up -d

# 종료
docker compose --env-file .env.local down
```

### 환경 변수
- `.env.example` 참고하여 `.env.local` 생성
- `.env.local`은 **절대 커밋 금지** (`.gitignore` 등록됨)

---

## Architecture

### 모듈 구조
```
io.chaekpool/
├── auth/                    # 인증/인가 (OAuth, JWT, Token)
│   ├── annotation/          # @AccessUserId, @RefreshUserId, @AccessToken, @RefreshToken
│   ├── dto/                 # AuthResponse
│   ├── exception/           # AuthException
│   ├── handler/             # CustomAccessDeniedHandler, CustomAuthenticationEntryPoint
│   ├── oauth/               # Kakao OAuth 로그인
│   │   ├── client/kakao/    # KakaoAuthClient, KakaoUserClient (Feign)
│   │   ├── config/          # FeignConfig (Logger, ErrorDecoder)
│   │   ├── controller/      # OAuthKakaoController (로그인/콜백)
│   │   ├── dto/kakao/       # KakaoTokenResponse, KakaoUserInfoResponse
│   │   └── service/         # KakaoOAuthService
│   └── token/               # JWT 토큰 관리
│       ├── config/          # JwtProperties
│       ├── controller/      # TokenController (refresh, rotate, logout)
│       ├── dto/             # TokenPair, TokenResponse
│       ├── entity/          # RefreshToken, TokenBlacklist (Redis)
│       ├── exception/       # InvalidToken, TokenExpired, TokenBlacklisted 등
│       ├── filter/          # JwtAuthenticationFilter
│       ├── repository/      # RefreshTokenRepository, TokenBlacklistRepository
│       └── service/         # TokenService, CookieService, JwtProvider
├── common/                  # 공통 기능
│   ├── config/              # WebSecurityConfig, CorsProperties, MetricsConfig
│   ├── controller/          # CommonController (robots.txt, healthcheck)
│   ├── dto/                 # ErrorResponse, UserMetadata
│   ├── exception/           # ServiceException 계층
│   │   ├── internal/        # BadRequest, NotFound, Forbidden, Conflict, Unauthorized, InternalServerError
│   │   └── external/        # ExternalServiceException
│   ├── filter/              # AccessLogFilter, UserMetadataFilter/Context
│   ├── handler/             # GlobalExceptionHandler
│   ├── logger/              # SingleLineFeignLogger
│   └── util/                # AssertionExtension, UserMetadataExtractor
└── user/                    # 사용자 관리
    ├── controller/          # UserController
    ├── dto/                 # UserResponse
    ├── repository/          # UserRepository (jOOQ)
    └── service/             # UserService
```

### 테스트 구조
```
src/test/kotlin/io/chaekpool/
├── config/                  # TestcontainersConfig (PostgreSQL, Valkey)
├── common/
│   ├── filter/              # AccessLogFilterTest, UserMetadataFilterTest, UserMetadataContextTest
│   └── util/                # AssertionExtensionTest, UserMetadataExtractorTest
└── ChaekpoolApplicationTests.kt  # Spring Context 로드 테스트
```

### 주요 기술 스택
- **API 경로**: `/api/v1/...`
- **Database**: PostgreSQL 18.2 + jOOQ (Type-safe SQL)
- **Migration**: Flyway 11.14 (`src/main/resources/db/migration`)
- **Cache/Session**: Valkey 9.0.2 (Redis 호환) - RefreshToken, TokenBlacklist 저장
- **Monitoring**: Prometheus + Loki + Jaeger (OpenTelemetry OTLP) + Grafana
- **Profile**: `local` (Docker Compose), `dev` (서버 배포)

---

## Code Conventions

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
    val accessTokenValiditySeconds: Long
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
│   ├── ForbiddenException
│   ├── ConflictException
│   ├── UnauthorizedException
│   └── InternalServerErrorException
├── external/
│   └── ExternalServiceException
└── auth/token/exception/
    ├── InvalidTokenException
    ├── TokenExpiredException
    ├── TokenBlacklistedException
    ├── TokenNotFoundException
    └── MissingClaimException
```

**ErrorResponse 형식**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "잘못된 요청입니다",
  "error_code": "INVALID_INPUT",
  "path": "/api/v1/users",
  "timestamp": "2026-02-23T10:30:00.000Z"
}
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

---

### 6. Redis 엔티티

```kotlin
@RedisHash(value = "refresh_token", timeToLive = 604800)  // 7일
data class RefreshToken(
    @Id val token: String,
    @Indexed val userId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
```

---

### 7. Feign 클라이언트

```kotlin
@FeignClient(
    name = "kakao-auth",
    url = "\${auth.oauth.kakao.auth-url}",
    configuration = [FeignConfig::class]
)
interface KakaoAuthClient {
    @PostMapping("/oauth/token")
    fun getToken(@RequestBody request: KakaoTokenRequest): KakaoTokenResponse
}
```

**FeignConfig**
- `SingleLineFeignLogger`: 요청/응답을 한 줄로 로깅
- `FeignErrorDecoder`: HTTP 에러를 `ExternalServiceException`으로 변환

---

### 8. Security 설정

**특징**
- Stateless Session (세션 없음)
- CSRF, FormLogin, HttpBasic 비활성화
- JWT 기반 인증
- `/api/v1/auth/**` 공개, 나머지 인증 필요

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

    Given("사용자 ID가 주어졌을 때") {
        val userId = 1L
        val user = User(id = userId, name = "Test")
        every { userRepository.findById(userId) } returns user

        When("findById를 호출하면") {
            val result = userService.findById(userId)

            Then("사용자 정보를 반환한다") {
                result.id shouldBe userId
            }
        }
    }
})
```

**BehaviorSpec 구조 (Given-When-Then)**
- **Given**: 테스트 컨텍스트 설정 - Mock 객체 생성, 테스트 데이터 준비, `every` 설정
- **When**: 테스트 대상 메서드 호출 - 실제 동작 실행
- **Then**: Assertion - 결과 검증, `verify` 호출

```kotlin
Given("Mock 설정과 테스트 데이터") {
    val testData = ...
    every { mock.method() } returns value  // ← Mock 설정은 Given에

    When("메서드를 호출하면") {
        val result = service.method()  // ← 실제 호출은 When에

        Then("결과를 검증한다") {
            result shouldBe expected  // ← Assertion은 Then에만
            verify { mock.method() }
        }
    }
}
```

**MockK 1.14.9 (Mocking 규칙)**
```kotlin
// Mock 생성 (beforeTest에서)
beforeTest {
    repository = mockk()  // relaxed = true 사용 금지
    service = Service(repository)
}

// Mock 설정 (Given 블록에서)
every { repository.findById(1L) } returns user
every { repository.save(any()) } just runs

// 검증 (Then 블록에서)
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
4. **커밋 세분화**: 기능/로직/시간 기준으로 분리, 하나의 논리적 변경 = 하나의 커밋
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
3. **비밀 정보 커밋 금지** - `.env.local`, 시크릿 키, 비밀번호 절대 커밋 금지
4. **deprecated 무시 금지** - `@Suppress("DEPRECATION")` 대신 올바른 API로 마이그레이션
5. **버전 하드코딩 금지** - build.gradle.kts 상단에 `val` 변수로 버전 관리 (단, plugins 블록 제외)

### ✅ 필수 준수 사항
1. **기존 패턴 준수** - 새 코드는 반드시 위 컨벤션과 기존 코드 패턴 따름
2. **한국어 커밋** - 커밋 메시지 제목/본문 모두 한국어 사용
3. **테스트 작성** - 새 기능은 반드시 단위 테스트 포함
4. **경고 제거** - 빌드 시 warning, deprecated 등 모든 경고 해결
5. **ThreadLocal cleanup** - ThreadLocal 사용 시 `try-finally`로 반드시 정리

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
val restdocsApiSpecVersion = "0.19.4"
```

**단, plugins 블록은 하드코딩**
```kotlin
plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.epages.restdocs-api-spec") version "0.19.4"
}
```
