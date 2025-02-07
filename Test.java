ConnectionProvider connectionProvider = ConnectionProvider.builder("custom-pool")
    .maxConnections(50) // Limit max connections
    .pendingAcquireMaxCount(100) // Limit queued requests
    .maxIdleTime(Duration.ofSeconds(20)) // Close idle connections after 20s
    .maxLifeTime(Duration.ofMinutes(5)) // Recreate connections every 5 min
    .build();

HttpClient httpClient = HttpClient.create(connectionProvider)
    .keepAlive(true)
    .responseTimeout(Duration.ofSeconds(30))
    .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP)
                         .host(proxyUrl)
                         .port(Integer.parseInt(proxyPort)));

WebClient webClient = WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(httpClient))
    .build();
