# Population_linkage with Docker
The `population_linkage` library can be used and deployed in a portable containerised format. The following guide details how to build and run the image.

## 1. Prerequisites
The following tools must be installed on your system to follow this guide:
- [Git](https://git-scm.com/)
- [Docker](https://www.docker.com/), [Podman](https://podman.io/) or any other container management tool
- A Neo4J container hosting the population data

## 2. Installing and building
### 2.1. Installing the repository
You will need the `population_linkage` repository installed on your system to build the image. To install this, run the following command:

```sh
# In a terminal (Windows/macOS/Linux)
git clone https://github.com/stacs-srg/population-linkage.git 
```

### 2.2. Building the images
As this repository is large and offer a number of use-cases and entrypoints, the Docker setup consists of a base image and then implementations of this base image for different entrypoints.

To build the base image, run the following command from the root of the repository:

```sh
# In a terminal (Windows/macOS/Linux)
docker build . -f docker/Dockerfile -t population-linkage-base:latest
```

For this guide, we will be using the *runall* image (for the `src/main/scripts/endtoend/runall.sh` entrypoint). To build this, we then run the following command:

```sh
# In a terminal (Windows/macOS/Linux)
docker build . -f docker/runall/Dockerfile -t population-linkage-runall:latest
```

## 3. Running
### 3.1. Creating a Docker Network
The `population_linkage` Docker setup is designed for use with a seperate Neo4J database container (e.g the [umea-neo4j-wrapper repository](https://github.com/jamesross03/umea-neo4j-wrapper)).

To interact with a second container, you will need to setup a Docker network. To do this, run the following command:

```sh
# In a terminal (Windows/macOS/Linux)
docker network create umea-neo4j-net
```

Where `umea-neo4j-net` is the name of the Docker network.

To connect to this with another container, simply add the following flag when running your second container:

```sh
# In a terminal (Windows/macOS/Linux)
--network umea-neo4j-net
```

### 3.2. Running the Docker image
To run the image created above, use the following command:

```sh
# In a terminal (Windows/macOS/Linux)
docker run --network umea-neo4j-net population-linkage-runall:latest
```

Note: By default the container attempts to connect with a Neo4J database hosted by a container named `umea-neo4j`. If this is not the case, [see 3.3](#33-setting-hostname) for details on overriding this. 

### 3.3. Setting hostname
The hostname of the Neo4J database container can be configured at runtime by using environment variables. To do this simply add the following flag to the run command (replacing `<HOSTNAME>` with the name of your container).

```sh
# In a terminal (Windows/macOS/Linux)
--e NEO4J_HOST=<HOSTNAME>
```
