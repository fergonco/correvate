# Zipper

Service to zip files received as multipart form-data.

## Build and run in local

The project build tool is Gradle, so it suffice with:

    ./gradlew build

This will build, run tests and generate a jar file in `./build/libs/` folder
that can be run as usual:

    java -jar build/libs/zipper-1.0.0.jar

No configuration or external dependencies are required.

### docker

Create a docker image:

    version=$(./gradlew properties | grep version: | sed 's/^.\{9\}//') && docker build . -t zipper:$version

Note that this is just a call to `docker build` but syncing the docker tag with the gradle project version. For local
testing it may suffice to use:

    docker build . -t zipper

Run the last docker image version:

    docker run -p 8080:8080 zipper:1.0.0

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

    curl --fail --trace-ascii - http://localhost:8080/zip -F 'files=@/tmp/a' -F 'files=@/tmp/b' -o /tmp/output.zip && unzip /tmp/output.zip && echo ok

