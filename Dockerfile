FROM amazoncorretto:21-alpine-jdk

COPY build/libs/transaction-storage-0.0.26.jar transaction-storage-0.0.26.jar

ENTRYPOINT ["java", "-jar", "transaction-storage-0.0.26.jar"]