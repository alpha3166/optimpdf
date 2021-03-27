FROM adoptopenjdk/openjdk11:alpine
RUN apk -U --no-cache add git maven imagemagick
RUN apk -U --no-cache add sudo && \
    echo "%wheel ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/wheel && \
    addgroup -g 1000 vscode && \
    adduser -u 1000 -G vscode -D vscode && \
    addgroup vscode wheel
