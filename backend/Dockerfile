FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Install gradle
RUN apk add --no-cache gradle

COPY build.gradle .
COPY settings.gradle .

# Download dependencies first (cache layer)
RUN gradle dependencies --no-daemon

COPY src src

RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -g 1000 -S bikangu && \
    adduser -u 1000 -S bikangu -G bikangu

COPY --from=builder /app/build/libs/*.jar app.jar

RUN chown -R bikangu:bikangu /app

USER bikangu

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]