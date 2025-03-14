FROM openjdk:17-jdk-slim
WORKDIR /chatApp
COPY target/*.jar chatApp.jar

ENV DB_HOST=192.168.86.128 \
    DB_PORT=5432 \
    DB_NAME=chatAppDB \
    DB_USERNAME=postgres \
    DB_PASSWORD=721215 \
    MONGO_HOST=192.168.86.128 \
    MONGO_PORT=27017 \
    MONGO_DBNAME=ChatApp

EXPOSE 8080
ENTRYPOINT ["java","-jar","chatApp.jar"]
