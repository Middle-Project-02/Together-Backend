# 1단계: Gradle 빌드 스테이지
FROM gradle:8.13-jdk21 AS build

WORKDIR /app

# 빌드 캐시 활용을 위해 먼저 Gradle 설정 파일 복사
COPY settings.gradle ./
COPY build.gradle ./
COPY gradle ./gradle

# 나머지 프로젝트 파일 복사
COPY src ./src

# Gradle 빌드 실행 (테스트 생략)
RUN gradle build -x test

# 2단계: 실행용 이미지
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# 빌드 결과물 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 앱 실행
CMD ["java", "-jar", "app.jar"]
