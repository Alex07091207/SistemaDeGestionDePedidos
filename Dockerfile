# Etapa 1: Compilación del proyecto con Maven
FROM eclipse-temurin:24-jdk AS buildstage
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY pom.xml .
COPY src /app/src
RUN mvn clean package -DskipTests

# Etapa 2: Creación de la imagen ligera de ejecución
FROM eclipse-temurin:24-jdk
WORKDIR /app

# Crear el directorio donde se montará el volumen del EFS para evitar fallos de permisos
RUN mkdir -p /mnt/efs/guias && chmod 777 /mnt/efs/guias

# Copiar el archivo .jar compilado en la etapa anterior
COPY --from=buildstage /app/target/sistemadegestiondepedidos-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]