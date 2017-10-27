FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/love_pop.jar /love_pop/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/love_pop/app.jar"]
