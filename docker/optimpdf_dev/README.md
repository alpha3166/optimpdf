# Usage

## Setup

Replace UID (`1000`) and GID (`1000`) in `Dockerfile` and `docker-compose.yml` with yours.

## With Docker

To build image:

    docker build -t optimpdf-dev .

To start shell:

    docker run -it --rm -u $(id -u):$(id -g) -v ~/.m2:/home/me/.m2 -v $PWD/../..:/optimpdf -w /optimpdf optimpdf-dev sh

## With Docker-Compose

To start shell:

    docker-compose run --rm ws sh
