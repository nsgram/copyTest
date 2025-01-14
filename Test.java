create VIEW asgwy_db_schema.QUOTES_REPORT_VIEW AS 

select grp.group_id,
	   grp.group_nm,
       grp.employer_id_nbr,
       grp.test_group_ind,
       qut.group_zip_cd,
       st.state_nm,
       qut.effective_dt,
       qut.eligible_entrd_cnt,
       qut.union_emp_ind,
       qut.union_emp_cnt,
       qc.concess_req_quote_id,
       qc.concess_req_status_cd,
       qc.concess_req_pct,
       qc.concess_req_reason_txt,
       qcvr.group_loc_addr_line1_txt,
       qcvr.group_loc_addr_line2_txt,
       qcvr.group_loc_city_nm,
       qcvr.curr_carrier_typ_desc,
       qcvr.curr_med_carrier_nm,
       qcvr.grp_mewa_ind,
       qcvr.aetna_peo_ind,
       qcvr.contract_period_mo_nbr,
       qcvr.tot_avg_emp_cnt,
       qcvr.sic_cd,
       qcvr.sic_nm,
       qcvr.def_broker_fee_amt,
       qcvr.participation_cnt,
       qcvr.eligible_dervd_cnt,
       qcvr.waiver_cnt,
       qcvr.eligible_ret_cnt,
       qcvr.cobra_emp_cnt,
       qcvr.participation_pct,
       qcvr.ft_eqvlnt_cnt,
       qcvr.erisa_cd,
       qcvr.curr_tpa_nm,
       qcvr.aetna_sales_exe,
       qut.quote_status_cd,
       qut.submission_dt,
       st_lkup.status_desc,
	   st.state_cd
from asgwy_db_schema.groups Grp
join asgwy_db_schema.quote qut on grp.group_id=qut.group_id
join asgwy_db_schema.quote_concession QC on grp.group_id= qc.concess_group_id
join asgwy_db_schema.quote_coverage qcvr on qut.quote_id= qcvr.quote_id
join asgwy_db_schema.state_lkup st on st.state_cd = qut.group_state_cd
join asgwy_db_schema.status_lkup st_lkup on st_lkup.status_cd = qut.quote_status_cd;
	
	
I have above view

write a view in post gress db where clause should be 

effective_dt is not null then add in add in condition
quote_status_cd is not null then add in add in condition
effective_dt is not null then add in add in condition
effective_dt is not null then add in add in condition


if only one condition is not null then add it into where clause
add all above condition dynamically 

I am using this view into spring boot rest api with JPA
