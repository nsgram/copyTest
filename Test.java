import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class QuotesReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public QuotesReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<QuotesReportEntity> getReportsDataForCSV(RequestDetailsDTO requestDetailsDTO) throws SQLException {
        StringBuilder sqlQuery = new StringBuilder();
        List<Object> queryArgs = new ArrayList<>();

        sqlQuery.append("SELECT * FROM quotes_report_view WHERE 1=1");

        if (requestDetailsDTO.getFromEffectiveDt() != null && !requestDetailsDTO.getFromEffectiveDt().isEmpty()) {
            sqlQuery.append(" AND effective_dt BETWEEN CAST(? AS DATE) AND CAST(? AS DATE)");
            queryArgs.add(requestDetailsDTO.getFromEffectiveDt());
            queryArgs.add(requestDetailsDTO.getToEffectiveDt());
        }
        if (requestDetailsDTO.getFromSubmissionDt() != null && !requestDetailsDTO.getFromSubmissionDt().isEmpty()) {
            sqlQuery.append(" AND submission_dt BETWEEN CAST(? AS DATE) AND CAST(? AS DATE)");
            queryArgs.add(requestDetailsDTO.getFromSubmissionDt());
            queryArgs.add(requestDetailsDTO.getToSubmissionDt());
        }
        if (requestDetailsDTO.getStateCd() != null && !requestDetailsDTO.getStateCd().isEmpty()) {
            sqlQuery.append(" AND group_state_cd = ?");
            queryArgs.add(requestDetailsDTO.getStateCd());
        }
        if (requestDetailsDTO.getStatusDesc() != null && !requestDetailsDTO.getStatusDesc().isEmpty()) {
            sqlQuery.append(" AND status_desc = ?");
            queryArgs.add(requestDetailsDTO.getStatusDesc());
        }

        log.info("Generated SQL Query: {}", sqlQuery);

        return jdbcTemplate.query(sqlQuery.toString(), (rs, rowNum) -> mapResultSetToEntity(rs), queryArgs.toArray());
    }

    private QuotesReportEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        QuotesReportEntity entity = new QuotesReportEntity();
        
        // Mapping fields from ResultSet to entity
        entity.setDummyId(rs.getLong("dummy_id"));
        entity.setGroupNm(rs.getString("group_nm"));
        entity.setEmployerIdNbr(rs.getString("employer_id_nbr"));
        entity.setTestGroupInd(rs.getBoolean("test_group_ind"));
        entity.setQuoteId(rs.getLong("quote_id"));
        entity.setQuoteStatusCd(rs.getString("quote_status_cd"));
        entity.setEffectiveDt(rs.getDate("effective_dt"));
        entity.setGroupZipCd(rs.getString("group_zip_cd"));
        entity.setGroupStateCd(rs.getString("group_state_cd"));
        entity.setEligibleEntrdCnt(rs.getInt("eligible_entrd_cnt"));
        entity.setUnionEmpInd(rs.getString("union_emp_ind"));
        entity.setUnionEmpCnt(rs.getInt("union_emp_cnt"));
        entity.setProductCd(rs.getString("product_cd"));
        entity.setGroupLocAddrLine1Txt(rs.getString("group_loc_addr_line1_txt"));
        entity.setGroupLocAddrLine2Txt(rs.getString("group_loc_addr_line2_txt"));
        entity.setGroupLocCityNm(rs.getString("group_loc_city_nm"));
        entity.setCurrCarrierTypDesc(rs.getString("curr_carrier_typ_desc"));
        entity.setCurrMedCarrierNm(rs.getString("curr_med_carrier_nm"));
        entity.setGrpMewaAssoc(rs.getString("grp_mewa_ind"));
        entity.setAetnaPeoInd(rs.getString("aetna_peo_ind"));
        entity.setContractPeriodMoNbr(rs.getInt("contract_period_mo_nbr"));
        entity.setTotAvgEmpCnt(rs.getInt("tot_avg_emp_cnt"));
        entity.setSicCd(rs.getString("sic_cd"));
        entity.setSicNm(rs.getString("sic_nm"));
        entity.setDefBrokerFeeAmt(rs.getBigDecimal("def_broker_fee_amt"));
        entity.setParticipationCnt(rs.getInt("participation_cnt"));
        entity.setEligibleDervdCnt(rs.getInt("eligible_dervd_cnt"));
        entity.setWaiverCnt(rs.getInt("waiver_cnt"));
        entity.setEligibleRetCnt(rs.getInt("eligible_ret_cnt"));
        entity.setCobraEmpCnt(rs.getInt("cobra_emp_cnt"));
        entity.setParticipationPct(rs.getBigDecimal("participation_pct"));
        entity.setFtEqvlntCnt(rs.getInt("ft_eqvlnt_cnt"));
        entity.setErisaCd(rs.getString("erisa_cd"));
        entity.setCurrTpaNm(rs.getString("curr_tpa_nm"));
        entity.setConcessReqStatusCd(rs.getString("concess_req_status_cd"));
        entity.setConcessReqPct(rs.getBigDecimal("concess_req_pct"));
        entity.setConcessReqReasonTxt(rs.getString("concess_req_reason_txt"));
        entity.setConn_create_dts(rs.getTimestamp("conn_create_dts"));
        entity.setCommentTxt(rs.getString("comment_txt"));
        entity.setQuotesSAFullNm(rs.getString("quote_sa_full_nm"));
        entity.setQuoteSANPN(rs.getString("quote_sa_npn"));
        entity.setQuoteSAAgencyNm(rs.getString("quote_sa_agency_nm"));
        entity.setQuoteSACfoNm(rs.getString("quote_sa_cfo_nm"));
        entity.setQuoteSACfoLoc(rs.getString("quote_sa_cfo_loc"));
        entity.setQuoteGAAgencyNm(rs.getString("quote_ga_agency_nm"));
        entity.setQuoteGASACfoNm(rs.getString("quote_ga_sa_cfo_nm"));
        entity.setQuoteGASACfoLoc(rs.getString("quote_ga_sa_cfo_loc"));
        entity.setQuoteSalesExecFirstName(rs.getString("quote_sales_exec_first_name"));
        entity.setQuoteSalesExecLastName(rs.getString("quote_sales_exec_last_name"));
        entity.setStatusDesc(rs.getString("status_desc"));
        entity.setCreateDts(rs.getDate("create_dts"));

        return entity;
    }
}
