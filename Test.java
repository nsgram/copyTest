@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class QuotesReportEntity {
	
	
	@Id
	@Column(name = "dummy_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long dummyId;

	// Groups
	@Column(name = "group_nm")
	private String groupNm;
	@Column(name = "employer_id_nbr")
	private String employerIdNbr;
	@Column(name = "test_group_ind")
	private Boolean testGroupInd;

	// Quotes
	@Column(name = "quote_id")
	private Long quoteId;
	@Column(name = "quote_status_cd")
	private String quoteStatusCd;
	@Column(name = "effective_dt")
	private Date effectiveDt;
	@Column(name = "group_zip_cd")
	private String groupZipCd;
	@Column(name = "group_state_cd")
	private String groupStateCd;
	@Column(name = "eligible_entrd_cnt")
	private Integer eligibleEntrdCnt;
	@Column(name = "union_emp_ind")
	private String unionEmpInd;
	@Column(name = "union_emp_cnt")
	private Integer unionEmpCnt;

	// quote_product
	@Column(name = "product_cd")
	private String productCd;

	// Coverage
	@Column(name = "group_loc_addr_line1_txt")
	private String groupLocAddrLine1Txt;
	@Column(name = "group_loc_addr_line2_txt")
	private String groupLocAddrLine2Txt;
	@Column(name = "group_loc_city_nm")
	private String groupLocCityNm;
	@Column(name = "curr_carrier_typ_desc")
	private String currCarrierTypDesc;
	@Column(name = "curr_med_carrier_nm")
	private String currMedCarrierNm;
	@Column(name = "grp_mewa_ind")
	private String grpMewaAssoc;
	@Column(name = "aetna_peo_ind")
	private String aetnaPeoInd;
	@Column(name = "contract_period_mo_nbr")
	private Integer contractPeriodMoNbr;
	@Column(name = "tot_avg_emp_cnt")
	private Integer totAvgEmpCnt;
	@Column(name = "sic_cd")
	private String sicCd;
	@Column(name = "sic_nm")
	private String sicNm;
	@Column(name = "def_broker_fee_amt")
	private BigDecimal defBrokerFeeAmt;
	@Column(name = "participation_cnt")
	private Integer participationCnt;
	@Column(name = "eligible_dervd_cnt")
	private Integer eligibleDervdCnt;
	@Column(name = "waiver_cnt")
	private Integer waiverCnt;
	@Column(name = "eligible_ret_cnt")
	private Integer eligibleRetCnt;
	@Column(name = "cobra_emp_cnt")
	private Integer cobraEmpCnt;
	@Column(name = "participation_pct")
	private BigDecimal participationPct;
	@Column(name = "ft_eqvlnt_cnt")
	private Integer ftEqvlntCnt;
	@Column(name = "erisa_cd")
	private String erisaCd;
	@Column(name = "curr_tpa_nm")
	private String currTpaNm;

	// quote_concession
	@Column(name = "concess_req_status_cd")
	private String concessReqStatusCd;
	@Column(name = "concess_req_pct")
	private BigDecimal concessReqPct;
	@Column(name = "concess_req_reason_txt")
	private String concessReqReasonTxt;
	@Column(name = "conn_create_dts")
	private Timestamp conn_create_dts;

	// QuoteDocument
	@Column(name = "comment_txt")
	private String commentTxt;

	// quote_selling_agent
	@Column(name = "quote_sa_full_nm")
	private String quotesSAFullNm;
	@Column(name = "quote_sa_npn")
	private String quoteSANPN;
	@Column(name = "quote_sa_agency_nm")
	private String quoteSAAgencyNm;
	@Column(name = "quote_sa_cfo_nm")
	private String quoteSACfoNm;
	@Column(name = "quote_sa_cfo_loc")
	private String quoteSACfoLoc;

	// quote_general_agency
	@Column(name = "quote_ga_agency_nm")
	private String quoteGAAgencyNm;
	@Column(name = "quote_ga_sa_cfo_nm")
	private String quoteGASACfoNm;
	@Column(name = "quote_ga_sa_cfo_loc")
	private String quoteGASACfoLoc;

	// quote_sales_exec
	@Column(name = "quote_sales_exec_first_name")
	private String quoteSalesExecFirstName;
	@Column(name = "quote_sales_exec_last_name")
	private String quoteSalesExecLastName;

	// Below attribute are Not used in report but required for JPA
	@Column(name = "status_desc")
	private String statusDesc;

	@Column(name = "create_dts")
	private Date createDts;
}
	I have below rowpappert but it getting null vaules for somet of columns
	
	//Quote report
	public List<QuotesReportEntity> getReportsDataForCSV(RequestDetailsDTO requestDetailsDTO) throws SQLException {
		StringBuilder sqlQuery = new StringBuilder();
		List<String> queryArgs = new ArrayList<>();
		sqlQuery.append("select * from quotes_report_view where 1=1 ");
		if (requestDetailsDTO.getFromEffectiveDt() != null && !requestDetailsDTO.getFromEffectiveDt().isEmpty()) {
			sqlQuery.append(" AND effective_dt BETWEEN ?::date and ?::date ");
			queryArgs.add(requestDetailsDTO.getFromEffectiveDt());
			queryArgs.add(requestDetailsDTO.getToEffectiveDt());
		}
		if (requestDetailsDTO.getFromSubmissionDt() != null && !requestDetailsDTO.getFromSubmissionDt().isEmpty()) {
			sqlQuery.append(" AND submission_dt BETWEEN ?::date and ?::date ");
			queryArgs.add(requestDetailsDTO.getFromSubmissionDt());
			queryArgs.add(requestDetailsDTO.getToSubmissionDt());
		}
		if (requestDetailsDTO.getStateCd() != null && !requestDetailsDTO.getStateCd().isEmpty()) {
			sqlQuery.append(" AND group_state_cd =?");
			queryArgs.add(requestDetailsDTO.getStateCd());
		}
		if (requestDetailsDTO.getStatusDesc() != null && !requestDetailsDTO.getStatusDesc().isEmpty()) {
			sqlQuery.append(" AND status_desc = ?");
			queryArgs.add(requestDetailsDTO.getStatusDesc());
		}
		Object[] preparedStatementArgs = new Object[queryArgs.size()];
		for (int i = 0; i < preparedStatementArgs.length; i++) {
			preparedStatementArgs[i] = queryArgs.get(i);
		}
		log.info(" CSV sqlQuery:{}", sqlQuery.toString());
		return jdbcTemplate.query(sqlQuery.toString(), new BeanPropertyRowMapper<>(QuotesReportEntity.class),
				preparedStatementArgs);
	}
