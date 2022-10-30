# OptimPDF

OptimPDF is a tool to optimize image-based PDFs, where each page consists of a single image, for handheld devices. It also can convert ZIPed image files to PDF.

## Subcommands

The following subcommands are accepted.

### `reduce`

- Shrink an image in a PDF so that the number of pixels in the PDF fits exactly into the specified size.
- Images that are originally smaller than the specified size will keep their original size and will not be enlarged.
- Portrait images will be reduced to the exact number of pixels when the device is placed vertically, and landscape images will be reduced to the exact number of pixels when the device is placed horizontally.
- Images smaller than the specified threshold will be scaled down to fit as a double-page view on the device.
- It also can lighten the color of paper burn. However, this may cause the light shading colors to disappear as well.

### `rotate`

- Aligns each page of a PDF to the specified angle (0, 90, 180, or 270) of rotation.
- You can also specify a reference PDF and align the direction of rotation of each page of that PDF. This is useful, for example, to restore pages that have been rotated by Adobe Acrobat's OCR.

### `unzip`

- Extracts only the image files from the ZIP file and creates a PDF with a single page consisting of a single image.
- You can also specify that the PDF should be right-open.
- The output is sorted by path in the ZIP.

## How to start

1. Install Java (11 or later) and ImageMagick (6 or later).

2. Download `optimpdf-2.0.0-jar-with-dependencies.jar` from the [Release page](https://github.com/alpha3166/optimpdf/releases).

3. Execute the JAR from the command line, specifying the subcommand and target file(s) as arguments.

    java -jar optimpdf-2.0.0-jar-with-dependencies.jar reduce some.pdf

## Specifying input and output

### Specifying input files

- Specify an input file to be processed by a command line argument.
- You can specify multiple files to be processed.
- With the `-r`, `--recursive` option, if the target is a directory, files in the directory are processed recursively.

### Specifying output files

- Without any options, it will try to output the result of processing to the same path as the input file and will fail to overwrite it.
- With the `-f`, `--force` option, the output file will be overwritten even if it already exists.
- The `-o`, `--outfile` option allows you to specify the name of the output file. It can be used only when there is only one input file.
- The `-s`, `--suffix` option can be used to specify a suffix to append to the input filename.
- The `-d`, `--outdir` option can be used to specify a directory to output files to.
- The `-u`, `--update` option allows you to process only those files whose output file does not exist or whose input file is newer than the output file.

## Command-Line Options

### Top-level Options

Option|Description
-|-
-h, --help|Show help message and exit.
-V, --version|Print version information and exit.

### Options common to all subcommands

Option|Description
-|-
-d, --outdir=_dir_|Write destination files to _dir_.
-f, --force|Overwrite destination files.
-h, --help|Show help message and exit.
-l, --list|Show the list of files to be processed and exit.
-n, --dry-run|Dry run (skip saving destination files).
-o, --outfile=_file_|Write output to _file_. For single input only.
-q, --quiet|Suppress displaying info on each page.
-r, --recursive|Process recursively.
-s, --suffix=_suffix_|Use source file name + _suffix_ as destination file name.
-u, --update|Process only when the source is newer than the destination or when the destination is missing.

### Options for `reduce` subcommand

Option|Description
-|-
-B, --bleach-pages=_pages_|Bleach specified pages. (eg: 1,3-5 or all)
-P, --pages=_pages_|Process specified pages only. (eg: 1,3-5)
-Q, --jpeg-quality=_jpegQuality_|Set JPEG quality. (Default: 50)
-S, --screen-size=_screenSize_|Set output screen size. (Default: 1536x2048)
-T, --number-of-threads=_numberOfThreads_|Set number of threads to use. (Default: 8)
-W, --double-page-threshold=_doublePageThreshold_|Set double-page size threshold. Halve output screen size if the source JPEG is smaller than this. (Default: 2539)

### Options for `rotate` subcommand

Option|Description
-|-
-D, --degree=_degree_|Set all page's rotation to the _degree_. Must be one of 0, 90, 180, or 270. (Default: 0)
-R, --ref-pdf=_pdf_|Match the rotation of each page to the referenced _pdf_ file.

### Options for `unzip` subcommand

Option|Description
-|-
-R, --right-to-left|Set direction to right-to-left.

## How to build

Install Git, Java (11 or later), Maven, and ImageMagick (6 or later), then clone & build.

    git clone https://github.com/alpha3166/optimpdf
    cd optimpdf
    mvn package
