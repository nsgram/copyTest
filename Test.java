@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SFDCReportEntity {
	
	@Id
	@Column(name = "dummy_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long dummyId;

	@Column(name = "status_desc")
	private String status_desc;

	@Column(name = "group_state_cd")
	private String group_state_cd;//not used ?

	@Column(name = "create_dts")
	private Date create_dts;//not used ?
	
	// Quote Performer
	@Column(name = "quote_perfmr_type_cd")
	private String quote_perfmr_type_cd;
	@Column(name = "quote_perfmr_first_nm")
	private String quote_perfmr_first_nm;
	@Column(name = "quote_perfmr_last_nm ")
	private String quote_perfmr_last_nm;
	@Column(name = "quote_perfmr_address")
	private String quote_perfmr_address;
	@Column(name = "quote_perfmr_email_txt")
	private String quote_perfmr_email_txt;

	// Quote Submitter
	@Column(name = "quote_submtr_type_cd")
	private String quote_submtr_type_cd;
	@Column(name = "quote_submtr_first_nm")
	private String quote_submtr_first_nm;
	@Column(name = "quote_submtr_last_nm ")
	private String quote_submtr_last_nm;
	@Column(name = "quote_submtr_address")
	private String quote_submtr_address;
	@Column(name = "quote_submtr_email_txt")
	private String quote_submtr_email_txt;
	@Column(name = "quote_submtr_phone_nbr")
	private String quote_submtr_phone_nbr;

	// Selling Agents
	@Column(name = "quote_sa_full_nm")
	private String quote_sa_full_nm;
	@Column(name = "quote_sa_email_txt")
	private String quote_sa_email_txt;
	@Column(name = "quote_sa_phone_nbr")
	private String quote_sa_phone_nbr;

	// Firm
	@Column(name = "quote_sa_agency_nm")
	private String quote_sa_agency_nm;
	@Column(name = "quote_sa_agency_addr")
	private String quote_sa_agency_addr;
	@Column(name = "quote_sa_agency_email_txt")
	private String quote_sa_agency_email_txt;
	@Column(name = "quote_sa_agency_phone_nbr")
	private String quote_sa_agency_phone_nbr;

	// General Agents
	@Column(name = "quote_ga_sa_nm")
	private String quote_ga_sa_nm;
	@Column(name = "quote_ga_sa_email")
	private String quote_ga_sa_email;
	@Column(name = "quote_ga_sa_phone")
	private String quote_ga_sa_phone;
	
	@Column(name = "quote_ga_agency_nm")
	private String quote_ga_agency_nm;
	@Column(name = "quote_ga_agency_email_txt")
	private String quote_ga_agency_email_txt;
	@Column(name = "quote_ga_agency_addr")
	private String quote_ga_agency_addr;
	@Column(name = "quote_ga_agency_phone_nbr")
	private String quote_ga_agency_phone_nbr;

	// Group
	@Column(name = "group_id")
	private Integer group_id;
	@Column(name = "smart_group_id")
	private Integer smart_group_id;
	@Column(name = "group_nm")
	private String group_nm;
	@Column(name = "prior_psu_id")
	private String prior_psu_id;
	@Column(name = "employer_id_nbr")
	private String employer_id_nbr;
	@Column(name = "test_group_ind")
	private Boolean test_group_ind;
	@Column(name = "group_nm_upt_ind")
	private String group_nm_upt_ind;
	
	// Quote Coverage
	@Column(name = "quote_id")
	private Integer quote_id;
	@Column(name = "smart_quote_id")
	private Integer smart_quote_id;
	@Column(name = "group_zip_cd")
	private String group_zip_cd;
	@Column(name = "group_loc_city_nm")
	private String group_loc_city_nm;
	@Column(name = "state_nm")
	private String state_nm;
	


	// GroupWorkLocation
	@Column(name = "group_loc_addr_line1_txt")
	private String group_loc_addr_line1_txt;
	@Column(name = "group_loc_addr_line2_txt")
	private String group_loc_addr_line2_txt;
	
	@Column(name = "eligible_entrd_cnt")
	private Integer eligible_entrd_cnt;
	@Column(name = "union_emp_cnt")
	private Integer union_emp_cnt;
	@Column(name = "enrlng_emp_cnt")
	private Integer enrlng_emp_cnt;
	@Column(name = "waiver_cnt")
	private Integer waiver_cnt;
	@Column(name = "eligible_ret_cnt")
	private Integer eligible_ret_cnt;
	@Column(name = "cobra_emp_cnt")
	private Integer cobra_emp_cnt;
	@Column(name = "ft_eqvlnt_cnt")
	private String ft_eqvlnt_cnt;
	@Column(name = "tot_avg_emp_cnt")
	private Integer tot_avg_emp_cnt;
	@Column(name = "sic_cd")
	private String sic_cd;
	@Column(name = "effective_dt")
	private Date effective_dt;
	@Column(name = "quote_type_cd")
	private String quote_type_cd;

	// Product Type
	@Column(name = "product_cd")
	private String product_cd;
	@Column(name = "curr_med_carrier_nm")
	private String curr_med_carrier_nm;
	@Column(name = "doc_claimsexp_indicator")//new column
	private String doc_claimsexp_indicator;
	@Column(name = "renewal_dt")
	private Date renewal_dt;
	@Column(name = "sgr_ind")
	private String sgr_ind;
	@Column(name = "participation_pct")
	private BigDecimal participation_pct;
	@Column(name = "grp_mewa_ind")
	private String grp_mewa_ind;

	@Column(name = "curr_carrier_typ_desc")
	private String curr_carrier_typ_desc;
	@Column(name = "erisa_cd")
	private String erisa_cd;
	@Column(name = "contract_period_mo_nbr")
	private Integer contract_period_mo_nbr;
	@Column(name = "quote_sales_exec_first_name")
	private String quote_sales_exec_first_name;
	@Column(name = "quote_sales_exec_last_name")
	private String quote_sales_exec_last_name;
	
	@Column(name = "ret_thold_ind")
	private String ret_thold_ind;
	@Column(name = "def_broker_fee_amt")
	private BigDecimal def_broker_fee_amt;
	@Column(name = "adj_broker_fee_amt")
	private BigDecimal adj_broker_fee_amt;
	@Column(name = "raf_type")
	private String raf_type;
	@Column(name = "doc_IMQ_indicator")//new column
	private String doc_IMQ_indicator;
	@Column(name = "doc_SBC_indicator")//new column
	private String doc_SBC_indicator;
	@Column(name = "doc_RC_indicator")//new column
	private String doc_RC_indicator;
	@Column(name = "rCDocUploadedIndicator") 
	private String rCDocUploadedIndicator;
	@Column(name = "concess_req_pct")
	private BigDecimal concess_req_pct;
	@Column(name = "concess_req_reason_txt")
	private String concess_req_reason_txt;
	@Column(name = "quote_concess_doc_ind")
	private Character quote_concess_doc_ind;
	@Column(name = "aetna_peo_ind")
	private String aetna_peo_ind;

	@Column(name = "doc_nm")//startW
	private String doc_nm;
	@Column(name = "doc_category")
	private String doc_category;
	@Column(name = "doc_subcategory")
	private String doc_subcategory;
	
	//CFO
	@Column(name = "quote_sa_CFO_nm")
	private String quote_sa_CFO_nm;
	@Column(name = "quote_sa_CFO_loc")
	private String quote_sa_CFO_loc;
	
	@Column(name="quote_ga_sa_CFO_nm")
	private String quote_ga_sa_CFO_nm;

	@Column(name="quote_ga_sa_CFO_loc")
	private String generalAgencyCFOLocation;

}
