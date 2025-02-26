import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MiddlewareController {

    private final MiddlewareService middlewareservice;
    private final JavaCrypto javaCrypto;
    private final ObjectMapper mapper;

    public MiddlewareController(MiddlewareService middlewareservice, JavaCrypto javaCrypto, ObjectMapper mapper) {
        this.middlewareservice = middlewareservice;
        this.javaCrypto = javaCrypto;
        this.mapper = mapper;
    }

    @GetMapping(value = "/refreshtoken", produces = "application/json")
    public Mono<ResponseEntity<String>> getUserToken(ServerWebExchange exchange) {
        log.info("Middleware controller | getUserToken ");

        return Mono.fromCallable(() -> {
            List<String> strHeaders = exchange.getRequest().getHeaders().get("tokens");
            if (strHeaders == null || strHeaders.isEmpty()) {
                log.warn("Middleware controller | getUserToken | Missing token header");
                return ResponseEntity.badRequest().body("{\"error\":\"Missing token header\"}");
            }

            String strEncrtTkn = strHeaders.get(0);
            if (strEncrtTkn.isBlank()) {
                log.warn("Middleware controller | getUserToken | Empty token received");
                return ResponseEntity.badRequest().body("{\"error\":\"Empty token received\"}");
            }

            try {
                String decryptedTkn = javaCrypto.decrypt(strEncrtTkn);
                log.info("Middleware controller | getUserToken | Decrypted token: {}", decryptedTkn);

                Tokens tokenObj = mapper.readValue(decryptedTkn, Tokens.class);
                String refreshTkn = tokenObj.getRefreshToken();

                Tokens newToken = middlewareservice.obtainToken("", refreshTkn);
                String encryptedToken = javaCrypto.encrypt(mapper.writeValueAsString(newToken));

                log.info("Middleware controller | getUserToken | Successfully encrypted new token");

                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.add("Content-Type", "application/json");
                responseHeaders.add("X-Content-Type-Options", "nosniff");

                return ResponseEntity.ok()
                        .headers(responseHeaders)
                        .body("{\"token\":\"" + encryptedToken + "\"}");

            } catch (Exception e) {
                log.error("Middleware controller | getUserToken | Error processing token", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\":\"Internal server error\"}");
            }
        });
    }
}
