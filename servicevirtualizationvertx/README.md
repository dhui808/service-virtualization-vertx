# Service Virtaulization with Vert.x

Just like [Service Virtaulization with Servlet](https://github.com/dhui808/service-virtualization-servlet) and 
[Service Virtaulization with Node.js](https://github.com/dhui808/service-virtualization-nodejs), this applications serves as the
service virtaulization server, which supports the development and testing of modern single-page web applications. It can also be 
used to test Microservices that depend on other SOAP or REST services. 

## Dependency

[service virtualization UI application](https://github.com/dhui808/service-virtualization-ui)

## Build Docker image

mvn clean install

## Push the image to Docker Hub registry

mvn deploy

## Run Docker image

docker run -d -p 8080:8080 -p 5005:5005 -t dannyhui/servicevirtualizationvertx

## Run Service Virtualization UI

Open Web Server for Chrome

Port: 4200
Folder: points to the root deployment folder of [service virtualization UI application](https://github.com/dhui808/service-virtualization-ui).

## Start the browser

http://localhost:8080/banking


## Clean up

### List running Docker containers

docker ps

### Stop the running Docker container
docker container stop <container_id> 

### Delete all stopped Docker containers

docker rm $(docker ps -a -q)

### Delete all Docker images
 
docker rmi -f $(docker images -a -q)

