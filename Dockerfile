FROM maven:3.8.3-openjdk-17 as build
COPY . .
RUN mvn clean package -DskipTests

FROM maven:3.8.3-openjdk-17
COPY --from=build /target/SpringBootSaml-0.0.1-SNAPSHOT.jar SpringBootSaml.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","SpringBootSaml.jar"]

