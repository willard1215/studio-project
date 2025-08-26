# -----------------------------
# 1) Build stage
# -----------------------------
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /workspace

# Gradle 캐시 최적화: 먼저 메타 파일만 복사
COPY build.gradle settings.gradle ./
COPY gradle gradle
# (gradle wrapper를 쓰지 않고 gradle 이미지의 gradle을 사용)
RUN gradle --version

# 소스 복사 후 빌드
COPY src src
# 테스트는 배포 파이프라인에서 이미 돌린다는 가정으로 제외
RUN gradle clean bootJar -x test --no-daemon

# -----------------------------
# 2) Runtime stage
# -----------------------------
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

# 보안상 비루트 계정 사용
RUN useradd -ms /bin/bash spring
USER spring

# 빌드 산출물 복사 (Spring Boot fat JAR)
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

# 컨테이너 런타임 튜닝 (필요 시 조정)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseStringDeduplication -Dfile.encoding=UTF-8"
# Spring 프로파일/DB/JWT 등은 환경변수로 주입
# 예) docker run ... -e SPRING_PROFILES_ACTIVE=prod -e SPRING_DATASOURCE_URL=... -e JWT_SECRET=...
ENV SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"

EXPOSE 8080

# 단순 헬스체크 (필요 시 /actuator/health 사용)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/api/auth/health || exit 1

ENTRYPOINT ["/bin/bash","-lc","java $JAVA_OPTS -jar /app/app.jar"]
