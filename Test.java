import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface StatusLookupRepository extends CrudRepository<StatusLookup, Long> {
    // Derived query for your specific use case
    List<String> findDistinctByStatusTypeCodeOrderByStatusDesc(String statusTypeCode);
}
