
# Usage

## Do mvn test

    docker build -f Dockerfile.im6 -t optimpdf:mvn-im6 .
    docker run -it --rm -u $(id -u):$(id -g) -v ~/.m2:/myhome/.m2 -v $PWD/../../optimpdf:/proj -w /proj -e MAVEN_CONFIG=/myhome/.m2 optimpdf:mvn-im6 mvn -Duser.home=/myhome test

    docker build -f Dockerfile.im7 -t optimpdf:mvn-im7 .
    docker run -it --rm -u $(id -u):$(id -g) -v ~/.m2:/myhome/.m2 -v $PWD/../../optimpdf:/proj -w /proj -e MAVEN_CONFIG=/myhome/.m2 optimpdf:mvn-im7 mvn -Duser.home=/myhome test

or

    ./docker-compose-pre.sh    # for the first time only
    docker-compose up
