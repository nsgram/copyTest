@GetMapping(value = "/refreshtoken")
	  public String getUserToekn(ServerWebExchange exchange)
	  {
		  log.info("Middleware controller | getUserToekn ");
		  String strEncrtTkn = "";
		  ObjectMapper mapper = new ObjectMapper();
		  JavaCrypto  javaCrypto= new JavaCrypto();
		  try { 
			  List<String> strHeadrs = exchange.getRequest().getHeaders().get("tokens");
		 	  if (strHeadrs != null) {
		 		 strEncrtTkn =  strHeadrs.get(0); 
		 		 }
		 	 String decryptdTkn = javaCrypto.decrypt(strEncrtTkn);
		 	  log.info(" Middleware controller | getUserToekn | decrypted token " + decryptdTkn); 
		 	Tokens t = mapper.readValue(decryptdTkn, Tokens.class);
		 	String refreshTkn = t.getRefreshToken();
		 	t = middlewareservice.obtainToken("", refreshTkn);
		 	 strEncrtTkn = javaCrypto.encrypt(mapper.writeValueAsString(t));
			log.info("MiddlewarService | getUserToekn |encrypted token " + strEncrtTkn);
			 
		 } 
		  catch (Exception e) { 
			  e.printStackTrace(); 
			  }
		  
		  return strEncrtTkn;
		  
	  
	  }

The method getUserToekn embeds untrusted data in generated output with strEncrtTkn, at line
126 of source/asgwy-webmwweb/
src/main/java/com/aetna/asgwy/webmw/controller/MiddlewareController.java. This
untrusted data is embedded into the output without proper sanitization or encoding, enabling
an attacker to inject malicious code into the generated web-page.
The attacker would be able to alter the returned web page by simply providing modified data in
the user input exchange, which is read by the getUserToekn method at line 126 of
source/asgwy-webmwweb/
src/main/java/com/aetna/asgwy/webmw/controller/MiddlewareController.java. This input
then flows through the code straight to the output web page, without sanitization.
This can enable a Reflected Cross-Site Scripting (XSS) attack.

The method getUserToekn embeds untrusted data in generated output with strEncrtTkn, at line 152 of source\asgwy-webmw-web\src\main\java\com\aetna\asgwy\webmw\controller\MiddlewareController.java. This untrusted data is embedded into the output without proper sanitization or encoding, enabling an attacker to inject malicious code into the generated web-page.

The attacker would be able to alter the returned web page by simply providing modified data in the user input exchange, which is read by the getUserToekn method at line 126 of source\asgwy-webmw-web\src\main\java\com\aetna\asgwy\webmw\controller\MiddlewareController.java. This input then flows through the code straight to the output web page, without sanitization. 

This can enable a Reflected Cross-Site Scripting (XSS) attack.



please fix the above sonar and checkmarx issue
