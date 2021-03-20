
# Usage

## Do mvn test

    docker build -f Dockerfile -t optimpdf:mvn .
    docker run -it --rm -u $(id -u):$(id -g) -v ~/.m2:/myhome/.m2 -v $PWD/../../optimpdf:/proj -w /proj -e MAVEN_CONFIG=/myhome/.m2 optimpdf:mvn mvn -Duser.home=/myhome test

or

    ./docker-compose-pre.sh
    docker-compose up
