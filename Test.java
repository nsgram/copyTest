return chain.filter(exchange.mutate().build()).then(
    Mono.defer(() -> exchange.getResponse().writeWith(
        Mono.just(exchange.getResponse().bufferFactory()
            .wrap(new ObjectMapper().writeValueAsBytes(response))
        )
    ))
);
