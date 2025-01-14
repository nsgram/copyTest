Below is the complete Spring Boot project code for implementing the described functionality, including entity classes, repository, service, controller, and dynamic query generation.

1. Entity Class

package com.example.quotesreport.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "QUOTES_REPORT_VIEW", schema = "asgwy_db_schema")
public class QuotesReportView {

    @Id
    private Long groupId;
    private String groupNm;
    private String employerIdNbr;
    private Boolean testGroupInd;
    private String groupZipCd;
    private String stateNm;
    private LocalDate effectiveDt;
    private Integer eligibleEntrdCnt;
    private Boolean unionEmpInd;
    private Integer unionEmpCnt;
    private Long concessReqQuoteId;
    private String concessReqStatusCd;
    private Double concessReqPct;
    private String concessReqReasonTxt;
    private String groupLocAddrLine1Txt;
    private String groupLocAddrLine2Txt;
    private String groupLocCityNm;
    private String currCarrierTypDesc;
    private String currMedCarrierNm;
    private Boolean grpMewaInd;
    private Boolean aetnaPeoInd;
    private Integer contractPeriodMoNbr;
    private Integer totAvgEmpCnt;
    private String sicCd;
    private String sicNm;
    private Double defBrokerFeeAmt;
    private Integer participationCnt;
    private Integer eligibleDervdCnt;
    private Integer waiverCnt;
    private Integer eligibleRetCnt;
    private Integer cobraEmpCnt;
    private Double participationPct;
    private Integer ftEqvlntCnt;
    private String erisaCd;
    private String currTpaNm;
    private String aetnaSalesExe;
    private String quoteStatusCd;
    private LocalDate submissionDt;
    private String statusDesc;
    private String stateCd;

    // Getters and setters (use Lombok @Data annotation for brevity, if desired)
    // Example:
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    // Add getters and setters for all fields.
}

2. Repository Interface

package com.example.quotesreport.repository;

import com.example.quotesreport.entity.QuotesReportView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotesReportViewRepository 
    extends JpaRepository<QuotesReportView, Long>, JpaSpecificationExecutor<QuotesReportView> {
}

3. Specification for Dynamic Query

package com.example.quotesreport.specification;

import com.example.quotesreport.entity.QuotesReportView;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QuotesReportViewSpecification {

    public static Specification<QuotesReportView> buildDynamicQuery(
            LocalDate effectiveDateFrom,
            LocalDate effectiveDateTo,
            String statusDesc,
            String stateCd,
            LocalDate submissionDateFrom,
            LocalDate submissionDateTo) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (effectiveDateFrom != null && effectiveDateTo != null) {
                predicates.add(criteriaBuilder.between(
                        root.get("effectiveDt"), effectiveDateFrom, effectiveDateTo));
            }

            if (statusDesc != null) {
                predicates.add(criteriaBuilder.equal(root.get("statusDesc"), statusDesc));
            }

            if (stateCd != null) {
                predicates.add(criteriaBuilder.equal(root.get("stateCd"), stateCd));
            }

            if (submissionDateFrom != null && submissionDateTo != null) {
                predicates.add(criteriaBuilder.between(
                        root.get("submissionDt"), submissionDateFrom, submissionDateTo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

4. Service Class

package com.example.quotesreport.service;

import com.example.quotesreport.entity.QuotesReportView;
import com.example.quotesreport.repository.QuotesReportViewRepository;
import com.example.quotesreport.specification.QuotesReportViewSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class QuotesReportViewService {

    @Autowired
    private QuotesReportViewRepository quotesReportViewRepository;

    public List<QuotesReportView> getFilteredQuotes(
            LocalDate effectiveDateFrom,
            LocalDate effectiveDateTo,
            String statusDesc,
            String stateCd,
            LocalDate submissionDateFrom,
            LocalDate submissionDateTo) {

        return quotesReportViewRepository.findAll(
                QuotesReportViewSpecification.buildDynamicQuery(
                        effectiveDateFrom, effectiveDateTo, statusDesc, stateCd,
                        submissionDateFrom, submissionDateTo));
    }
}

5. Controller Class

package com.example.quotesreport.controller;

import com.example.quotesreport.entity.QuotesReportView;
import com.example.quotesreport.service.QuotesReportViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/quotes-report")
public class QuotesReportViewController {

    @Autowired
    private QuotesReportViewService quotesReportViewService;

    @GetMapping("/filter")
    public List<QuotesReportView> getFilteredQuotes(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateFrom,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateTo,
            @RequestParam(required = false) String statusDesc,
            @RequestParam(required = false) String stateCd,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submissionDateFrom,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submissionDateTo) {

        return quotesReportViewService.getFilteredQuotes(
                effectiveDateFrom, effectiveDateTo, statusDesc, stateCd,
                submissionDateFrom, submissionDateTo);
    }
}

6. application.properties (Database Configuration)

spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

7. Run the Application

	•	Build and run your Spring Boot application.
	•	Access the endpoint:

GET http://localhost:8080/api/quotes-report/filter

Query parameters you can use:

	•	effectiveDateFrom, effectiveDateTo: Date range for effectiveDt.
	•	statusDesc: Filter by status description.
	•	stateCd: Filter by state code.
	•	submissionDateFrom, submissionDateTo: Date range for submissionDt.

Summary

	•	The QUOTES_REPORT_VIEW PostgreSQL view ensures essential filtering is done at the database level.
	•	Spring Boot implements dynamic filtering using Specification and Spring Data JPA.
	•	This setup is highly extensible for future enhancements.
