import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuotesControllerTest {

    @Mock
    private QuotesService quotesService;

    @InjectMocks
    private QuotesController quotesController;

    private final ObjectMapper objMapper = new ObjectMapper();

    @Test
    void testGetAllGroups() throws Exception {
        // Arrange
        String quoteType = "ILLUS";
        String stringKey = "{\"performerId\": \"2993443\", \"performerAgencyId\": \"288888\", \"performerType\": \"EXTERNAL\", \"performerRole\": \"superuser\", \"performerPrivilege\": \"SHARED\"}";

        KeyIdentifiers keyIdentifiers = objMapper.readValue(stringKey, KeyIdentifiers.class);
        keyIdentifiers.setPerformerRole(keyIdentifiers.getPerformerRole().toUpperCase(Locale.ENGLISH));

        DashboardResponse expectedResponse = new DashboardResponse();
        when(quotesService.getRecentQuotesForEachGroup(quoteType, keyIdentifiers)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<DashboardResponse> response = quotesController.getRecentQuotesForEachGroup(quoteType, stringKey);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        then(quotesService).should(atLeastOnce()).getRecentQuotesForEachGroup(quoteType, keyIdentifiers);
    }
}
