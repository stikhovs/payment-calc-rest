FROM openjdk:17

ARG JAR=build/libs/*SNAPSHOT.jar
ARG EXPOSE=8080

ENV JAR=${JAR}
ENV EXPOSE=${EXPOSE}

RUN gradle clean bootJar

COPY ${JAR} /app.jar

CMD exec java $JAVA_OPTS -jar /app.jar

EXPOSE ${EXPOSE}
