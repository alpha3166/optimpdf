FROM eclipse-temurin:11-alpine
ARG USERNAME=me
RUN addgroup -g 1000 $USERNAME && \
    adduser -u 1000 -G $USERNAME -D $USERNAME
RUN apk --update-cache add sudo && \
    echo "$USERNAME ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/$USERNAME
RUN apk add git less
RUN apk add maven
RUN apk add imagemagick
USER $USERNAME
