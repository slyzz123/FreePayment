FROM pig4cloud/java:8-jre

MAINTAINER wangiegie@gmail.com

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms128m -Xmx256m -Djava.security.egd=file:/dev/./urandom"

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mkdir -p /pigx-register

WORKDIR /pigx-register

EXPOSE 8848

ADD ./target/pigx-register.jar ./


CMD sleep 60;java $JAVA_OPTS -jar pigx-register.jar


