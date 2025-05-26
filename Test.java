 @Operation(
    summary = "Save Broker Certification",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = @Content(schema = @Schema(implementation = BrokerCertificationRequest.class))
    )
)
@PostMapping("/brokercertification")
public ResponseEntity<Object> saveBrokerCertification(
        @RequestBody BrokerCertificationRequest brokerCertificationRequest,
        HttpServletRequest httpServletRequest) {
    ...
}
