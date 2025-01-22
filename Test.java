To enhance your implementation by using the OpenCSV library, you can replace the manual CSV generation in the generateCsvResponse method. OpenCSV is efficient and reduces the manual effort needed for handling CSV formatting. Here’s the updated method using OpenCSV:

Dependency

First, ensure you have the OpenCSV dependency in your pom.xml if you’re using Maven:

<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.8</version>
</dependency>

Updated generateCsvResponse Method

Replace the current implementation of generateCsvResponse with the following:

private ResponseEntity<byte[]> generateCsvResponse(List<QuotesReport> quotesReportList, String stateCd) {
    log.info("Generating CSV file using OpenCSV...");
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8);
         CSVWriter csvWriter = new CSVWriter(writer)) {

        String stateCode = StringUtils.isNotBlank(stateCd) ? stateCd + "_" : "";

        String filename = "Quotes_Report_" + stateCode + new Random().nextInt(99) + 1 + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

        // Write header row
        csvWriter.writeNext(AsgwyConstants.QuotesReportHeader());

        // Write data rows
        quotesReportList.stream()
                .map(this::convertToCsvRowArray)
                .forEach(csvWriter::writeNext);

        csvWriter.flush();
        byte[] csvBytes = byteArrayOutputStream.toByteArray();

        HttpHeaders headers = createCsvHeaders(filename, csvBytes.length);
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(csvBytes);

    } catch (IOException e) {
        log.error("Error generating CSV file", e);
        throw new AsgwyGlobalException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error generating CSV file");
    }
}

Helper Method: Convert Report to Array

Modify the convertToCsvRow method to return an array of strings:

private String[] convertToCsvRowArray(QuotesReport report) {
    return new String[]{
        report.getAgentFirstName(),
        report.getAgentLastName(),
        report.getAgentNPN(),
        report.getFirmName(),
        report.getGaName(),
        report.getGroupNm(),
        report.getEmployerIdNbr(),
        report.getGroupZipCd(),
        report.getStateNm(),
        report.getGroupLocAddrLine1Txt(),
        report.getGroupLocAddrLine2Txt(),
        report.getGroupLocCityNm(),
        report.getCurrCarrierTypDesc(),
        report.getCurrMedCarrierNm(),
        report.getGrpMewaAssoc(),
        report.getAetnaPeoInd(),
        safeString(report.getEffectiveDt()),
        safeString(report.getContractPeriodMoNbr()),
        safeString(report.getEligibleEntrdCnt()),
        safeString(report.getTotAvgEmpCnt()),
        report.getSicCd(),
        report.getSicNm(),
        safeString(report.getDefBrokerFeeAmt()),
        report.getUnionEmpInd(),
        safeString(report.getUnionEmpCnt()),
        report.getProductCd(),
        safeString(report.getParticipationCnt()),
        safeString(report.getEligibleDervdCnt()),
        safeString(report.getWaiverCnt()),
        safeString(report.getEligibleRetCnt()),
        safeString(report.getCobraEmpCnt()),
        safeString(report.getParticipationPct()),
        safeString(report.getFtEqvlntCnt()),
        report.getErisaCd(),
        report.getCurrTpaNm(),
        report.getAetnaSalesExe(),
        safeString(report.getTestGroupInd()),
        safeString(report.getConcessReqQuoteId()),
        report.getConcessReqStatusCd(),
        safeString(report.getConcessReqPct()),
        report.getConcessReqReasonTxt()
    };
}

Advantages of Using OpenCSV

	1.	Simplified API: OpenCSV automatically handles escaping special characters (e.g., commas, quotes).
	2.	Readability: The code is more concise and easier to maintain.
	3.	Extensibility: It supports additional features like parsing and handling complex CSV files if needed later.

This approach significantly improves your implementation’s efficiency and reliability.
