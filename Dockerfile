FROM maven:3.8.4-openjdk-17-slim AS build

COPY src /usr/src/app/src
COPY pom.xml /usr/src/app

WORKDIR /usr/src/app
RUN mvn clean package

FROM eclipse-temurin:17-jre-jammy

COPY --from=build /usr/src/app/target/FTPMaven-1.0-SNAPSHOT.jar /usr/app/ftp-server.jar

EXPOSE 21

ENTRYPOINT ["java", "-jar", "/usr/app/ftp-server.jar"]
