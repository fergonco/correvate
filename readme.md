# Zipper

Service to zip files received as multipart form-data.

## Approach

In order to support big files, we want to stream input files to a zip step and
stream the output of that step to the HTTP response, with minimal buffering.

To achieve that we cannot use the Spring MultipartFile[] approach (as just
knowing the number of sent files requires us to buffer the whole request). We
need to disable this approach (see `application.properties`) and use some
library that helps with parsing the multipart body (`apache commons file
upload`).

Additionally we need to remove the limits to the file and request size (see
`application.properties`).

Regarding design, in general I split domain logic from infrastructure logic
(http entry points) but in this case domain logic is so thin (if it's there at
all) that for simplicity I avoid introducing any abstraction.

## Build and run in local

The project build tool is Gradle, so it suffice with:

    ./gradlew build

This will build, run tests and generate a jar file in `./build/libs/` folder
that can be run as usual:

    java -jar build/libs/zipper.jar

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

# Testing with big files

It's possible to create big files with this command:

    fallocate -l 3G /tmp/big3.img

Then use the previous `curl` command to send it to the application and use `jvisualvm` to monitor memory usage.

# SDLC from a SaaS

All software development process starts with a vision, some requirements, a
roadmap. Some initial infrastructure and architecture should be planned (cloud
provider, clusters/serverless, supported zones, etc.), although in my
experience these decisions are revisited during the development of the project.

After this initial phase, I see software development process as short
iterations of these three steps:

* Requirements definition
* Implementation
* Deployment & feedback

1. Requirements definition

It consist on a dialog between product and engineering: Product defines what to
build. Engineering provides feedback as an estimation on the build costs.
Product may adjust the scope based on this feedback.

In the past I have worked with Linear and Jira in order to define the tasks to
implement during an iteration.

For estimations I have found "planning pocker sessions" useful. A estimation
distribution based on best and worst case task estimations has been useful in
the past in order to:

* Remove the pressure to put tight estimations (when giving the worst case)
* Communicating to product the uncertainty of an estimation

(See https://estigator.mozz.app/)

2. Implementation

There are several activities that I see taking place while the system is built and
that I consider part of the "implementation" phase:

* writing tests
* design/architectural decisions
* deployment

For testing I like to put the focus on covering functional requirements with
mocked external dependencies, which I guess puts me somewhat near TDD/BDD. I
have used Gitlab CI/CD and GCP Cloud Build.

For implementation I think it's important to build redundancies in the team
regarding the knowledge of the system being built. For this, occasional pair
programming works well but other async collaboration may be also effective.

Regarding deployment, I think developers need to be involved in deployment and
follow "you build it, you run it". Reduce the friction to deploy. Kubernetes
makes this really easy. In the past I have used Helm and Kustomize for the
configuration.

3. Support & feedback

After the work is deployed it should be evaluated and a new iteration starts.

Part of the feedback comes in the form of support. The system may malfunction
here and there and intervention from the developers may be necessary. It's
important to know how much effort support requires (or how much effort is left
for new functionalities).

In this stage we need to setup monitoring and alerting with tools like datadog,
the ELK stack, prometheus & grafana.

