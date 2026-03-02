# 책풀 API (chaekpool-api)

> 책을 고르고, 리뷰를 쓰고, 공유하는 기록형 SNS의 백엔드

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Kotlin 2.3.10 / Java 25 |
| Framework | Spring Boot 4.0.2 |
| Build | Gradle 9.3.1 (Kotlin DSL) |
| Database | PostgreSQL 18.2 + jOOQ 3.19.29 |
| Cache | Valkey 9.0.2 (Redis 호환) |
| Auth | Kakao OAuth2 + JWT |
| Migration | Flyway 11.14 |
| Monitoring | Prometheus + Loki + Jaeger + Grafana |
| API Docs | springdoc-openapi 3.0.1 |
| Test | Kotest 6.1.0 + MockK 1.14.9 + Testcontainers 2.0.3 |
| E2E | Playwright (Python) |
| CI/CD | Jenkins |

## 시작하기

### 사전 요구사항

- JDK 25+
- Docker & Docker Compose
- (선택) 카카오 개발자 앱 등록 — OAuth 로그인 테스트 시 필요

### 환경 설정

`.env.local` 파일에 개발 환경 설정이 포함되어 있으며 별도 수정 없이 사용 가능.

카카오 OAuth 로그인 테스트가 필요한 경우 `.env.local`에 발급받은 키 입력:

```
KAKAO_CLIENT_ID=<카카오 REST API 키>
KAKAO_CLIENT_SECRET=<카카오 Client Secret>
```

### 실행

```bash
# local 프로필로 실행 (Docker Compose 자동 실행)
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

수동으로 Docker Compose만 실행하는 경우:

```bash
# 실행
docker compose -f docker-compose.local.yml up -d

# 종료
docker compose -f docker-compose.local.yml down

# 종료 + 데이터 초기화 (볼륨 삭제)
docker compose -f docker-compose.local.yml down -v
```

## 빌드 명령어

| 명령어 | 설명 |
|---|---|
| `./gradlew build` | 전체 빌드 |
| `./gradlew compileKotlin` | 컴파일만 |
| `./gradlew bootRun` | 앱 실행 (port 8080) |
| `./gradlew test` | 전체 테스트 |
| `./gradlew jooqGenerate` | jOOQ 코드 생성 |

## 프로젝트 구조

```
src/main/kotlin/io/chaekpool/
├── auth/                    # 인증/인가 (OAuth, JWT, Token)
│   ├── oauth2/              # 카카오 OAuth2 로그인
│   ├── token/               # JWT 토큰 관리
│   └── swagger/             # Swagger OAuth2 인증 UI
├── common/                  # 공통 기능 (보안, 필터, 예외, 유틸)
└── user/                    # 사용자 관리
```

## 인프라 구성

`docker-compose.local.yml`로 로컬 개발 환경 구성.

| 서비스 | 이미지 | 포트 | 설명 |
|---|---|---|---|
| PostgreSQL | postgres:18.2-alpine3.23 | 5432 | 데이터베이스 |
| Valkey | valkey/valkey:9.0.2-alpine | 6379 | 캐시 (Redis 호환) |
| Prometheus | prom/prometheus:v3.9.1 | 9090 | 메트릭 수집 |
| Jaeger | jaegertracing/all-in-one:1.76.0 | 16686 | 분산 추적 |
| Loki | grafana/loki:3.4.2 | 3100 | 로그 수집 |
| Grafana | grafana/grafana:11.5.2 | 3000 | 모니터링 대시보드 |

## API 문서

| URL | 설명 |
|---|---|
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI |
| `http://localhost:8080/swagger-oauth2-ui/kakao.html` | 카카오 OAuth 팝업 로그인 Swagger |
| `http://localhost:8080/v3/api-docs` | OpenAPI Spec (JSON) |

`local`, `dev` 프로필에서만 활성화.

## 테스트

### 단위 / 통합 테스트

- **단위 테스트**: Kotest (BDD) + MockK
- **통합 테스트**: Testcontainers (PostgreSQL, Valkey)

```bash
# 전체 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests "*UserServiceTest"

# 캐시 무시 재실행
./gradlew test --tests "*Test" --rerun-tasks
```

### E2E 테스트

Playwright (Python) 기반. 자세한 내용은 [`e2e/README.md`](e2e/README.md) 참고.

```bash
cd e2e
source .venv/bin/activate
python test_auth_flow.py
```

## 모니터링

| 도구 | URL | 비고 |
|---|---|---|
| Grafana | `http://localhost:3000` | admin / admin |
| Prometheus | `http://localhost:9090` | |
| Jaeger | `http://localhost:16686` | |

## 환경 변수

`.env.local` 기반. 프로덕션 환경에서는 환경변수로 주입.

| 구분 | 변수 | 기본값 | 설명 |
|---|---|---|---|
| Server | `SERVER_PORT` | 8080 | 서버 포트 |
| DB | `POSTGRES_URL` | jdbc:postgresql://localhost:5432/cp | JDBC URL |
| DB | `POSTGRES_USER` | admin | DB 사용자 |
| DB | `POSTGRES_PASSWORD` | admin | DB 비밀번호 |
| DB | `POSTGRES_DB` | cp | DB 이름 |
| DB | `POSTGRES_PORT` | 5432 | DB 포트 |
| Cache | `CACHE_HOST` | localhost | Valkey 호스트 |
| Cache | `CACHE_PORT` | 6379 | Valkey 포트 |
| Cache | `CACHE_PASSWORD` | admin | Valkey 비밀번호 |
| Crypto | `CRYPTO_SECRET_KEY` | (개발용 고정값) | 암호화 키 |
| JWT | `JWT_SECRET` | (개발용 고정값) | JWT 서명 키 |
| JWT | `JWT_ACCESS_TOKEN_VALIDITY_SECONDS` | 900 | Access Token 유효시간 (초) |
| JWT | `JWT_REFRESH_TOKEN_VALIDITY_SECONDS` | 604800 | Refresh Token 유효시간 (초) |
| OAuth | `KAKAO_CLIENT_ID` | (직접 입력) | 카카오 REST API 키 |
| OAuth | `KAKAO_CLIENT_SECRET` | (직접 입력) | 카카오 Client Secret |
| OAuth | `KAKAO_REDIRECT_URI` | http://localhost:8080/api/v1/auth/oauth2/kakao/callback | OAuth 콜백 URL |
| CORS | `CORS_ALLOWED_ORIGINS` | http://localhost:8080,http://localhost:3000 | 허용 Origin |
| Monitoring | `GRAFANA_PORT` | 3000 | Grafana 포트 |
| Monitoring | `PROMETHEUS_PORT` | 9090 | Prometheus 포트 |
| Monitoring | `JAEGER_UI_PORT` | 16686 | Jaeger UI 포트 |
| Monitoring | `LOKI_PORT` | 3100 | Loki 포트 |
