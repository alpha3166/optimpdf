FROM eclipse-temurin:11-alpine as builder
RUN apk -U add maven
RUN apk -U add imagemagick
COPY . /optimpdf
WORKDIR /optimpdf
RUN mvn package

FROM eclipse-temurin:11-jre-alpine as runner
RUN addgroup -g 1000 me && \
    adduser -u 1000 -G me -D me && \
    apk -U --no-cache add imagemagick
COPY --from=builder /optimpdf/target/optimpdf-*-jar-with-dependencies.jar /optimpdf.jar
ENTRYPOINT ["java", "-jar", "/optimpdf.jar"]
