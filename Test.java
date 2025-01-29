return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory()
							.wrap(new ObjectMapper().writeValueAsBytes("The uploaded file appears to be unsafe."))));


return chain.filter(exchange.mutate().build()).then(
    Mono.defer(() -> exchange.getResponse().writeWith(
        Mono.just(exchange.getResponse().bufferFactory()
            .wrap(new ObjectMapper().writeValueAsBytes(response))
        )
    ))
);


