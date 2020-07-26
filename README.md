# Service Virtualization for Mobile Web Banking with Vert.x
This application provides the service virtualization to test the modern SPA mobile web banking projects:
[mobileweb-angular-mvc-poc](https://github.com/dhui808/mobileweb-angular-mvc-poc)
and [mobileweb-angular-redux-poc](https://github.com/dhui808/mobileweb-angular-redux-poc).
This approach can also be used to test microservices that depend on other SOAP or REST services. 

Implemented with Eclipse Vert.x toolkit, this reactive application can be deployed as Docker 
microservice to any Cloud platform such as OpenShift. 

## Application configuration
There are two configuration files: application.properties and logback.xml, under resources
folder.

The application.properties looks like below:

webservicemockdata.home=/usr/github-vertx/webservicemockdata
server.servlet.context-path=/banking
logging.config=file:/usr/webservicemockserver-vertx-logging/logback.xml

The logback.xml must be copied to the location as specified by logging.config property in
application.properties.

The content of project webservicemockdata must be copied to the location as specified by 
webservicemockdata.home property in application.properties.

## Dependency

[service virtualization UI application](https://github.com/dhui808/service-virtualization-ui)

[service virtualization data](https://github.com/dhui808/service-virtualization-data)

## Start server from Eclipse in debug mode
Define Eclipse Debug Configuration with the following settings:
Main class:io.vertx.core.Starter
Arguments:run webservicemockserver.WebserviceVirtualServiceApplication

Right-click on WebserviceVirtualServiceApplication.java - Debug As - Debug Configuration...  
Select the Debug Configuration defined above - Click Debug button 

## Build
cd servicevirtualizationvertx

mvn clean install

## Build Docker image
mvn clean install -Pdocker

## Push the image to Docker Hub registry
mvn deploy -Pdocker

## Start server from command line - Windows
start.cmd

## Start server from command line - Unix/Linux
./start.sh

## Build Docker image

mvn clean install

## Push the image to Docker Hub registry

mvn deploy

## Run Docker image with Fabric8

mvn install -Pfabric8

## Run Docker image directly

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

### Delete all stopped Docker containers  (Git Bash)

docker rm $(docker ps -a -q)

### Delete all Docker images  (Git Bash)
 
docker rmi -f $(docker images -a -q)

