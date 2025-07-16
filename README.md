# 🧠 Thinkeep

**Thinkeep**는 일기 작성과 AI 기반 회상 퀴즈를 통해 기억을 더 오래 보존할 수 있도록 도와주는 Spring Boot 애플리케이션입니다.

## 📋 목차
- [프로젝트 개요](#-프로젝트-개요)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [시스템 아키텍처](#️-시스템-아키텍처)
- [시작하기](#-시작하기)
- [API 문서](#-api-문서)
- [환경 설정](#-환경-설정)
- [배포](#-배포)
- [모니터링](#-모니터링)

## 🎯 프로젝트 개요

Thinkeep은 다음과 같은 핵심 아이디어를 바탕으로 개발되었습니다:

1. **일기 작성**: 매일 4가지 질문(감정, 만난 사람, 먹은 음식, 기억에 남는 일)에 답하며 하루를 기록
2. **AI 회상 퀴즈**: GPT를 활용해 과거 일기 내용을 바탕으로 퀴즈를 생성하여 기억을 강화
3. **스트릭 시스템**: 연속 작성일수를 추적하고 뱃지를 통해 동기부여 제공
4. **감정 트래킹**: 월별 감정 변화를 시각화하여 자기 분석 도구 제공

## ✨ 주요 기능

### 📝 일기 관리
- **매일 일기 작성**: 구조화된 4가지 질문을 통한 일기 작성
- **감정 기록**: 하루의 감정 상태를 함께 기록
- **일기 목록 조회**: 과거 작성한 모든 일기를 날짜순으로 조회
- **수정/삭제**: 작성한 일기의 편집 및 삭제 기능

### 🧩 AI 회상 퀴즈
- **자동 퀴즈 생성**: OpenAI GPT를 활용한 개인화된 회상 퀴즈
- **3지선다 형식**: 과거 일기 내용을 바탕으로 한 객관식 문제
- **스킵 기능**: 하루 최대 2회까지 퀴즈 건너뛰기 가능
- **오답 재시도**: 틀린 문제나 건너뛴 문제를 다시 풀 수 있는 기능

### 🏆 게이미피케이션
- **스트릭 카운트**: 연속 일기 작성일수 추적
- **뱃지 시스템**: 3일, 7일, 14일, 30일 연속 작성 시 뱃지 획득
- **진행률 추적**: 일기 완성도 및 퀴즈 정답률 확인

### 👤 사용자 관리
- **일반 회원가입**: 닉네임/비밀번호 기반 회원가입
- **카카오 로그인**: 카카오 계정을 통한 간편 로그인
- **JWT 인증**: 토큰 기반 사용자 인증 (개발 시 토글 가능)

### 📊 데이터 분석
- **월별 감정 분석**: 특정 월의 감정 변화 패턴 분석
- **통계 제공**: 전체 기록 수, 감정별 분포 등

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Database**: MySQL 8.0 (AWS RDS)
- **ORM**: Spring Data JPA + Hibernate
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle 8.14.2

### External APIs
- **AI**: OpenAI GPT-4o-mini (퀴즈 생성)
- **Social Login**: Kakao OAuth2

### Infrastructure
- **Cloud**: AWS EC2
- **Containerization**: Docker + Docker Compose
- **Monitoring**: Prometheus + Grafana
- **CI/CD**: GitHub Actions

### Documentation & Testing
- **API Docs**: Swagger/OpenAPI 3.0
- **Health Check**: Spring Boot Actuator
- **Testing**: JUnit 5

## 🏗️ 시스템 아키텍처
![KakaoTalk_20250713_052757170](https://github.com/user-attachments/assets/c46dc006-8752-49ce-a753-7e1eb2053de9)



### 주요 컴포넌트
- **Controller Layer**: REST API 엔드포인트 제공
- **Service Layer**: 비즈니스 로직 처리
- **Repository Layer**: 데이터 접근 및 영속성 관리
- **Security Layer**: JWT 인증 및 권한 관리
- **External Integration**: OpenAI API 연동

## 🚀 시작하기

### 필수 요구사항
- Java 17+
- MySQL 8.0+
- Docker & Docker Compose (선택사항)

### 로컬 실행

1. **저장소 클론**
```bash
git clone https://github.com/your-username/thinkeep.git
cd thinkeep
```

2. **환경 변수 설정**
```bash
# .env 파일 생성
cp .env.example .env

# 필수 환경 변수 설정
DB_URL=jdbc:mysql://localhost:3306/thinkeep
DB_USERNAME=your_username
DB_PASSWORD=your_password
OPENAI_API_KEY=your_openai_api_key
JWT_SECRET_KEY=your_jwt_secret_key
```

3. **데이터베이스 생성**
```sql
CREATE DATABASE thinkeep CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

4. **애플리케이션 실행**
```bash
# Gradle 빌드 및 실행
./gradlew bootRun

# 또는 JAR 파일 빌드 후 실행
./gradlew bootJar
java -jar build/libs/thinkeep-0.0.1-SNAPSHOT.jar
```

### Docker를 사용한 실행

```bash
# Docker Compose로 전체 스택 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f thinkeep-app
```

## 📚 API 문서

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### 주요 API 엔드포인트

#### 인증 (Authentication)
```http
POST /api/auth/login          # 일반 로그인
POST /api/auth/kakao-login    # 카카오 로그인
POST /api/auth/logout         # 로그아웃
GET  /api/auth/me            # 현재 사용자 정보
```

#### 사용자 (Users)
```http
POST /api/users              # 회원가입
GET  /api/users/{userNo}     # 사용자 조회
PUT  /api/users/{userNo}     # 사용자 정보 수정
DELETE /api/users/{userNo}   # 사용자 삭제
```

#### 일기 (Records)
```http
POST /api/records            # 오늘 일기 작성
GET  /api/records/today      # 오늘 기록 상태 조회
GET  /api/records/all        # 모든 일기 조회
GET  /api/records/{date}     # 특정 날짜 일기 조회
PUT  /api/records/{recordId} # 일기 수정
DELETE /api/records/{recordId} # 일기 삭제
```

#### 퀴즈 (Quizzes)
```http
GET  /api/quizzes/today           # 오늘의 퀴즈 생성
POST /api/quizzes/submit          # 퀴즈 정답 제출
GET  /api/quizzes/today/wrong     # 오답 퀴즈 조회
GET  /api/quizzes/today/result    # 퀴즈 결과 요약
```

#### 뱃지 (Badges)
```http
GET  /api/badges             # 모든 뱃지 조회
POST /api/badges             # 뱃지 생성
PUT  /api/badges/{badgeId}   # 뱃지 수정
DELETE /api/badges/{badgeId} # 뱃지 삭제
```

## ⚙️ 환경 설정

### JWT 토글 설정
개발 시 JWT 인증을 비활성화할 수 있습니다:

```properties
# application.properties
app.security.jwt-enabled=false  # JWT 비활성화 (개발용)
app.security.jwt-enabled=true   # JWT 활성화 (운영용)
```

### OpenAI API 설정
퀴즈 생성을 위한 OpenAI API 키 설정:

```properties
openai.api.key=${OPENAI_API_KEY}
```

### 데이터베이스 설정
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

## 🚀 배포

### AWS EC2 배포

1. **GitHub Actions 워크플로우**
   - `main` 브랜치에 push 시 자동 배포
   - JAR 파일 빌드 및 Docker 이미지 생성
   - AWS EC2에 배포

2. **필요한 GitHub Secrets**
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
EC2_INSTANCE_ID
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
OPENAI_API_KEY
GRAFANA_PASSWORD
```

3. **배포 과정**
```bash
# 자동 배포 프로세스
1. 코드 빌드 (Gradle)
2. Docker 이미지 생성
3. AWS S3에 배포 패키지 업로드
4. EC2에서 Docker Compose 실행
```

## 📊 모니터링

### Prometheus + Grafana
- **Prometheus**: http://your-domain:9090
- **Grafana**: http://your-domain:3000
- **Health Check**: http://your-domain:8080/actuator/health
