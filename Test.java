Here are the application.properties files without comments:

Gateway Application (application.properties):

spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
server.tomcat.max-http-post-size=52428800
server.tomcat.max-swallow-size=52428800
webclient.connection.timeout=30000
webclient.response.timeout=60000
resttemplate.connection.timeout=30000
resttemplate.read.timeout=60000
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
server.compression.min-response-size=1024

Middleware Application (application.properties):

spring.webflux.multipart.enabled=true
spring.webflux.multipart.max-file-size=50MB
spring.webflux.multipart.max-request-size=50MB
spring.codec.max-in-memory-size=50MB
spring.server.connection-timeout=5000
webclient.connection.timeout=30000
webclient.response.timeout=60000
