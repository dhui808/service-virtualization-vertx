###
# vert.x docker example using a Java verticle packaged as a fat jar
# To build:
#  docker build -t yourdockerid/webservicemockserver-vertx .
# To run:
#  docker run -t -i -p 8080:8080 yourdockerid/webservicemockserver-vertx
###

FROM java:8-jre

ENV VERTICLE_FILE webservicemockserver-0.0.1-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

EXPOSE 8080

# Copy your fat jar to the container
COPY target/$VERTICLE_FILE $VERTICLE_HOME/

# Copy webservicemockdata and logback.xml
COPY target/webservicemockdata /usr/github-vertx/webservicemockdata
COPY target/classes/logback.xml /usr/webservicemockserver-vertx-logging/logback.xml
RUN mkdir /usr/webservicemockserver-vertx-logging/logs

RUN groupadd --gid 1000 appusergroup  && useradd --uid 1000 --gid appusergroup --shell /bin/bash --create-home appuser
RUN chown -R appuser:appusergroup /usr/github-vertx/webservicemockdata
RUN chmod -R 755 /usr/github-vertx/webservicemockdata
RUN chown -R appuser:appusergroup /usr/webservicemockserver-vertx-logging
RUN chmod -R 777 /usr/webservicemockserver-vertx-logging
USER appuser

# Launch the verticle
WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $VERTICLE_FILE"]