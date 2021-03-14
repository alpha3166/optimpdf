# OptimPDF

ScanSnap PDF optimizer for handheld devices

## What is OptimPDF

OptimPDF is a simple small tool to reduce the PDF file size, optimizing the inside JPEG width and height to fit handheld devices such as iPad or Kindle. Since I am using Fujitsu's ScanSnap document scanner, the tool may NOT be able to handle any type of PDFs but is limited to the ones made by ScanSnap Manager (I'm not sure because I haven't tried any other ones).

## Quick Start

1. Install Java 11 or later and ImageMagick 7.

2. Download `optimpdf-1.0.0-jar-with-dependencies.jar` from [Release page](releases).

3. Execute the JAR from the command-line, specifying target PDFs as arguments.

       java -jar optimpdf-1.0.0-jar-with-dependencies.jar some.pdf

   The sample above will create the reduced `some_r.pdf` in the same directory of the source PDF. If the argument is a directory, the tool will process all `*.pdf`s under the directory.

## Command-Line Options

    usage: java -jar optimpdf-1.0.0-jar-with-dependencies.jar [OPTIONS] PDF...
     -b <arg>   bleach specified pages (eg: 1,3-5 or all)
     -d <arg>   output directory
     -f         overwrite output file
     -h         display this help and exit
     -l         display the list of PDFs to be processed and exit
     -n         dry-run (skip saving new PDFs)
     -o <arg>   output file name (for single input only)
     -p <arg>   process specified pages only (eg: 1,3-5)
     -Q <arg>   JPEG quality (Default: 50)
     -q         suppress displaying info of each page
     -s <arg>   output file suffix (Default: _r)
     -t <arg>   number of threads to use (Default: 8)
     -u         process only when the source PDF is newer than the destination
                PDF or when the destination PDF is missing. enable -f
     -w <arg>   double-page size threshold. halve output screen size if source
                JPEG is smaller than this (Default: 2539)
     -x <arg>   output screen size (Default: 1536x2048)

## How to build OptimPDF

Install Git and Maven, and just call `mvn`.

    git clone https://github.com/alpha3166/optimpdf
    cd optimpdf
    mvn package

With Docker, use this instead.

    git clone https://github.com/alpha3166/optimpdf
    cd optimpdf
    docker run -it --rm -u $(id -u):$(id -g) -v ~/.m2:/myhome/.m2 -v $PWD:/proj -w /proj -e MAVEN_CONFIG=/myhome/.m2 maven:3-adoptopenjdk-11 mvn -Duser.home=/myhome package

With Docker Compose, use this instead. `prepare-for-docker-compose.sh` is for the first time only.

    git clone https://github.com/alpha3166/optimpdf
    cd optimpdf/build
    ./prepare-for-docker-compose.sh
    docker-compose up

## How to run OptimPDF

Install Java 11 or later and ImageMagick 7, and run `java` using the all-in-one JAR.

    java -jar optimpdf-1.0.0-jar-with-dependencies.jar some.pdf

With Docker, use this instead.

    cd optimpdf/run
    docker build -t optimpdf .
    docker run -it --rm -u $(id -u):$(id -g) -v $PWD/../target:/mylib -v $PWD:/work -w /work optimpdf java -jar /mylib/optimpdf-1.0.0-jar-with-dependencies.jar some.pdf

With Docker Compose, put your source PDFs into `optimpdf/run/input` directory and do this. `prepare-for-docker-compose.sh` is for the first time only.

    cd optimpdf/run
    ./prepare-for-docker-compose.sh
    docker-compose up

Within the Docker container, the following command will be executed automatically.

    java -jar optimpdf-1.0.0-jar-with-dependencies.jar -d output -u input
