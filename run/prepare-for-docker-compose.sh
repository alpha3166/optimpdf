#!/bin/sh

# Run this script once, before executing `docker-compose up` for the first time.

# Pass your UID:GID to docker-compose through the .env file.
echo UID=$(id -u) > .env
echo GID=$(id -g) >> .env
