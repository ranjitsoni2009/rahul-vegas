FROM openjdk:17-jdk-alpine
EXPOSE 8080
MAINTAINER worldfamilyenglish

COPY applicationinsights.json lib/applicationinsights.json
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.2.8/applicationinsights-agent-3.2.8.jar lib/applicationinsights-agent-3.2.8.jar

COPY /build/libs/vegas-1.0.0.jar vegas-1.0.0.jar
ENTRYPOINT ["java", "-javaagent:lib/applicationinsights-agent-3.2.8.jar", "-jar", "vegas-1.0.0.jar"]
