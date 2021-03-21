
# Usage

## Build and Run

    docker build -f Dockerfile -t optimpdf ../..
    docker run -it --rm -u $(id -u):$(id -g) -v $PWD:/work -w /work optimpdf some.pdf

or

    ./docker-compose-pre.sh    # for the first time only
    docker-compose run optimpdf some.pdf
