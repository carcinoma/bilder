FROM i386/openjdk:11-slim

COPY target/bilder-0.0.1-SNAPSHOT.jar /bilder-app.jar
COPY startup.sh /startup.sh

VOLUME /images
VOLUME /cache

CMD ["/startup.sh"]
