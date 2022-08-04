# Use OpenJDK 8
FROM artifactory.rakuten-it.com/felix-docker-release-local/jre-temurin-focal:8

# Expose port 8080 to Docker host
EXPOSE 8080

#Proxy for downloading packages
ENV http_proxy=http://pkg.proxy.prod.jp.local:10080
ENV https_proxy=http://pkg.proxy.prod.jp.local:10080

ARG TARGET_DIR=.
ARG JAR_FILE=${TARGET_DIR}/felix-testsend-manager*.jar

### Move application JAR file from Docker host to the container
VOLUME /tmp
ADD ${JAR_FILE} app.jar
RUN sh -c 'touch /app.jar' # Update timestamp
###

### Move entrypoint Shell file from Docker host to the container
ADD docker/wrapper.sh /usr/local/bin/wrapper.sh
RUN chmod a+x /usr/local/bin/wrapper.sh
###

#unset proxies before entrypoint
ENV http_proxy=
ENV https_proxy=
ENTRYPOINT ["/bin/bash", "/usr/local/bin/wrapper.sh"]