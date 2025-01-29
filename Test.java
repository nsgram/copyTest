return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
							.wrap(new ObjectMapper().writeValueAsBytes("The uploaded file appears to be unsafe."))));


return chain.filter(exchange.mutate().build()).then(
    Mono.defer(() -> exchange.getResponse().writeWith(
        Mono.just(exchange.getResponse().bufferFactory()
            .wrap(new ObjectMapper().writeValueAsBytes(response))
        )
    ))
);


try {
    byte[] responseBody = new ObjectMapper().writeValueAsBytes(response);
    exchange.getResponse().writeWith(
        Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody))
    );
} catch (JsonProcessingException e) {
    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing response", e));
}

return chain.filter(exchange.mutate().build());



