FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY src/ src/

RUN javac src/compiler/*.java

EXPOSE 8080

CMD ["java", "-cp", "src", "compiler.Main", "--server", "8080"]