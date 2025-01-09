package com.example.controller;

import com.example.dto.GroupDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@RestController
public class GroupDetailsController {

    @GetMapping("/api/group-details/download")
    public void downloadGroupDetailsAsCsv(HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=group_details.csv");

        try (PrintWriter writer = response.getWriter()) {
            // Write CSV header
            writer.println(String.join(",",
                "Agent First Name",
                "Agent Last Name",
                "Agent NPN",
                "Firm Name",
                "GA Name",
                "Group Name",
                "Group EIN",
                "Group Zip Code",
                "Group State",
                "Quote Effective Date",
                "Number of eligible employees",
                "Does eligible employees include collectively bargained union employees?",
                "Number of collectively bargained union employees?",
                "Product type",
                "Medical Eligible count",
                "Enrolled count for participation",
                "Total # of Waivers",
                "# of Retirees Enrolled for Coverage",
                "# of COBRA/St Continuation Enrolled",
                "Participation %",
                "FTE",
                "MLR TAE Lives",
                "Requested broker fee",
                "SIC Code",
                "SIC Name",
                "ERISA Indicator",
                "Group work location Address Line 1",
                "Group work location Address Line 2",
                "Group work location City",
                "Group work location State",
                "Group work location Zip",
                "Contract Type (in Months)",
                "TPA",
                "Current carrier product type",
                "Are you using Using Aetna Signature Administrators® (ASA)",
                "Is this request for an effective date that is outside of the renewal date?",
                "PEO (yes/no)",
                "Currently with Aetna Professional Employer Organization (PEO)?",
                "Is group currently in a Grandfathered Plan?",
                "Is the group Off cycle?",
                "Is the group a spinoff?",
                "Is the group currently with a MEWA?",
                "Current Funding Arrangement",
                "Aetna Sales Executive",
                "Test Group"
            ));

            // Dummy data for demonstration
            List<GroupDetails> groupDetailsList = getDummyData();

            // Write CSV data
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
                    details.getNumberOfEligibleEmployees().toString(),
                    details.getDoesEligibleEmployeesIncludeCollectivelyBargainedUnionEmployees().toString(),
                    details.getNumberOfCollectivelyBargainedUnionEmployees().toString(),
                    details.getProductType(),
                    details.getMedicalEligibleCount().toString(),
                    details.getEnrolledCountForParticipation().toString(),
                    details.getTotalNumberOfWaivers().toString(),
                    details.getNumberOfRetireesEnrolledForCoverage().toString(),
                    details.getNumberOfCobraContinuationEnrolled().toString(),
                    details.getParticipationPercentage().toString(),
                    details.getFte().toString(),
                    details.getMlrTaeLives().toString(),
                    details.getRequestedBrokerFee().toString(),
                    details.getSicCode(),
                    details.getSicName(),
                    details.getErisaIndicator().toString(),
                    details.getGroupWorkLocationAddressLine1(),
                    details.getGroupWorkLocationAddressLine2(),
                    details.getGroupWorkLocationCity(),
                    details.getGroupWorkLocationState(),
                    details.getGroupWorkLocationZip(),
                    details.getContractTypeInMonths().toString(),
                    details.getTpa(),
                    details.getCurrentCarrierProductType(),
                    details.getUsingAetnaSignatureAdministrators().toString(),
                    details.getIsOutsideRenewalDate().toString(),
                    details.getPeo(),
                    details.getCurrentlyWithAetnaProfessionalEmployerOrganization().toString(),
                    details.getIsGrandfatheredPlan().toString(),
                    details.getIsOffCycle().toString(),
                    details.getIsSpinoff().toString(),
                    details.getIsCurrentlyWithMewa().toString(),
                    details.getCurrentFundingArrangement(),
                    details.getAetnaSalesExecutive(),
                    details.getTestGroup().toString()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Dummy data
    private List<GroupDetails> getDummyData() {
        GroupDetails groupDetails = new GroupDetails();
        groupDetails.setAgentFirstName("John");
        groupDetails.setAgentLastName("Doe");
        groupDetails.setAgentNPN("12345");
        groupDetails.setFirmName("Test Firm");
        groupDetails.setGaName("GA Name");
        groupDetails.setGroupName("Group A");
        groupDetails.setGroupEIN("EIN12345");
        groupDetails.setGroupZipCode("12345");
        groupDetails.setGroupState("NY");
        groupDetails.setQuoteEffectiveDate("2023-12-31");
        groupDetails.setNumberOfEligibleEmployees(10);
        groupDetails.setDoesEligibleEmployeesIncludeCollectivelyBargainedUnionEmployees(true);
        groupDetails.setNumberOfCollectivelyBargainedUnionEmployees(2);
        groupDetails.setProductType("Type A");
        groupDetails.setMedicalEligibleCount(8);
        groupDetails.setEnrolledCountForParticipation(6);
        groupDetails.setTotalNumberOfWaivers(2);
        groupDetails.setNumberOfRetireesEnrolledForCoverage(1);
        groupDetails.setNumberOfCobraContinuationEnrolled(1);
        groupDetails.setParticipationPercentage(75.0);
        groupDetails.setFte(10);
        groupDetails.setMlrTaeLives(10);
        groupDetails.setRequestedBrokerFee(500.0);
        groupDetails.setSicCode("1234");
        groupDetails.setSicName("Tech");
        groupDetails.setErisaIndicator(true);
        groupDetails.setGroupWorkLocationAddressLine1("Address Line 1");
        groupDetails.setGroupWorkLocationAddressLine2("Address Line 2");
        groupDetails.setGroupWorkLocationCity("New York");
        groupDetails.setGroupWorkLocationState("NY");
        groupDetails.setGroupWorkLocationZip("12345");
        groupDetails.setContractTypeInMonths(12);
        groupDetails.setTpa("TPA Name");
        groupDetails.setCurrentCarrierProductType("Type B");
        groupDetails.setUsingAetnaSignatureAdministrators(true);
        groupDetails.setIsOutsideRenewalDate(false);
        groupDetails.setPeo("Yes");
        groupDetails.setCurrentlyWithAetnaProfessionalEmployerOrganization(true);
        groupDetails.setIsGrandfatheredPlan(false);
        groupDetails.setIsOffCycle(false);
        groupDetails.setIsSpinoff(false);
        groupDetails.setIsCurrentlyWithMewa(false);
        groupDetails.setCurrentFundingArrangement("Funding Arrangement");
        groupDetails.setAetnaSalesExecutive("Executive Name");
        groupDetails.setTestGroup(true);

        return Arrays.asList(groupDetails);
    }
}
