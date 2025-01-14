public class QuotesReportViewSpecification {

    public static Specification<QuotesReportView> buildDynamicQuery(
            LocalDate effectiveDateFrom,
            LocalDate effectiveDateTo,
            String statusDesc,
            String stateCd,
            LocalDate submissionDateFrom,
            LocalDate submissionDateTo) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (effectiveDateFrom != null && effectiveDateTo != null) {
                predicates.add(criteriaBuilder.between(
                        root.get("effectiveDt"), effectiveDateFrom, effectiveDateTo));
            }

            if (statusDesc != null) {
                predicates.add(criteriaBuilder.equal(root.get("statusDesc"), statusDesc));
            }

            if (stateCd != null) {
                predicates.add(criteriaBuilder.equal(root.get("stateCd"), stateCd));
            }

            if (submissionDateFrom != null && submissionDateTo != null) {
                predicates.add(criteriaBuilder.between(
                        root.get("submissionDt"), submissionDateFrom, submissionDateTo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
