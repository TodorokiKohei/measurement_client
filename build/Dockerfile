FROM gradle:7.4.2-jdk11 as builder

# RUN mkdir /clients
WORKDIR /clients
COPY clients/* .
RUN gradle build

FROM openjdk:11-jre-slim-bullseye as runner
WORKDIR /clients
COPY /clients/app/