To configure a RestTemplate with a proxy in Spring Web 6.1.13, you can still use Apache HttpClient or the standard JDK proxy setup, since RestTemplate doesn’t directly support proxies out of the box. Below is an updated implementation compatible with Spring Web 6.1.13.

Code Example for Spring Web 6.1.13

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateWithProxy {

    public RestTemplate createRestTemplateWithProxy(String proxyHost, int proxyPort) {
        // Configure the proxy
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);

        // Create the HttpClient with the proxy configuration
        CloseableHttpClient httpClient = HttpClients.custom()
                .setProxy(proxy)
                .build();

        // Integrate the HttpClient with RestTemplate
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return new RestTemplate(requestFactory);
    }

    public static void main(String[] args) {
        String proxyHost = "proxy.example.com";
        int proxyPort = 8080;

        RestTemplateWithProxy restTemplateConfig = new RestTemplateWithProxy();
        RestTemplate restTemplate = restTemplateConfig.createRestTemplateWithProxy(proxyHost, proxyPort);

        // Test API Call
        String apiUrl = "https://jsonplaceholder.typicode.com/posts";
        String response = restTemplate.getForObject(apiUrl, String.class);
        System.out.println(response);
    }
}

Required Maven Dependency

Ensure the correct version of Apache HttpClient (version 5.x for Spring Web 6.x) is included in your pom.xml:

<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.2.1</version>
</dependency>

Explanation of Code

	1.	Apache HttpClient 5.x:
	•	The org.apache.hc.client5 library is compatible with Spring Web 6.x and Java 17+.
	•	HttpHost is used to define the proxy settings.
	2.	RestTemplate Configuration:
	•	Use HttpComponentsClientHttpRequestFactory to bridge the HttpClient proxy configuration into the RestTemplate.
	3.	Spring Compatibility:
	•	Spring Web 6.x supports Java 17 and adopts HttpClient 5.x for compatibility.

Alternative: Standard JDK Proxy (No Extra Library)

If you prefer not to use Apache HttpClient, you can use the standard java.net.Proxy:

import java.net.InetSocketAddress;
import java.net.Proxy;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateWithJDKProxy {

    public RestTemplate createRestTemplateWithJDKProxy(String proxyHost, int proxyPort) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(proxy);
        return new RestTemplate(requestFactory);
    }

    public static void main(String[] args) {
        String proxyHost = "proxy.example.com";
        int proxyPort = 8080;

        RestTemplateWithJDKProxy config = new RestTemplateWithJDKProxy();
        RestTemplate restTemplate = config.createRestTemplateWithJDKProxy(proxyHost, proxyPort);

        // Example API call
        String apiUrl = "https://jsonplaceholder.typicode.com/posts";
        String response = restTemplate.getForObject(apiUrl, String.class);
        System.out.println(response);
    }
}

Which to Choose?

	1.	Apache HttpClient: Offers better control and advanced configurations. Use this for complex proxy requirements.
	2.	JDK Proxy: A lightweight solution suitable for simple use cases. No external library is needed.

Both approaches work seamlessly in Spring Web 6.1.13. Choose based on your project needs!
