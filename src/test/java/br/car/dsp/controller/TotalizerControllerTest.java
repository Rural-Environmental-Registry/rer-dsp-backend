package br.car.dsp.controller;

import br.car.dsp.dto.DetailByIdentifierResponse;
import br.car.dsp.dto.TotalizerFilterRequest;
import br.car.dsp.dto.TotalizerResponse;
import br.car.dsp.service.TotalizerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TotalizerControllerTest {

	@Mock
	private TotalizerService totalizerService;

	@InjectMocks
	private TotalizerController totalizerController;

	private TotalizerFilterRequest filter;
	private List<TotalizerResponse> totalizers;
	private DetailByIdentifierResponse detail;

	@BeforeEach
	void setUp() {
		filter = new TotalizerFilterRequest();
		filter.setIdState("DF");

		totalizers = List.of(
				new TotalizerResponse("Imóveis cadastrados", "REGISTERED_AREA", 10.0, "ha", 20L, "un.")
		);

		detail = new DetailByIdentifierResponse(
				"DF123456789012",
				"-15.79",
				"-47.88",
				"-15.79, -47.88",
				"DF",
				"Distrito Federal",
				"Brasília",
				new BigDecimal("2.5"),
				"10/01/2020",
				"15/06/2024",
				new BigDecimal("120.50"),
				1001,
				null
		);
	}

	@Test
	void getTotalizerByStateOrCity_ShouldDelegateToService() {
		// Given
		when(totalizerService.getTotalizers(filter)).thenReturn(totalizers);

		// When
		List<TotalizerResponse> result = totalizerController.getTotalizerByStateOrCity(filter);

		// Then
		assertEquals(totalizers, result);
		verify(totalizerService).getTotalizers(filter);
	}

	@Test
	void getDetailsByIdentifier_ShouldDelegateToService() {
		// Given
		when(totalizerService.getDetailByIdentifier("DF123456789012")).thenReturn(detail);

		// When
		DetailByIdentifierResponse result =
				totalizerController.getDetailsByIdentifier("DF123456789012");

		// Then
		assertEquals(detail, result);
		verify(totalizerService).getDetailByIdentifier("DF123456789012");
	}
}
