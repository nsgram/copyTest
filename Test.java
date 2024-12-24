public static void getProxyResponse(String fileReference, String token) {

		URL urlCon;
		try {
			InetSocketAddress proxyAddress = new InetSocketAddress("proxy.aetna.com", 9119);

			Proxy proxyDetails = new Proxy(Proxy.Type.HTTP, proxyAddress);

			Authenticator.setDefault(new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {

					return new PasswordAuthentication("N993527", "Java#Java@09".toCharArray());
				}
			});
			urlCon = new URL("https://sit1-api.cvshealth.com/file/scan/download/v1/files/"+fileReference);
			URLConnection urlconn = urlCon.openConnection();

			HttpsURLConnection httpsURLConn = (HttpsURLConnection) urlCon.openConnection(proxyDetails);

			System.out.println("***********Call the  httpsURLConn**********" + httpsURLConn);

			if (urlconn instanceof HttpsURLConnection) {
				httpsURLConn.setRequestMethod("GET");
			}
			httpsURLConn.setDoOutput(true);
			httpsURLConn.setDoInput(true);
			httpsURLConn.setUseCaches(true);
			
			httpsURLConn.setRequestProperty("x-api-key", "T1gpDfjoNoNPdqqVfGgR1kw3Rnz0oi6w");
			httpsURLConn.setRequestProperty("Authorization", token);
			
			
			OutputStream outStream = httpsURLConn.getOutputStream();
			
			outStream.flush();
			outStream.close();
			
			int responseCode = httpsURLConn.getResponseCode();
			
			System.out.println("***********responseCode**********" + responseCode);
			System.out.println("***********responseCode**********" + httpsURLConn.getResponseMessage());
			
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

sun.net.www.protocol.https.DelegateHttpsURLConnection:https://sit1-api.cvshealth.com/file/scan/download/v1/files/1735053929164-AVScan__negative-84ddf130
***********responseCode**********405
***********responseCode**********Method Not Allowed


getting 200 ok response in postman
