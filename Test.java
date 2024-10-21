The error you’re seeing (MultipartException and ClientAbortException) is related to handling large multipart file uploads in the gateway or middleware application. This can occur if the file size exceeds the set limits or if there are issues with the connection being prematurely closed (EOFException).

Here are steps you can take to resolve this issue:

1. Ensure Correct Configuration of File Upload Limits

Make sure the upload limits are set correctly in both the gateway and middleware application properties:

Gateway Application (application.properties):

spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
server.tomcat.max-http-post-size=52428800
server.tomcat.max-swallow-size=52428800

Middleware Application (application.properties):

spring.webflux.multipart.max-file-size=50MB
spring.webflux.multipart.max-request-size=50MB
spring.codec.max-in-memory-size=50MB

2. Adjust Client Timeout and Buffer Size

The EOFException can also happen if the client (gateway) closes the connection prematurely due to timeouts. Increase the timeout settings for both the gateway and middleware.

Timeout Configuration (Gateway)

server.connection-timeout=60000  # Increase to 60 seconds or more
webclient.connection.timeout=60000
webclient.response.timeout=120000  # Adjust as needed for large files

Timeout Configuration (Middleware)

spring.server.connection-timeout=60000  # Increase to 60 seconds or more
webclient.connection.timeout=60000
webclient.response.timeout=120000  # Adjust as needed for large files

3. Enable Chunked Transfer Encoding (Gateway)

Make sure that the gateway supports chunked transfer encoding for large file uploads:

server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
server.compression.min-response-size=1024

4. Increase Tomcat Upload Buffer (if using Tomcat in Gateway)

If you are using Tomcat, increase the buffer size to handle larger file uploads:

server.tomcat.max-swallow-size=52428800  # 50MB
server.tomcat.max-http-post-size=52428800

5. Review File Upload Handling in Code

In the gateway or middleware application, you might also need to adjust your file upload logic. If using WebClient or RestTemplate, make sure the body size matches the upload limit configurations and that the connection is not aborted prematurely.

Example for WebClient (If used in Middleware):

WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
        .responseTimeout(Duration.ofMillis(120000))  // Set the appropriate timeout
        .option(ChannelOption.SO_RCVBUF, 50 * 1024 * 1024)))  // Increase buffer size
    .build();

6. Increase Buffer Sizes for Spring MVC (If used)

If you’re using Spring MVC in the gateway, make sure to increase buffer sizes for large file uploads:

spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

After making these changes, restart both the gateway and middleware applications, and try uploading the large file again. Let me know if the issue persists.
