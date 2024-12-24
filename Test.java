public static void getProxyResponse(String fileReference, String token) {
    try {
        // Proxy setup
        InetSocketAddress proxyAddress = new InetSocketAddress("proxy.aetna.com", 9119);
        Proxy proxyDetails = new Proxy(Proxy.Type.HTTP, proxyAddress);

        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("N993527", "Java#Java@09".toCharArray());
            }
        });

        // URL Connection
        URL url = new URL("https://sit1-api.cvshealth.com/file/scan/download/v1/files/" + fileReference);
        HttpsURLConnection httpsURLConn = (HttpsURLConnection) url.openConnection(proxyDetails);

        httpsURLConn.setRequestMethod("GET");
        httpsURLConn.setDoInput(true); // Enable input for reading the response
        httpsURLConn.setUseCaches(true);

        // Set headers
        httpsURLConn.setRequestProperty("x-api-key", "T1gpDfjoNoNPdqqVfGgR1kw3Rnz0oi6w");
        httpsURLConn.setRequestProperty("Authorization", token);

        // Execute request
        int responseCode = httpsURLConn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        System.out.println("Response Message: " + httpsURLConn.getResponseMessage());
    } catch (IOException e) {
        e.printStackTrace();
    }
}
