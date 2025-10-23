FROM ubuntu:focal
#SHELL ["/bin/bash", "-c"]
RUN apt-get update
RUN apt-get install -y openjdk-17-jdk
RUN apt-get install -y wget
RUN apt-get install -y net-tools iproute2 iputils-ping

##Installing maven 3.8.9
RUN wget https://dlcdn.apache.org/maven/maven-3/3.8.9/binaries/apache-maven-3.8.9-bin.tar.gz -P /tmp
RUN tar xf /tmp/apache-maven-*.tar.gz -C /opt
RUN ln -s /opt/apache-maven-3.8.9 /opt/maven
RUN touch /etc/profile.d/maven.sh
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=/opt/apache-maven-3.8.9/bin:$PATH

##Installing python/python packages
RUN apt-get install python3 -y
RUN apt-get install python3-pip -y
RUN pip install opencv-python


WORKDIR /app
COPY pom.xml /app
ADD . /app
RUN mvn clean
RUN mvn install:install-file -Dfile=resources/commons-0.0.1-SNAPSHOT-jar-with-dependencies.jar -DgroupId=system-commons -DartifactId=commons -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar 
RUN mvn package
RUN mv target/location-0.0.1-SNAPSHOT-jar-with-dependencies.jar location.jar
#ENTRYPOINT ["java","-jar","apps.jar"]
CMD /bin/bash
