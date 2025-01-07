import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.aetna.asgwy.service.StateLkupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class StateLkupServiceImplTest {

    @Mock
    private StateLkupRepository stateLkupRepository;

    @InjectMocks
    private StateLkupServiceImpl stateLkupService;

    private StateLkup mockState1;
    private StateLkup mockState2;

    @BeforeEach
    void setUp() {
        // Mock states
        mockState1 = new StateLkup();
        mockState1.setStateCd("CA");
        mockState1.setStateNm("California");

        mockState2 = new StateLkup();
        mockState2.setStateCd("NY");
        mockState2.setStateNm("New York");
    }

    @Test
    void getAllStates_shouldReturnStateLkupDtos_whenRepositoryHasData() {
        // Arrange
        when(stateLkupRepository.findAll()).thenReturn(List.of(mockState1, mockState2));

        // Act
        List<StateLkupDto> result = stateLkupService.getAllStates();

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Result size should match repository data size");

        assertEquals("CA", result.get(0).getStateCd());
        assertEquals("California", result.get(0).getStateNm());

        assertEquals("NY", result.get(1).getStateCd());
        assertEquals("New York", result.get(1).getStateNm());

        verify(stateLkupRepository, times(1)).findAll();
    }

    @Test
    void getAllStates_shouldReturnEmptyList_whenRepositoryHasNoData() {
        // Arrange
        when(stateLkupRepository.findAll()).thenReturn(List.of());

        // Act
        List<StateLkupDto> result = stateLkupService.getAllStates();

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result list should be empty");

        verify(stateLkupRepository, times(1)).findAll();
    }

    @Test
    void getAllStates_shouldHandleNullResultFromRepositoryGracefully() {
        // Arrange
        when(stateLkupRepository.findAll()).thenReturn(null);

        // Act
        List<StateLkupDto> result = stateLkupService.getAllStates();

        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result list should be empty when repository returns null");

        verify(stateLkupRepository, times(1)).findAll();
    }

    @Test
    void getAllStates_shouldThrowException_whenRepositoryThrowsException() {
        // Arrange
        when(stateLkupRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> stateLkupService.getAllStates());
        assertEquals("Database error", exception.getMessage());

        verify(stateLkupRepository, times(1)).findAll();
    }
}
