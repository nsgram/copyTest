groupDTOList = entityManager.createNativeQuery(latestQuotesByType.getQuery(), "GetQuotes")
    .setParameter(1, quoteType)
    .setFirstResult(startIndex)
    .setMaxResults(page.getPageSize())
    .unwrap(org.hibernate.query.NativeQuery.class)
    .addEntity(DashboardDTO.class) // Map result to DashboardDTO
    .getResultList();

TypedQuery<DashboardDTO> query = entityManager.createQuery("SELECT new com.example.DashboardDTO(...) FROM YourEntity e WHERE ...", DashboardDTO.class);
groupDTOList = query.setParameter(1, quoteType)
                    .setFirstResult(startIndex)
                    .setMaxResults(page.getPageSize())
                    .getResultList();
