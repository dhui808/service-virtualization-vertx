# Service Virtualization for Mobile Web Banking with Vert.x
This application provides the service virtualization to test the mobile web banking projects:
[mobileweb-angular-mvc-poc](https://github.com/dhui808/mobileweb-angular-mvc-poc)
and [mobileweb-angular-redux-poc](https://github.com/dhui808/mobileweb-angular-redux-poc).

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

## Build the application from command line
mvn clean package

## Start server from Eclipse in debug mode
Define Eclipse Debug Configuration with the following settings:
Main class:io.vertx.core.Starter
Arguments:run webservicemockserver.WebserviceVirtualServiceApplication

Right-click on WebserviceVirtualServiceApplication.java - Debug As - Debug Configuration...  
Select the Debug Configuration defined above - Click Debug button 

## Start server from command line - Windows
start.cmd

## Start server from command line - Unix/Linux
./start.sh

## Build Docker image
docker build -t yourdockerid/webservicemockserver-vertx .

## Push Docker image 
docker push docker.io/yourdockerid/webservicemockserver-vertx

## Run Docker image 
docker run -t -i -p 8080:8080 yourdockerid/webservicemockserver-vertx

