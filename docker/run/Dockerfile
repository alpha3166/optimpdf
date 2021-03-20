FROM adoptopenjdk/openjdk11:alpine as builder
RUN apk -U --no-cache add maven imagemagick
COPY optimpdf /optimpdf
WORKDIR /optimpdf
RUN mvn package

FROM adoptopenjdk/openjdk11:alpine
RUN apk -U --no-cache add imagemagick
COPY --from=builder /optimpdf/target/optimpdf-*-jar-with-dependencies.jar /optimpdf.jar
ENTRYPOINT ["java", "-jar", "/optimpdf.jar"]
