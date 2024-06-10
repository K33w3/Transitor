#Keep in mind to change the DB connector to env variables and the entrypoint to the correct class
FROM openjdk:17-alpine
LABEL authors="andrei"

RUN apk --no-cache add git maven bash mysql-client
WORKDIR /app
COPY . .
RUN mvn clean install
ADD https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.29/mysql-connector-java-8.0.29.jar /app/mysql-connector-java-8.0.29.jar
ENV _JAVA_OPTIONS="-Djava.awt.headless=true"
ENTRYPOINT ["sh", "-c", "echo 'DB_HOST: $DB_HOST'; echo 'DB_PORT: $DB_PORT'; while ! nc -z $DB_HOST $DB_PORT; do sleep 1; done; java -cp /app/mysql-connector-java-8.0.29.jar:target/phase2-1.0-SNAPSHOT.jar com.bcs05.Main"]
