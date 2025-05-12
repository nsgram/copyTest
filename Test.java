 public static String encryptedBase64withIV(String fileId,String token){
    String base64="";
    {
      String url = "https://sit1-api.cvshealth.com/file/scan/download/multipart/v1/files/"+fileId;
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("x-api-key", "T1gpDfjoNoNPdqqVfGgR1kw3Rnz0oi6w")
          .header("Authorization", "Bearer "+token)
          .header("x-customAppName", "cvs_file_scan_v1_asg")
          //.header("env", "pt")
          .GET()
          .build();

      try {
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
          byte[] bodyBytes = response.body();
          base64 = Base64.getEncoder().encodeToString(bodyBytes);
        } else {
          System.err.println("Request failed with status: " + response.statusCode());
        }
      } catch (IOException | InterruptedException e) {
        System.err.println("Download failed: " + e.getMessage());
      }
    }
    return base64;
  }
