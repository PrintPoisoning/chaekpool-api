# CLAUDE.md - chaekpool-api

## Project

책풀(chaekpool) API - 책을 고르고, 리뷰를 쓰고, 공유하는 기록형 SNS의 백엔드.
Spring Boot 4.0.2 / Kotlin 2.3.10 / Java 25 / Gradle (Kotlin DSL)

## Build & Run

```bash
./gradlew build              # 전체 빌드
./gradlew compileKotlin      # 컴파일 확인
./gradlew bootRun            # 앱 실행
./gradlew test               # 테스트

# 로컬 인프라 (PostgreSQL, Valkey, Prometheus, Loki, Jaeger, Grafana)
docker compose --env-file .env.local up -d
```

환경변수: `.env.example` 참고하여 `.env.local` 생성. `.env.local`은 절대 커밋하지 않는다.

## Architecture

```
io.chaekpool/
├── auth/           # 인증/인가 (OAuth, JWT, Token 관리)
│   ├── annotation/ # @AccessUserId, @RefreshUserId, @AccessToken, @RefreshToken + Resolver
│   ├── oauth/      # Kakao OAuth (Feign client, controller, service, DTO)
│   └── token/      # JWT 발급/검증, Refresh/Blacklist (Redis), Cookie
├── common/         # 공통 (보안, 예외, 필터, 유틸, 설정)
│   ├── config/     # WebSecurity, AuthorizationRules, CORS, Metrics, Snowflake
│   ├── exception/  # ServiceException -> internal/ (BadRequest, NotFound, ...) + external/
│   ├── filter/     # AccessLogFilter, UserMetadataFilter/Context
│   └── util/       # AssertionExtension, SnowflakeIdGenerator
└── monitoring/     # 모니터링 테스트용
```

- API 경로: `/api/v1/...`
- DB: PostgreSQL 18.2 (JPA) + Flyway 마이그레이션
- 캐시/세션: Valkey 9.0.2 (Redis 호환) - RefreshToken, Blacklist 저장
- 모니터링: Prometheus + Loki + Jaeger(OpenTelemetry OTLP) + Grafana
- 프로필: `local` (Docker Compose), `dev` (서버 배포)

## Code Conventions

**Logging** - `private val log = KotlinLogging.logger {}` 만 사용. LoggerFactory, LoggerDelegate 사용 금지.

**DTO** - `data class` + `@param:JsonProperty("snake_case")` 로 JSON 매핑.

**DI** - 생성자 주입만 사용. 필드 주입 금지.

**Properties** - `@ConfigurationProperties` data class + prefix 스캔.

**예외 체계** - `ServiceException(errorCode, httpStatus, message)` 상속 구조.
- `common/exception/internal/`: BadRequest, NotFound, Forbidden, Conflict, Unauthorized, InternalServerError
- `common/exception/external/`: 외부 서비스 호출 실패
- `auth/token/exception/`: InvalidToken, TokenExpired, TokenBlacklisted, TokenNotFound, MissingClaim
- `ErrorResponse`: status, error, message, error_code, path, timestamp

**검증 유틸** - `AssertionExtension.kt` 확장 함수: `.isTrueOrThrow {}`, `.notNullOrThrow {}`, `.hasTextOrThrow {}`, `.notEmptyOrThrow {}`, `.requireOrThrow({}) {}`

**필터** - `OncePerRequestFilter` 상속, ThreadLocal 사용 시 `try-finally`로 반드시 cleanup.

**커스텀 어노테이션** - `@Target(VALUE_PARAMETER)`, `@Retention(RUNTIME)` + `HandlerMethodArgumentResolver` 구현.

**ID 생성** - `fun interface IdGenerator { fun nextId(): Long }` / Snowflake 기반.

**Redis 엔티티** - `@RedisHash(value, timeToLive)` + `@Id`, `@Indexed`.

**Feign** - `@FeignClient` 인터페이스 기반, `SingleLineFeignLogger`, `FeignErrorDecoder` 사용.

**Security** - Stateless, CSRF/FormLogin/HttpBasic 비활성, `JwtAuthenticationFilter` + `UserMetadataFilter` 등록.

## Commit Convention

Angular-style (improved). 커밋 메시지는 한국어로 작성한다.

### Format

```
<type>(<scope>): <한국어 요약>

- add: 파일명 (설명)
- modify: 파일명 (설명)
- delete: 파일명 (설명)

BREAKING CHANGE: 설명 (있을 경우만)
```

### Types

`feat` `fix` `refactor` `style` `docs` `test` `chore` `perf` `ci` `build`

### Rules

- scope: 소문자, 선택사항 (auth, common, monitoring, config 등)
- 제목: 한국어, 명령형, 마침표 없음, 72자 이하
- 본문: 파일 단위 변경사항 (`add:`, `modify:`, `delete:`) + 로직 설명
- 커밋은 세분화: 기능/로직/시간 기준으로 분리, 하나의 논리적 변경 = 하나의 커밋
- **Co-Authored-By 절대 사용 금지**

### Examples

```
feat(auth): token refresh, rotate 기능 구현

- add: TokenService.kt (refresh, rotate 로직)
- modify: TokenController.kt (refresh 엔드포인트 추가)
- add: TokenPair.kt (access + refresh 토큰 쌍 DTO)
```

```
fix(common): 잘못된 url mapping 수정

- modify: CommonController.kt (robots.txt 경로 수정)
```

## Important Rules

1. **Co-Authored-By 금지** - 커밋 메시지에 공동 작성자/협력자 표기를 절대 추가하지 않는다
2. **불필요한 주석 금지** - AI가 생성한 주석, 자명한 코드 설명 주석을 남기지 않는다
3. **비밀 정보 금지** - `.env.local`, 시크릿 키, 비밀번호를 절대 커밋하지 않는다
4. **기존 패턴 준수** - 새 코드는 반드시 위 컨벤션과 기존 코드 패턴을 따른다
5. **한국어 커밋** - 커밋 메시지 제목/본문 모두 한국어로 작성한다
