# Usa una imagen base con Java 17
FROM eclipse-temurin:17-jdk-jammy

# Establece el directorio de trabajo
WORKDIR /app

# Copia los archivos de Maven
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Descarga dependencias (para cachear en capas)
RUN ./mvnw dependency:go-offline

# Copia el código fuente
COPY src ./src

# Compila el proyecto y genera el JAR (omite tests para acelerar)
RUN ./mvnw clean package -DskipTests

# Copia el JAR generado al contenedor
COPY target/*.jar app.jar

# Expone el puerto
EXPOSE 8081

# Comando para ejecutar la app
CMD ["java", "-jar", "app.jar"]