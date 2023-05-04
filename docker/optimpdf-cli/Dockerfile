FROM eclipse-temurin:11-alpine as builder
RUN apk --update-cache add maven imagemagick
COPY . /optimpdf
WORKDIR /optimpdf
RUN mvn package

FROM eclipse-temurin:11-jre-alpine as runner
ARG USERNAME=me
RUN addgroup -g 1000 $USERNAME && \
    adduser -u 1000 -G $USERNAME -D $USERNAME && \
    apk --update-cache --no-cache add imagemagick
COPY --from=builder /optimpdf/target/optimpdf-*-jar-with-dependencies.jar /optimpdf.jar
ENTRYPOINT ["java", "-jar", "/optimpdf.jar"]
USER $USERNAME
