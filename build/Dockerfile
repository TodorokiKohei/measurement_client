FROM gradle:7.4.2-jdk11 as builder
WORKDIR /clients
COPY clients/ .
RUN gradle build

FROM openjdk:11-jre-slim-bullseye as runner
WORKDIR /clients
RUN mkdir configs results
COPY --from=builder /clients/app/build/libs/app-all.jar .
# COPY --from=builder /clients/app/src/main/dist/configs ./configs/
ENTRYPOINT [ "java", "-jar", "app-all.jar"]