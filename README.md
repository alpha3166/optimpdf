# OptimPDF

ScanSnap PDF optimizer for handheld devices

## What is OptimPDF

OptimPDF is a simple small tool to reduce the PDF file size, optimizing the inside JPEG width and height to fit handheld devices such as iPad or Kindle. Since I am using Fujitsu's ScanSnap document scanner, the tool may NOT be able to handle any type of PDFs but is limited to the ones made by ScanSnap Manager (I'm not sure because I haven't tried any other ones).

## Quick Start

1. Install Java (11 or later) and ImageMagick (6 or later).

2. Download `optimpdf-1.0.2-jar-with-dependencies.jar` from [Release page](https://github.com/alpha3166/optimpdf/releases).

3. Execute the JAR from the command-line, specifying target PDFs as arguments.

       java -jar optimpdf-1.0.2-jar-with-dependencies.jar some.pdf

   The sample above will create the reduced `some_r.pdf` in the same directory of the source PDF. If the argument is a directory, the tool will process all `*.pdf`s under the directory.

## Command-Line Options

    usage: java -jar OPTIMPDF_JAR [OPTION]... PDF_OR_DIR...
    Optimizes PDFs for handheld devices
     -b <arg>   bleach specified pages (eg: 1,3-5 or all)
     -d <arg>   output directory
     -f         overwrite output file
     -h         display this help and exit
     -l         display the list of PDFs to be processed and exit
     -n         dry-run (skip saving new PDFs)
     -o <arg>   output file name (for single input only). disable -d
     -p <arg>   process specified pages only (eg: 1,3-5)
     -Q <arg>   JPEG quality (Default: 50)
     -q         suppress displaying info of each page
     -s <arg>   output file suffix (Default: _r)
     -t <arg>   number of threads to use (Default: Number of CPU cores)
     -u         process only when the source PDF is newer than the destination
                PDF or when the destination PDF is missing. enable -f
     -w <arg>   double-page size threshold. halve output screen size if source
                JPEG is smaller than this (Default: 2539)
     -x <arg>   output screen size (Default: 1536x2048)

## How to build OptimPDF

Install Git, Java (11 or later), Maven, and ImageMagick (6 or later), then clone & build.

    git clone https://github.com/alpha3166/optimpdf
    cd optimpdf
    mvn package

## How to use OptimPDF with Docker

See the [README.md](docker/optimpdf_run/README.md) in [docker/optimpdf_run](docker/optimpdf_run) directory.
