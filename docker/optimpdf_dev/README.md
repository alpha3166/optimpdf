# Usage

## Setup

Replace UID (`1000`) and GID (`1000`) in `Dockerfile` and `docker-compose.yml` to the ones you are using.

## To test

    docker-compose run --rm ws mvn test

or

    docker build -t optimpdf:dev .
    docker run -it --rm -u $(id -u):$(id -g) -v ~/.m2:/home/me/.m2 -v $PWD/../..:/workspace -w /workspace optimpdf:dev mvn test

## To build

    docker-compose run --rm ws mvn package

or

    docker build -t optimpdf:dev .
    docker run -it --rm -u $(id -u):$(id -g) -v ~/.m2:/home/me/.m2 -v $PWD/../..:/workspace -w /workspace optimpdf:dev mvn package
