FROM eclipse-temurin:21-jre
WORKDIR /app

COPY antonio_mesa_gravimetrica/target/*.jar app.jar

# Cambiado al puerto 443 según tu application.properties (SSL)
EXPOSE 443

ENTRYPOINT ["java", "-jar", "app.jar"]