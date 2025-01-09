package com.example.controller;

import com.example.dto.GroupDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class GroupDetailsController {

    @GetMapping("/api/group-details/csv")
    public ResponseEntity<byte[]> downloadGroupDetailsAsCsv() {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(byteArrayOutputStream, true, StandardCharsets.UTF_8)) {

            // Write the CSV header
            writer.println(String.join(",",
                "Agent First Name", "Agent Last Name", "Agent NPN", "Firm Name", "GA Name",
                "Group Name", "Group EIN", "Group Zip Code", "Group State", "Quote Effective Date",
                "Number of Eligible Employees", "Union Employees Included?", 
                "Number of Union Employees", "Product Type", "Medical Eligible Count",
                "Enrolled Count", "Total Waivers", "Retirees Enrolled", "COBRA Enrolled",
                "Participation %", "FTE", "MLR TAE Lives", "Broker Fee", "SIC Code", 
                "SIC Name", "ERISA Indicator", "Work Location Address Line 1",
                "Work Location Address Line 2", "City", "State", "Zip", "Contract Type (Months)",
                "TPA", "Carrier Product Type", "Using ASA?", "Outside Renewal Date?", 
                "PEO", "With Aetna PEO?", "Grandfathered Plan?", "Off Cycle?", "Spinoff?",
                "With MEWA?", "Funding Arrangement", "Sales Executive", "Test Group"
            ));

            // Retrieve data
            List<GroupDetails> groupDetailsList = fetchGroupDetails();

            // Write data rows
            for (GroupDetails details : groupDetailsList) {
                writer.println(String.join(",",
                    details.getAgentFirstName(),
                    details.getAgentLastName(),
                    details.getAgentNPN(),
                    details.getFirmName(),
                    details.getGaName(),
                    details.getGroupName(),
                    details.getGroupEIN(),
                    details.getGroupZipCode(),
                    details.getGroupState(),
                    details.getQuoteEffectiveDate(),
                    String.valueOf(details.getNumberOfEligibleEmployees()),
                    String.valueOf(details.getCollectivelyBargainedUnionEmployeesIncluded()),
                    String.valueOf(details.getNumberOfCollectivelyBargainedUnionEmployees()),
                    details.getProductType(),
                    String.valueOf(details.getMedicalEligibleCount()),
                    String.valueOf(details.getEnrolledCountForParticipation()),
                    String.valueOf(details.getTotalNumberOfWaivers()),
                    String.valueOf(details.getNumberOfRetireesEnrolled()),
                    String.valueOf(details.getNumberOfCOBRAEnrolled()),
                    String.valueOf(details.getParticipationPercentage()),
                    String.valueOf(details.getFte()),
                    String.valueOf(details.getMlrTaeLives()),
                    String.valueOf(details.getRequestedBrokerFee()),
                    details.getSicCode(),
                    details.getSicName(),
                    String.valueOf(details.getErisaIndicator()),
                    details.getGroupWorkLocationAddressLine1(),
                    details.getGroupWorkLocationAddressLine2(),
                    details.getGroupWorkLocationCity(),
                    details.getGroupWorkLocationState(),
                    details.getGroupWorkLocationZip(),
                    String.valueOf(details.getContractTypeInMonths()),
                    details.getTpa(),
                    details.getCurrentCarrierProductType(),
                    String.valueOf(details.getUsingAetnaSignatureAdministrators()),
                    String.valueOf(details.getRequestOutsideRenewalDate()),
                    details.getPeo(),
                    String.valueOf(details.getCurrentlyWithAetnaPEO()),
                    String.valueOf(details.getGrandfatheredPlan()),
                    String.valueOf(details.getOffCycle()),
                    String.valueOf(details.getSpinoff()),
                    String.valueOf(details.getCurrentlyWithMEWA()),
                    details.getCurrentFundingArrangement(),
                    details.getAetnaSalesExecutive(),
                    String.valueOf(details.getTestGroup())
                ));
            }

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=group_details.csv");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

            // Return as ResponseEntity
            return ResponseEntity.status(HttpStatus.OK)
                    .headers(headers)
                    .body(byteArrayOutputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Error generating CSV file", e);
        }
    }

    private List<GroupDetails> fetchGroupDetails() {
        // Simulate fetching data. Replace this with actual service/database call.
        GroupDetails dummy = new GroupDetails();
        dummy.setAgentFirstName("John");
        dummy.setAgentLastName("Doe");
        dummy.setAgentNPN("12345");
        dummy.setFirmName("ABC Firm");
        dummy.setGaName("XYZ GA");
        dummy.setGroupName("Group A");
        dummy.setGroupEIN("123456789");
        dummy.setGroupZipCode("12345");
        dummy.setGroupState("NY");
        dummy.setQuoteEffectiveDate("2024-01-01");
        dummy.setNumberOfEligibleEmployees(50);
        dummy.setCollectivelyBargainedUnionEmployeesIncluded(true);
        dummy.setNumberOfCollectivelyBargainedUnionEmployees(10);
        dummy.setProductType("Product A");
        dummy.setMedicalEligibleCount(40);
        dummy.setEnrolledCountForParticipation(35);
        dummy.setTotalNumberOfWaivers(5);
        dummy.setNumberOfRetireesEnrolled(3);
        dummy.setNumberOfCOBRAEnrolled(2);
        dummy.setParticipationPercentage(87.5);
        dummy.setFte(45);
        dummy.setMlrTaeLives(50);
        dummy.setRequestedBrokerFee(1200.50);
        dummy.setSicCode("5678");
        dummy.setSicName("IT Industry");
        dummy.setErisaIndicator(true);
        dummy.setGroupWorkLocationAddressLine1("123 Main St");
        dummy.setGroupWorkLocationAddressLine2("Suite 456");
        dummy.setGroupWorkLocationCity("New York");
        dummy.setGroupWorkLocationState("NY");
        dummy.setGroupWorkLocationZip("12345");
        dummy.setContractTypeInMonths(12);
        dummy.setTpa("TPA XYZ");
        dummy.setCurrentCarrierProductType("Carrier A");
        dummy.setUsingAetnaSignatureAdministrators(true);
        dummy.setRequestOutsideRenewalDate(false);
        dummy.setPeo("Yes");
        dummy.setCurrentlyWithAetnaPEO(false);
        dummy.setGrandfatheredPlan(false);
        dummy.setOffCycle(false);
        dummy.setSpinoff(false);
        dummy.setCurrentlyWithMEWA(false);
        dummy.setCurrentFundingArrangement("Arrangement A");
        dummy.setAetnaSalesExecutive("Jane Doe");
        dummy.setTestGroup(true);

        return List.of(dummy);
    }
}
