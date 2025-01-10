import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatusLookupRepository extends JpaRepository<StatusLookup, Long> {

    List<String> findDistinctByOrderByStatusDesc();
}
