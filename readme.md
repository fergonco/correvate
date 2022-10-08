# Zipper

Service to zip files received as multipart form-data.

## Approach

In order to support big files, we want to stream input files to a zip process
and stream the output of that process to the HTTP response, with minimal
buffering.

To achieve that we cannot use the Spring MultipartFile[] approach (as just
knowing the number of sent files requires us to buffer the whole request). We
need to disable this approach (see `application.properties`) and use some
library that helps with parsing the multipart body (`apache commons file
upload`).

Additionally we need to remove the limits to the file and request size (see
`application.properties`).

Regarding design, in general I split domain logic from infrastructure logic
(http entry points) but in this case domain logic is so thin (if there is at
all) that for simplicity I avoid introducing any abstraction.

## Build and run in local

The project build tool is Gradle, so it suffice with:

    ./gradlew build

This will build, run tests and generate a jar file in `./build/libs/` folder
that can be run as usual:

    java -jar build/libs/zipper-1.0.0.jar

### docker

Create a docker image:

    docker build . -t zipper

Run the last docker image version:

    docker run -p 8080:8080 zipper

# Manual testing

Create a couple of files:

    cat > /tmp/a <<EOF
    hola
    mundo
    EOF

    cat > /tmp/b <<EOF
    hello
    world
    EOF

Start the docker container:

    docker run -p 8080:8080 zipper

Hit the endpoint and uncompress the result:

    curl --fail --trace-ascii - http://localhost:8080/zip -F 'files=@/tmp/a' -F 'files=@/tmp/b' -o /tmp/output.zip && unzip /tmp/output.zip && echo ok && rm a b

