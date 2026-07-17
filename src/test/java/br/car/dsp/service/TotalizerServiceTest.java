package br.car.dsp.service;

import br.car.dsp.dto.DetailByIdentifierResponse;
import br.car.dsp.dto.TotalizerFilterRequest;
import br.car.dsp.dto.TotalizerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TotalizerServiceTest {

	private TotalizerService totalizerService;

	@BeforeEach
	void setUp() {
		totalizerService = new TotalizerService();
	}

	@Test
	void getTotalizers_WhenFilterNull_ShouldReturnNationalTotalizers() {
		// When
		List<TotalizerResponse> result = totalizerService.getTotalizers(null);

		// Then
		assertEquals(5, result.size());
	}

	@Test
	void getTotalizers_WhenStateProvided_ShouldReturnFilteredTotalizers() {
		// Given
		TotalizerFilterRequest filter = new TotalizerFilterRequest();
		filter.setIdState("DF");

		// When
		List<TotalizerResponse> result = totalizerService.getTotalizers(filter);

		// Then
		assertFalse(result.isEmpty());
		assertNotNull(result.getFirst().name());
	}

	@Test
	void getDetailByIdentifier_WhenKnown_ShouldReturnDetail() {
		// When
		DetailByIdentifierResponse result =
				totalizerService.getDetailByIdentifier("DF123456789012");

		// Then
		assertEquals("DF123456789012", result.codeProperty());
		assertEquals("Distrito Federal", result.nameState());
	}

	@Test
	void getDetailByIdentifier_WhenUnknown_ShouldThrowNotFound() {
		// When & Then
		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> totalizerService.getDetailByIdentifier("UNKNOWN")
		);

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}
}
