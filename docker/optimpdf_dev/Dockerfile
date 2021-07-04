FROM adoptopenjdk/openjdk11:alpine
RUN apk -U add git maven imagemagick
RUN apk -U add sudo && \
    echo "%wheel ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/wheel && \
    addgroup -g 1000 me && \
    adduser -u 1000 -G me -D me && \
    addgroup me wheel
