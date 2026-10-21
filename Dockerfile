FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/shopee-project-1.0.0.jar app.jar
EXPOSE 8080 8088
ENTRYPOINT ["java","-jar","/app/app.jar"]