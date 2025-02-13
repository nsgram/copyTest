package com.aetna.asgwy.reporting.batch.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

//import com.aetna.asgwy.dto.SFDCReport;
//import com.aetna.asgwy.dto.SFDCReport;
//import com.aetna.asgwy.dto.QuotesReport;
import com.aetna.asgwy.reporting.batch.constants.ReportHeaderConstants;
import com.aetna.asgwy.reporting.batch.repositories.entity.QuotesReportEntity;
import com.aetna.asgwy.reporting.batch.repositories.entity.SFDCReportEntity;
import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConvertToCSV {
	
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

	//Quote report
	public byte[] convertListToBytes(List<QuotesReportEntity> quotesReportEntityList) {
		byte[] csvBytes = null;
		if (quotesReportEntityList.size() > 0) {// <= threshold) {
			log.info("CSV File generating....");
			// File fpw = new File("printwriter.txt");

			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8);
					CSVWriter csvWriter = new CSVWriter(writer)) {
				csvWriter.writeNext(ReportHeaderConstants.quotesReportHeader());

				// quotesReportList.stream().map(this::convertToCsvRowArray).forEach(csvWriter::writeNext);
				quotesReportEntityList.stream().map(this::convertToCsvRowArray).forEach(csvWriter::writeNext);

				csvWriter.flush();

				csvBytes = byteArrayOutputStream.toByteArray();
				// HttpHeaders headers = createCsvHeaders(filename, csvBytes.length);
				// return
				// ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(csvBytes);
			} catch (Exception e) {
				log.error("Exception at generating CSV file ::{}", e);
				throw new RuntimeException("Error generating CSV file", e);
			}
		}
		return csvBytes;
	}
	
	public byte[] convertListToBytesForSFDCReport(List<SFDCReportEntity> quotesReportEntityList) {
		byte[] csvBytes = null;
		if (quotesReportEntityList.size() > 0) {// <= threshold) {
			log.info("CSV File generating....");
			// File fpw = new File("printwriter.txt");

			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8);
					CSVWriter csvWriter = new CSVWriter(writer)) {
				csvWriter.writeNext(ReportHeaderConstants.sfdcReportHeader());

				// quotesReportList.stream().map(this::convertToCsvRowArray).forEach(csvWriter::writeNext);
				//quotesReportEntityList.stream().map(this::convertSFDCReportToCsvRowForSFDCReport).forEach(csvWriter::writeNext);
				quotesReportEntityList.stream().map(this::convertSFDCReportToCsvRow).forEach(csvWriter::writeNext);
				

				csvWriter.flush();

				csvBytes = byteArrayOutputStream.toByteArray();
				// HttpHeaders headers = createCsvHeaders(filename, csvBytes.length);
				// return
				// ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(csvBytes);
			} catch (Exception e) {
				log.error("Exception at generating CSV file ::{}", e);
				throw new RuntimeException("Error generating CSV file", e);
			}
		}
		return csvBytes;
	}
	
	
	private String[] convertToCsvRowArray(QuotesReportEntity report) {
		return new String[] { report.getGroupNm(), report.getEmployerIdNbr(), safeString(report.getTestGroupInd()),
				safeString(report.getQuoteId()), report.getStatusDesc(), convertObjectToDateString(report.getEffectiveDt()),
				report.getGroupZipCd(), report.getGroupStateCd(), safeString(report.getEligibleEntrdCnt()),
				report.getUnionEmpInd(), safeString(report.getUnionEmpCnt()), report.getProductCd(),
				report.getGroupLocAddrLine1Txt(), report.getGroupLocAddrLine2Txt(), report.getGroupLocCityNm(),
				report.getCurrCarrierTypDesc(), report.getCurrMedCarrierNm(), report.getGrpMewaAssoc(),
				safeString(report.getAetnaPeoInd()), safeString(report.getContractPeriodMoNbr()),
				safeString(report.getTotAvgEmpCnt()), report.getSicCd(), report.getSicNm(),
				safeString(report.getDefBrokerFeeAmt()), safeString(report.getParticipationCnt()),
				safeString(report.getEligibleDervdCnt()), safeString(report.getWaiverCnt()),
				safeString(report.getEligibleRetCnt()), safeString(report.getCobraEmpCnt()),
				safeString(report.getParticipationPct()), safeString(report.getFtEqvlntCnt()), report.getErisaCd(),
				report.getCurrTpaNm(), report.getConcessReqStatusCd(), safeString(report.getConcessReqPct()),
				report.getConcessReqReasonTxt(), convertObjectToDateString(report.getConn_create_dts()), report.getCommentTxt(),
				report.getQuotesSAFullNm(), report.getQuoteSANPN(), report.getQuoteSAAgencyNm(),
				report.getQuoteSACfoNm(), report.getQuoteSACfoLoc(), report.getQuoteGAAgencyNm(),
				report.getQuoteGASACfoNm(), report.getQuoteGASACfoLoc(),
				safeString(report.getQuoteSalesExecFirstName()) + " "
						+ safeString(report.getQuoteSalesExecLastName()) };
	}

		
	
	private String[] convertSFDCReportToCsvRow(SFDCReportEntity report) {
		//This is for Selling Agents Broker Type with static value "Licensed"
		if(StringUtils.isNotBlank(report.getQuote_sa_full_nm())) {
				report.setStatus_desc("Licensed");
			} else {
				report.setStatus_desc("");
				
			}
		
		// Group
		return new String[] { 
				//Groups - 7 fields
				safeString(report.getGroup_id()),
				safeString(report.getSmart_group_id()), 
				report.getGroup_nm(), 
				report.getPrior_psu_id(),
				report.getEmployer_id_nbr(), 
				safeString(report.getTest_group_ind()),
				report.getGroup_nm_upt_ind(),
				
				//Quote - 7 fields
				 ""+report.getQuote_id(),
				//This is Smart Quote ID Header
				safeString(report.getSmart_quote_id()),
				report.getGroup_zip_cd(),
				safeString(report.getEligible_entrd_cnt()),
				safeString(report.getUnion_emp_cnt()),
				convertObjectToDateString(report.getEffective_dt()), 
				report.getQuote_type_cd(),
				
				 //QuoteCoverage - 30 fields
				report.getGroup_loc_addr_line1_txt(),
				report.getGroup_loc_addr_line2_txt(), 
				report.getGroup_loc_city_nm(), 
				report.getState_nm(), 
				safeString(report.getEnrlng_emp_cnt()),
				safeString(report.getWaiver_cnt()),
				safeString(report.getEligible_ret_cnt()),
				safeString(report.getCobra_emp_cnt()), 
				report.getFt_eqvlnt_cnt(),
				safeString(report.getTot_avg_emp_cnt()),
				safeString(report.getSic_cd()),
				report.getProduct_cd(), 
				report.getCurr_med_carrier_nm(), 
				report.getDoc_claimsexp_indicator(),
				convertObjectToDateString(report.getRenewal_dt()), 
				report.getSgr_ind(),
				safeString(report.getParticipation_pct()), 
				report.getGrp_mewa_ind(),
				report.getCurr_carrier_typ_desc(),
				report.getErisa_cd(),
				safeString(report.getContract_period_mo_nbr()),
				report.getRet_thold_ind(), 
				safeString(report.getDef_broker_fee_amt()),
				safeString(report.getAdj_broker_fee_amt()), 
				report.getRaf_type(), 
				report.getDoc_IMQ_indicator(),
				report.getDoc_RC_indicator(),
				report.getDoc_SBC_indicator(),				
				report.getRCDocUploadedIndicator(), 
				report.getAetna_peo_ind(),
				
				//QuoteConcession - 3 fields
				safeString(report.getConcess_req_pct()),
				report.getConcess_req_reason_txt(),
				safeString(report.getQuote_concess_doc_ind()),
				
				//QuoteDocument - 3 fields							 
				report.getDoc_nm(), 
				report.getDoc_category(), 
				report.getDoc_subcategory(),
				
				
				
				
				//Quote Contributor	- 9 fields
				report.getQuote_perfmr_type_cd(),
				report.getQuote_perfmr_first_nm() + " " + report.getQuote_perfmr_last_nm(),
				report.getQuote_perfmr_address(),
				report.getQuote_perfmr_email_txt(),
				
				report.getQuote_submtr_type_cd(),
				report.getQuote_submtr_first_nm() + " " + report.getQuote_submtr_last_nm(),
				report.getQuote_submtr_address(),
				report.getQuote_submtr_email_txt(),
				report.getQuote_submtr_phone_nbr(),
				
				//QuoteSellingAgent	- 10 fields		
				//This is for Selling Agents Broker Type with static value "Licensed"
				report.getStatus_desc(),
				report.getQuote_sa_full_nm(),  		
				report.getQuote_sa_email_txt(), 
				report.getQuote_sa_phone_nbr(),
				report.getQuote_sa_agency_nm(),
				report.getQuote_sa_agency_addr(), 
				report.getQuote_sa_agency_email_txt(), 
				report.getQuote_sa_agency_phone_nbr(),
				report.getQuote_sa_CFO_nm(),
				report.getQuote_sa_CFO_loc(),
				
				//QuoteGeneralAgency	 - 9 fields
				report.getQuote_ga_sa_nm(), 
				report.getQuote_ga_sa_email(), 
				report.getQuote_ga_sa_phone(),
				
				report.getQuote_ga_agency_nm(), 
				report.getQuote_ga_agency_addr(), 
				report.getQuote_ga_agency_email_txt(),
				report.getQuote_ga_agency_phone_nbr(), 
				
				report.getQuote_sa_CFO_loc(),
				report.getQuote_ga_sa_CFO_nm(),
				
				
				//QuoteSalesExec - 1 field - AETNASALESEXECUTIVE
				safeString(report.getQuote_sales_exec_first_name()) + " " + safeString(report.getQuote_sales_exec_last_name()),					
				
				};
	}

	private String getStringValue(Object obj) {
		// Handle null values
		return obj != null ? obj.toString() : "";
	}

	private String safeString(Object obj) {
		return obj != null ? obj.toString() : "";
	}

	private String escapeCsvField(String field) {
		if (field == null) {
			return "";
		}
		if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
			// Wrap in quotes and escape existing quotes
			return "\"" + field.replace("\"", "\"\"") + "\"";
		}
		return field;
	}
	
	
	/*
	 * This method is used to covert
	 * 
	 * @java.util.Date
	 * 
	 * @java.sql.Timestamp object to MM/dd/yyyy string format
	 * 
	 */
	private String formatDate1(Object date) {
		
		if (date == null) {
			return "";
		}
		Instant instant;
		if (date instanceof Date) {
			instant = ((Date) date).toInstant();
		} else if (date instanceof Timestamp) {
			instant = ((Timestamp) date).toInstant();
		} else {
			log.error("Unsupported date type: {}", date.getClass().getName());
			return "";
		}
		LocalDate localDate = instant.atZone(ZoneId.of("America/New_York")).toLocalDate();
		return FORMATTER.format(localDate);
	}
	
	
	private String convertObjectToDateString(Object dateObj) {
		String fromattedDateStrig = "";
		if(dateObj instanceof Date) {
			Date date = (Date) dateObj;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			fromattedDateStrig =  sdf.format(date);
		}			
		return fromattedDateStrig;
	}
}



covert CSV file to excel 
Header should be BOLD and back backgroud of headers are green
add border of each
