# Use a base image with Maven and Java
FROM maven:3.6.3-jdk-11

# Copy the project files to the container
COPY . /usr/src/seneca

# Set the working directory
WORKDIR /usr/src/seneca/seneca-src

# Build the project
RUN mvn clean compile assembly:single
RUN mv target/seneca-1.0-jar-with-dependencies.jar ../seneca.jar

# Switch back to root
WORKDIR /usr/src/seneca

# Run the application
ENTRYPOINT ["java", "-jar", "seneca.jar"]
