# AgriNex backend image: spec §45 agrinex-backend (ADR-0021). The images are built on the deployment host. No
# secret enters any layer: configuration arrives only through the container environment at run time (ADR-0020
# item 4). Layered extraction follows the Spring Boot 4.1 "Efficient Container Images" reference.

FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/ src/
# The tests run before an image is built (backend: mvnw verify, with Testcontainers); the image build only packages.
RUN ./mvnw -B -q -DskipTests package \
    && cp target/agrinex-backend-*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:25-jre-noble
# curl serves only the container health check (ADR-0021 item 7).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system agrinex \
    && useradd --system --gid agrinex --no-create-home --shell /usr/sbin/nologin agrinex
WORKDIR /application
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./
USER agrinex
EXPOSE 8080
# The heap is sized from the container's memory limit; an OutOfMemoryError stops the JVM so the container restarts.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "-jar", "application.jar"]
