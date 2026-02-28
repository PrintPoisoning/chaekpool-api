# Swagger UI 사용 가이드

## 시작하기

### Step 1: 서버 실행

```bash
# 인프라 기동
docker compose --env-file .env.local up -d

# 앱 실행
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

### Step 2: Swagger UI 접속

브라우저에서 [/swagger-ui/index.htm](http://localhost:8080/swagger-ui.html) 접근

![Swagger UI 메인 화면](images/swagger-ui-main.png)

---

## 빠른 시작: Dev Login으로 API 테스트

### Step 1: Dev Login 호출

1. **Dev** 태그를 펼칩니다
2. `POST /api/v1/auth/dev/login` 클릭
3. **Try it out** 버튼 클릭
4. `username` 파라미터를 원하는 값으로 입력 (기본값: `dev-user`)
5. **Execute** 클릭

![Dev Login 실행](images/swagger-dev-login.png)

### Step 2: access_token 복사

응답 Body에서 `data.access_token` 값을 복사합니다.

```json
{
  "trace_id": "...",
  "span_id": "...",
  "status": 200,
  "data": {
    "access_token": "eyJhbGciOi..."  ← 이 값을 복사
  }
}
```

### Step 3: Authorize 설정

1. 페이지 상단의 **Authorize** 버튼 (자물쇠 아이콘) 클릭

![Authorize 버튼](images/swagger-authorize-button.png)

2. **Value** 필드에 복사한 `access_token` 값을 붙여넣기
3. **Authorize** 클릭 → **Close**

![Authorize 입력](images/swagger-authorize-input.png)

### Step 4: 인증된 API 호출

이제 인증이 필요한 API를 테스트할 수 있습니다.

- `GET /api/v1/users/me` → 200 OK + 사용자 정보 반환
- `DELETE /api/v1/auth/token` → 204 No Content (로그아웃)

![인증된 API 호출 결과](images/swagger-users-me.png)

---

## 카카오 OAuth로 API 테스트

### Step 1: 카카오 로그인

브라우저 주소창에 아래 URL을 직접 입력하여 카카오 로그인 페이지로 이동합니다:

```
http://localhost:8080/api/v1/auth/oauth2/kakao/authorize
```

서버가 자동으로 카카오 로그인 페이지로 리다이렉트하며, 로그인 완료 후 callback URL로 돌아옵니다.

### Step 2: access_token 확인

카카오 로그인 완료 후 callback 엔드포인트가 자동 호출되어 JWT 토큰이 발급됩니다. 응답에서 `access_token`을 복사합니다.

### Step 3: Authorize 설정

위 "빠른 시작" Step 3과 동일하게 Authorize 설정 후 API 테스트

---

## 참고

### 토큰 자동 유지 (persistAuthorization)

`persist-authorization: true` 설정으로 브라우저를 새로고침해도 Authorize에 입력한 토큰이 유지됩니다. 브라우저 localStorage에 저장되므로 탭을 닫아도 유지됩니다.

### 프로필별 활성화/비활성화

| 프로필 | Swagger UI | Dev Login |
|--------|-----------|-----------|
| local  | O         | O         |
| dev    | O         | O         |
| prod   | X (기본)  | X         |

- `springdoc.api-docs.enabled: true`는 local/dev 프로필에서만 명시적으로 활성화
- `DevAuthController`는 `@Profile("local", "dev")`로 local/dev에서만 빈 등록

### 토큰 만료 시 갱신

access_token이 만료되면 (기본 15분) **Token** 태그의 `POST /api/v1/auth/token/refresh`를 호출하세요. Dev Login 시 설정된 refresh_token 쿠키로 새 access_token이 발급됩니다. 발급받은 토큰으로 Authorize를 다시 설정하면 됩니다.

### 주의사항

- Dev Login으로 생성된 사용자는 테스트 용도이며, 매 호출마다 새 사용자가 생성됩니다
- 데이터를 초기화하려면: `docker compose --env-file .env.local down -v`
