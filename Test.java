


		public class QuotesReportRequest {
		private Date fromEffectiveDt;
		private Date toEffectiveDt;
		private String statusDesc;
		private String stateCd;
		private Date fromSubmissionDt;
		private Date toSubmissionDt;
		}


		i have below jsonRequest

		{
  		"fromEffectiveDt": "2024-01-17T11:54:10.797Z",
  		"toEffectiveDt": "2025-01-17T11:54:10.797Z",
  		"statusDesc": "",
  		"stateCd": "",
  		"fromSubmissionDt": "2024-01-17T11:54:10.797Z",
  		"toSubmissionDt": "2025-01-17T11:54:10.797Z"
		}



		while I coverted into 
		ObjectMapper mapper = new ObjectMapper();
		String jsonRequest = mapper.writeValueAsString(quotesReportRequest);
		gettting below json object
		{
    		"stateCd": "",
    		"statusDesc": "",
   		"toEffectiveDt": 1737111112878,
    		"toSubmissionDt": 1737111112878,
   		"fromEffectiveDt": 1705488712878,
    		"fromSubmissionDt": 1705488712878
		}

		if date is not null then I want set date as MM/dd/YYYY
