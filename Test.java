SELECT DISTINCT grp.group_nm,
    grp.employer_id_nbr,
    grp.test_group_ind,
    qut.group_zip_cd,
    qut.effective_dt,
    qut.eligible_entrd_cnt,
    qut.union_emp_ind,
    qut.union_emp_cnt,
    qut.quote_status_cd,
    qut.quote_id,
    qut.group_state_cd,
    qp.product_cd,
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
    css.concess_req_status_cd,
    css.concess_req_pct,
    css.concess_req_reason_txt,
    css.create_dts,
    comm.comment_txt,
    qsa.quote_sa_full_nm,
    qsa.quote_sa_npn,
    qsa.quote_sa_agency_nm,
    qsa.quote_sa_cfo_nm,
    qsa.quote_sa_cfo_loc,
    qga.quote_ga_sa_cfo_loc,
    qga.quote_ga_sa_cfo_nm,
    qga.quote_ga_agency_nm,
    qse.quote_sales_exec_first_name,
    qse.quote_sales_exec_last_name,
    stat.status_desc,
    qut.submission_dt
   -- row_number() OVER() as row_id
   FROM asgwy_db_schema.groups grp
     JOIN asgwy_db_schema.quote qut ON grp.group_id = qut.group_id
     JOIN asgwy_db_schema.status_lkup stat ON stat.status_cd::text = qut.quote_status_cd::text
     JOIN asgwy_db_schema.quote_selling_agent qsa ON qsa.group_id = grp.group_id
     JOIN asgwy_db_schema.quote_coverage qcvr ON qcvr.quote_id = qut.quote_id
     LEFT JOIN asgwy_db_schema.quote_product qp ON qp.quote_id = qut.quote_id
     LEFT JOIN asgwy_db_schema.quote_sales_exec qse ON qse.group_id = grp.group_id
     LEFT JOIN ( SELECT quote_concession.concess_group_id,
            quote_concession.concess_req_quote_id,
            quote_concession.concess_req_status_cd,
            quote_concession.concess_req_pct,
            quote_concession.concess_req_reason_txt,
            max(quote_concession.quote_concession_id) AS max_consession,
            quote_concession.create_dts
           FROM asgwy_db_schema.quote_concession
          GROUP BY quote_concession.concess_group_id, quote_concession.concess_req_quote_id, quote_concession.concess_req_status_cd, quote_concession.concess_req_pct, quote_concession.concess_req_reason_txt, quote_concession.create_dts) css ON grp.group_id = css.concess_group_id
     LEFT JOIN asgwy_db_schema.quote_comment comm ON comm.quote_id = qut.quote_id
     LEFT JOIN asgwy_db_schema.quote_general_agency qga ON qga.group_id = grp.group_id;
