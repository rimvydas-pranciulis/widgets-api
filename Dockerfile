FROM eclipse-temurin:21
EXPOSE 8080
COPY build/libs/widgets-api.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]