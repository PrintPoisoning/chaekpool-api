# Configuration Cache (CC) 가이드

## 개요

Gradle Configuration Cache는 빌드 설정 단계(태스크 그래프 구성)의 결과를 캐싱하여, 이후 빌드에서 설정 단계를 완전히 스킵하는 기능이다.

### 적용 현황

| 설정                  | 값                              | 파일                  |
|---------------------|--------------------------------|---------------------|
| Configuration Cache | `true`                         | `gradle.properties` |
| Build Cache         | `true`                         | `gradle.properties` |
| JVM 옵션              | `-Xmx1024m -XX:+UseParallelGC` | `gradle.properties` |

### CC 호환을 위한 핵심 변경

`jooqGenerate` 태스크를 `doLast` 람다에서 `@CacheableTask abstract class`로 변환하여 CC 비호환 원인(`this$0` 빌드 스크립트 참조 캡처)을 제거했다.

```
Before: tasks.register("jooqGenerate") { doLast { ... } }   → CC 비호환
After:  @CacheableTask abstract class JooqGenerateTask       → CC 호환
```

---

## 빌드 시나리오별 동작

### 일상 빌드 (코드만 변경, 스키마 변경 없음) — 가장 빈번

```bash
./gradlew build
```

- **Configuration**: CC 재사용 (설정 단계 스킵)
- **jooqGenerate**: UP-TO-DATE (마이그레이션 SQL 미변경)
- **compileKotlin / test**: 변경된 소스만 재컴파일/재실행

### 스키마 변경 (마이그레이션 SQL 추가/수정)

```bash
./gradlew build
```

- **Configuration**: CC 재사용
- **jooqGenerate**: 재실행 (`@InputDirectory` 변경 감지)
- **compileKotlin / test**: 생성된 jOOQ 코드 변경으로 재실행

### 변경 없음 (CI 재빌드 등)

```bash
./gradlew build
```

- **Configuration**: CC 재사용
- **모든 태스크**: UP-TO-DATE
- **소요 시간**: ~1초

### CC가 무효화되는 경우

아래 변경이 발생하면 CC가 무효화되어 설정 단계가 한 번 재실행된다. 재실행 후 자동으로 다시 저장된다.

| 변경 대상                 | 예시                             |
|-----------------------|--------------------------------|
| `build.gradle.kts`    | 의존성 추가, 플러그인 변경, 태스크 설정 수정     |
| `gradle.properties`   | JVM 옵션, 설정값 변경                 |
| `settings.gradle.kts` | 모듈 구조 변경                       |
| Gradle 버전             | `gradle-wrapper.properties` 수정 |
| 빌드에 참조되는 환경 변수        | CC 입력으로 추적되는 경우                |

---

## 트러블슈팅

### CC 상태 확인

빌드 로그 첫 줄에서 CC 상태를 확인할 수 있다.

```
Reusing configuration cache.                                    ← CC 재사용
Calculating task graph as no cached configuration is available   ← CC 미스 (새로 계산)
```

빌드 로그 마지막에서 저장/재사용 여부를 확인할 수 있다.

```
Configuration cache entry stored.   ← 새로 저장됨
Configuration cache entry reused.   ← 캐시 재사용됨
```

### CC 캐시 수동 삭제

CC 관련 문제 발생 시 캐시를 삭제하고 재빌드한다.

```bash
rm -rf .gradle/configuration-cache
./gradlew build
```

### CC 비활성화 (폴백)

CC 직렬화 오류 등 해결이 어려운 경우, `gradle.properties`에서 비활성화한다.

```properties
org.gradle.configuration-cache=false
```

Build Cache + JVM 튜닝은 CC와 독립적으로 동작하므로 CC만 비활성화해도 기본적인 캐싱은 유지된다.

### jooqGenerate 강제 재실행

생성된 코드가 손상된 경우 clean 후 재생성한다.

```bash
./gradlew jooqClean jooqGenerate
```

---

## CI 운영 참고

### Docker 컨테이너 환경에서의 CC

Jenkins, GitHub Actions 등 Docker 컨테이너 기반 CI에서는 빌드마다 새 컨테이너가 생성되므로, 별도 조치 없이는 CC 캐시가 유실된다.

**캐시 저장 위치**

| 캐시 종류               | 저장 위치                                   | 비고                  |
|---------------------|-----------------------------------------|---------------------|
| Configuration Cache | **프로젝트** `.gradle/configuration-cache/` | 프로젝트 디렉토리 내부        |
| Build Cache         | `~/.gradle/caches/build-cache-1/`       | GRADLE_USER_HOME 내부 |
| 의존성 캐시              | `~/.gradle/caches/modules-2/`           | GRADLE_USER_HOME 내부 |

