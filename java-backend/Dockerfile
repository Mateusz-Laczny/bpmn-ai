FROM openjdk:17-jdk-alpine
COPY target/llm2bpmn-0.0.1.jar llm2bpmn-0.0.1.jar
ENTRYPOINT ["java","-Dspring.profiles.active=paid-api", "-jar","/llm2bpmn-0.0.1.jar"]