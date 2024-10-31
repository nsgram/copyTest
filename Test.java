select distinct grp.group_id,
                grp.group_nm,
                quote.effective_dt,
                quote.submission_dt,
                quote.update_dts,
                quote.smart_quote_id,
                quote.quote_id,
                coverage.eligible_dervd_cnt,
                quote.eligible_entrd_cnt,
                CASE
                    WHEN coverage.eligible_dervd_cnt IS NOT NULL
                         AND coverage.eligible_dervd_cnt > 0 THEN coverage.eligible_dervd_cnt
                    ELSE quote.eligible_entrd_cnt
                END AS eligible_lives,
                quote.quote_type_cd,
                grp.group_typ_cd,
                quote.group_state_cd,
                quote.create_usr_id,
                quote.quote_status_cd,
                status.status_desc,
                status.status_disp_nm,
                status.status_typ_cd
from asgwy_db_schema.groups grp
INNER JOIN asgwy_db_schema.quote quote ON grp.group_id=quote.group_id
LEFT JOIN asgwy_db_schema.quote_coverage coverage ON quote.quote_id = coverage.quote_id
LEFT JOIN asgwy_db_schema.status_lkup status ON quote.quote_status_cd = status.status_cd
LEFT JOIN
    (select *
     from asgwy_db_schema.quote
     where expired_dt IS NULL
         and quote_status_cd NOT LIKE 'DND_%' ) quote2 ON (quote.group_id = quote2.group_id)
AND quote.update_dts < quote2.update_dts
LEFT JOIN asgwy_db_schema.quote_contrbr contrbr ON quote.quote_id =contrbr.quote_id
LEFT JOIN asgwy_db_schema.quote_selling_agent sellingAgent ON grp.group_id = sellingAgent.group_id
WHERE (contrbr.quote_submtr_id='247791'
       OR sellingAgent.QUOTE_SA_PROXY_ID ='247791')
    AND quote.quote_type_cd ='MEDUW'
    AND quote2.update_dts IS NULL
    AND quote.expired_dt IS NULL
    AND status.status_typ_cd = 'QUOTE_STATUS'
    AND quote.quote_status_cd NOT LIKE 'DND_%'
UNION
select distinct grp.group_id,
                grp.group_nm,
                quote.effective_dt,
                quote.submission_dt,
                quote.update_dts,
                quote.smart_quote_id,
                quote.quote_id,
                coverage.eligible_dervd_cnt,
                quote.eligible_entrd_cnt,
                CASE
                    WHEN coverage.eligible_dervd_cnt IS NOT NULL
                         AND coverage.eligible_dervd_cnt > 0 THEN coverage.eligible_dervd_cnt
                    ELSE quote.eligible_entrd_cnt
                END AS eligible_lives,
                quote.quote_type_cd,
                grp.group_typ_cd,
                quote.group_state_cd,
                quote.create_usr_id,
                quote.quote_status_cd,
                status.status_desc,
                status.status_disp_nm,
                status.status_typ_cd
from asgwy_db_schema.groups grp
INNER JOIN asgwy_db_schema.quote quote ON grp.group_id=quote.group_id
LEFT JOIN asgwy_db_schema.quote_coverage coverage ON quote.quote_id = coverage.quote_id
LEFT JOIN asgwy_db_schema.status_lkup status ON quote.quote_status_cd = status.status_cd
LEFT JOIN
    (select *
     from asgwy_db_schema.quote
     where expired_dt IS NULL
         and quote_status_cd NOT LIKE 'DND_%' ) quote2 ON (quote.group_id = quote2.group_id)
AND quote.update_dts < quote2.update_dts
LEFT JOIN asgwy_db_schema.quote_selling_agent sellingAgent ON grp.group_id = sellingAgent.group_id
LEFT JOIN asgwy_db_schema.quote_general_agency genAgecy ON grp.group_id = genAgecy.group_id
WHERE (sellingAgent.QUOTE_SA_PROXY_ID ='294266'
       OR genAgecy.QUOTE_GA_PROXY_ID='294266')
    AND quote.quote_type_cd ='MEDUW'
    AND quote2.update_dts IS NULL
    AND quote.expired_dt IS NULL
    AND status.status_typ_cd = 'QUOTE_STATUS'
    AND quote.quote_status_cd NOT LIKE 'DND_%'
ORDER BY quote.update_dts DESC;
