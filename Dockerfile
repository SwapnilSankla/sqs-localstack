FROM eclipse-temurin:17-jdk AS build
COPY . /src
WORKDIR /src
ENV DOCKER_HOST tcp://dind:2375
RUN ./gradlew --no-daemon clean build

FROM eclipse-temurin:17-jre
EXPOSE 8080
COPY --from=build /src/build/libs/sqs-1.0.jar /src/app/sqs-1.0.jar
ENTRYPOINT ["java", "-jar", "/src/app/sqs-1.0.jar"]