Build Cache와 의존성 캐시는 GRADLE_USER_HOME(`~/.gradle`)에 저장되므로 해당 경로만 Volume 마운트하면 보존된다. 반면 CC는 **프로젝트 디렉토리 내부**에 저장되므로 별도
처리가 필요하다.

### 이 프로젝트의 CI 구성 (Jenkins + Docker)

호스트 바인드 마운트(`/var/lib/jenkins/.gradle`)를 Docker 이미지의 `GRADLE_USER_HOME`(`/home/gradle/.gradle`)에 마운트하고, 프로젝트의 `.gradle`을 심링크하여 CC를 보존한다.

```groovy
// ci/Jenkinsfile
stage('Build') {
    steps {
        sh 'mkdir -p /home/gradle/.gradle/chaekpool-api && ln -sfn /home/gradle/.gradle/chaekpool-api .gradle'
        sh './gradlew assemble --no-daemon'
    }
}
```

**동작 원리**:

1. 호스트의 `/var/lib/jenkins/.gradle`이 컨테이너의 `/home/gradle/.gradle`에 바인드 마운트되어 빌드 간 보존됨
2. 그 안에 `chaekpool-api/` 디렉토리를 생성
3. 프로젝트의 `.gradle` → `/home/gradle/.gradle/chaekpool-api`로 심링크
4. CC 캐시가 `chaekpool-api/configuration-cache/`에 저장되어 다음 빌드에서 재사용

**전제 조건**: `disableConcurrentBuilds()`로 동시 빌드가 없어야 캐시 충돌이 발생하지 않는다.

### CI 캐시 적용 효과

Jenkins 빌드 #29 (캐시 적용) 기준, 코드 변경 없이 재빌드 시 측정 결과:

| 단계 | 캐시 미적용 (평균) | 캐시 적용 (#29) | 개선율 |
|---|---|---|---|
| Build (`assemble`) | ~1분 40초 | 19초 | ~81% 감소 |
| Test (`cleanTest test`) | ~58초 | 10초 | ~83% 감소 |
| **전체 파이프라인** | **~3분 4초** | **~52초** | **~72% 감소** |

**캐시 구성별 효과**:

- **Gradle 배포판 캐시**: 최초 1회 다운로드 후 재사용 (~5초 절약)
- **Configuration Cache**: 태스크 그래프 재계산 스킵 (~80초 절약)
- **의존성 캐시**: 라이브러리 재다운로드 방지
- **Build Cache**: UP-TO-DATE 태스크 스킵

### 다른 CI 환경 적용 예시

**GitHub Actions**:

```yaml
- uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
      .gradle/configuration-cache
    key: gradle-${{ hashFiles('**/*.gradle.kts', 'gradle.properties') }}
    restore-keys: gradle-
```

**GitLab CI**:

```yaml
cache:
  key: gradle
  paths:
    - .gradle/configuration-cache/
    - ~/.gradle/caches/
    - ~/.gradle/wrapper/
```

핵심은 동일하다. GRADLE_USER_HOME 외에 **프로젝트의 `.gradle/configuration-cache/`** 를 캐시 대상에 포함해야 CC가 빌드 간 보존된다.

### CC + Build Cache 조합

| 캐시 종류               | 대상              | 위치                                |
|---------------------|-----------------|-----------------------------------|
| Configuration Cache | 태스크 그래프 (설정 단계) | `.gradle/configuration-cache/`    |
| Build Cache         | 태스크 출력물 (실행 단계) | `~/.gradle/caches/build-cache-1/` |

두 캐시는 독립적으로 동작한다. CC는 설정 단계를 스킵하고, Build Cache는 동일 입력에 대한 태스크 실행을 스킵한다.

### CC 캐시 초기화 (CI)

CI에서 CC 관련 문제 발생 시, 호스트의 캐시 디렉토리를 삭제한다.

```bash
# Jenkins 호스트에서 CC만 삭제
rm -rf /var/lib/jenkins/.gradle/chaekpool-api/configuration-cache

# 전체 Gradle 캐시 초기화 (호스트에서)
rm -rf /var/lib/jenkins/.gradle/*
```

---

## 참고 자료

- [Gradle Configuration Cache](https://docs.gradle.org/current/userguide/configuration_cache.html)
- [Gradle Build Cache](https://docs.gradle.org/current/userguide/build_cache.html)
- [Gradle Best Practices for Tasks](https://docs.gradle.org/current/userguide/best_practices_tasks.html)
