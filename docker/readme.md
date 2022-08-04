# How to run on local env
- JDK version: 1.8

## Build the artifacts
Build this project with JDK 1.8 to generate JAR file (``felix-testsend-manager*.jar``)
- Run command ``./gradlew clean build``

Make sure the JAR file is placed in the right directory: ``./build/libs``

## Start docker-compose
- Run command ``docker-compose up --build``

## Stop docker-compose
- Run command ``docker-compose down -v``