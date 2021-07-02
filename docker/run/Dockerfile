FROM adoptopenjdk/openjdk11:alpine
RUN apk -U --no-cache add imagemagick && \
    addgroup -g 1000 me && \
    adduser -u 1000 -G me -D me
COPY target/optimpdf-*-jar-with-dependencies.jar /optimpdf.jar
ENTRYPOINT ["java", "-jar", "/optimpdf.jar"]
