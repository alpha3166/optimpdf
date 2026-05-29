# Usage

## Setup

Put target PDF files in this directory.

## With Podman/Docker

To build image:

    podman build -f Containerfile -t optimpdf-cli ../..

To run:

    podman run -it --rm -v $PWD:/optimpdf -w /optimpdf optimpdf-cli some.pdf

## With Podman/Docker Compose

To run:

    podman compose run --rm cli some.pdf
