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

        // Request settings
        httpsURLConn.setRequestMethod("GET");
        httpsURLConn.setDoInput(true);
        httpsURLConn.setUseCaches(true);
        httpsURLConn.setRequestProperty("x-api-key", "T1gpDfjoNoNPdqqVfGgR1kw3Rnz0oi6w");
        httpsURLConn.setRequestProperty("Authorization", token);

        // Execute request
        int responseCode = httpsURLConn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        System.out.println("Response Message: " + httpsURLConn.getResponseMessage());

        // Check if the response is successful
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(httpsURLConn.getInputStream()))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                System.out.println("Response Body:\n" + response.toString());
            }
        } else {
            System.err.println("Error: Unable to fetch the file. Response Code: " + responseCode);
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
}
