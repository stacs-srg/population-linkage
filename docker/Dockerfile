# Use Maven to run
FROM maven:3.9.9-eclipse-temurin-21-jammy

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml, source code and entrypoint
COPY pom.xml .
COPY src ./src
COPY docker/port_forwarding.sh .

# Install dependencies
RUN mvn clean install

# Install socat for traffic forwarding
RUN apt update && apt install -y socat

RUN chmod +x ./*.sh ./src/main/scripts/**/*.sh