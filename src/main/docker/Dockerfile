# online-ivr-service Dockerfile
# Version 1.0

# Base images 基础镜像
FROM openjdk:8-jre-alpine


#MAINTAINER 维护者信息
MAINTAINER yyzmain

#设置时区
ENV TZ='Asia/Shanghai'

#VOLUME 用于指定持久化目录
VOLUME /tmp

#EXPOSE 映射端口
#EXPOSE 9002

#USER 指定运行容器时的用户名或 UID
USER root

#WORKDIR 相当于cd
WORKDIR /root/app

#ADD  文件放在当前目录下
ADD main-study-0.0.1.jar /home/app

#RUN 执行以下命令
#RUN unzip nlbomc-strategy-service-2.0.0-RELEASE-2.0.0-RELEASE.zip

#WORKDIR 相当于cd
#WORKDIR /home/rhkf/online-ivr-service-2.0.0-RELEASE/config

#RUN 执行以下命令
#RUN rm -rf application-prod.yml
#RUN cp application-yz.yml application-prod.yml


WORKDIR /home/app
#不写现场应该没办法运行，只是猜测没验证
CMD java -jar -Xms256m -Xmx1024m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -Xss256k \
        -Dloader.path=/home/rhkf/lib -Dspring.profiles.active=dev -Dspring.config.location=config/ \
        -Dlogging.config=config/log4j2-dev.xml -Dspring.thymeleaf.prefix=file:templates/ \
        app/nlbomc-strategy-service-2.0.0-RELEASE.jar
