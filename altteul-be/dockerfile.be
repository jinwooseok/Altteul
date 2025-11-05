# 그레들 버전, JDK 버전에 따라 다르게 수정
FROM bellsoft/liberica-openjdk-alpine:17 AS builder
WORKDIR /build

# 빌더 이미지에서 애플리케이션 빌드
COPY . /build
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# APP
FROM bellsoft/liberica-openjdk-alpine:17
WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul

RUN apk update \
    && apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone \
    && apk del tzdata

# 빌더 이미지에서 jar 파일만 복사
COPY --from=builder /build/build/libs/*-SNAPSHOT.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "-Dsun.net.inetaddr.ttl=0", "app.jar"]