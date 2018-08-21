# Image provides a container that runs alfresco-bm-rest-api to create scenarios on Alfresco Enterprise Content Services.

 # Fetch image based on Java 8
 FROM alfresco/alfresco-base-java:8

 COPY target/alfresco-bm-rest-api-${docker.project_version}.jar /usr/bin
 RUN ln /usr/bin/alfresco-bm-rest-api-${docker.project_version}.jar /usr/bin//alfresco-bm-rest-api.jar

 ENV JAVA_OPTS=""
 ENTRYPOINT java $JAVA_OPTS -jar /usr/bin/alfresco-bm-rest-api.jar