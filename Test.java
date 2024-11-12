if (StringUtils.isNotBlank(quoteType)) {
				if (!BROKER.equalsIgnoreCase(keyIdentifiers.getUserrole())) {

					LatestQuotesByType latestQuotesByType = new LatestQuotesByType(dashBoardRequest,sortInput);
					groupDTOList = entityManager.createNativeQuery(latestQuotesByType.getQuery(), "GetQuotes")
							.setParameter(1, quoteType).setFirstResult(startIndex).setMaxResults(page.getPageSize()).getResultList();
				}

//Type safety: The expression of type List needs unchecked conversion to conform to List<DashboardDTO>
