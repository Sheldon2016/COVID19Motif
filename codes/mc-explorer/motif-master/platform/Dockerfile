FROM tomcat:9.0.1-jre8-alpine

RUN rm -rf /usr/local/tomcat/webapps/ROOT/

ADD ROOT.war /usr/local/tomcat/webapps/

CMD ["catalina.sh", "run"]
