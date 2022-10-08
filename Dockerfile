FROM gradle:7.5-jdk-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM gcr.io/distroless/java11-debian11
COPY --from=build /home/gradle/src/build/libs/zipper.jar /app/
WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "zipper.jar"]

