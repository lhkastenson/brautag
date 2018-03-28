FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/brautag.jar /brautag/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/brautag/app.jar"]
