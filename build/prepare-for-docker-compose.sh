#!/bin/sh

# Run this script once, before executing `docker-compose up` for the first time.

# Prevent $HOME/.m2 be made by root:root.
# When you `docker run maven` with your UID:GID, $HOME/.m2 must be owned by you.
mkdir -p ~/.m2

# Pass your UID:GID to docker-compose through the .env file.
echo UID=$(id -u) > .env
echo GID=$(id -g) >> .env
