# Etapa de build
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom y dependencias para cache
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# Copiar código fuente y compilar
COPY src ./src
RUN mvn -B -DskipTests package

# Etapa de ejecución
FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx1024m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
