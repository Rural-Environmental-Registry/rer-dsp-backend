package br.car.dsp.service;

import br.car.dsp.dto.AreaOfInterestAggregate;
import br.car.dsp.dto.AreaOfInterestMeasuresConfigResponse;
import br.car.dsp.dto.DetailByIdentifierResponse;
import br.car.dsp.dto.HomeKpisConfigResponse;
import br.car.dsp.dto.InstallationConfigResponse;
import br.car.dsp.dto.KpiCardConfigResponse;
import br.car.dsp.dto.TotalizerFilterRequest;
import br.car.dsp.dto.TotalizerResponse;
import br.car.dsp.model.AreaOfInterest;
import br.car.dsp.model.TerritoryLevel2;
import br.car.dsp.model.TerritoryLevel3;
import br.car.dsp.repository.AreaOfInterestRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TotalizerServiceTest {

	@Mock
	private AreaOfInterestRepository areaOfInterestRepository;

	@Mock
	private InstallationConfigService installationConfigService;

	@InjectMocks
	private TotalizerService totalizerService;

	@BeforeEach
	void stubDefaultInstallationConfig() {
		lenient().when(installationConfigService.getInstallationConfig())
				.thenReturn(installationConfig(
						"Registered properties",
						"un.",
						"ha"
				));
	}

	@Test
	void getTotalizers_WhenFilterNull_ShouldUseRealAreaOfInterestAggregate() {
		when(areaOfInterestRepository.aggregateAll())
				.thenReturn(aggregate(508L, new BigDecimal("160652.4")));

		List<TotalizerResponse> result = totalizerService.getTotalizers(null);

		assertEquals(5, result.size());
		TotalizerResponse primary = result.stream()
				.filter(item -> TotalizerService.CODE_AREA_OF_INTEREST.equals(item.code()))
				.findFirst()
				.orElseThrow();
		assertEquals("Registered properties", primary.name());
		assertEquals(508.0, primary.value());
		assertEquals(160652L, primary.subItemValue());
		assertEquals("un.", primary.unitOfMeasurement());
		assertEquals("ha", primary.subItemName());
		verify(areaOfInterestRepository).aggregateAll();
	}

	@Test
	void getTotalizers_WhenStateProvided_ShouldAggregateByLevel2() {
		when(areaOfInterestRepository.aggregateByLevel2Id("DF"))
				.thenReturn(aggregate(12L, new BigDecimal("100.6")));

		TotalizerFilterRequest filter = new TotalizerFilterRequest();
		filter.setLevel2Id("DF");

		List<TotalizerResponse> result = totalizerService.getTotalizers(filter);

		TotalizerResponse primary = result.stream()
				.filter(item -> TotalizerService.CODE_AREA_OF_INTEREST.equals(item.code()))
				.findFirst()
				.orElseThrow();
		assertEquals(12.0, primary.value());
		assertEquals(101L, primary.subItemValue());
		verify(areaOfInterestRepository).aggregateByLevel2Id("DF");
	}

	@Test
	void getTotalizers_WhenCitiesProvided_ShouldAggregateByLevel3() {
		when(areaOfInterestRepository.aggregateByLevel3Ids(anyCollection()))
				.thenReturn(aggregate(3L, new BigDecimal("45")));

		TotalizerFilterRequest filter = new TotalizerFilterRequest();
		filter.setLevel2Id("ES");
		filter.setLevel3Ids(List.of("3200607"));

		List<TotalizerResponse> result = totalizerService.getTotalizers(filter);

		TotalizerResponse primary = result.stream()
				.filter(item -> TotalizerService.CODE_AREA_OF_INTEREST.equals(item.code()))
				.findFirst()
				.orElseThrow();
		assertEquals(3.0, primary.value());
		assertEquals(45L, primary.subItemValue());
		verify(areaOfInterestRepository).aggregateByLevel3Ids(eq(List.of("3200607")));
	}

	@Test
	void getTotalizers_WhenConfigLabelChanges_ShouldUseLabelFromConfig() {
		when(installationConfigService.getInstallationConfig())
				.thenReturn(installationConfig(
						"Imóveis cadastrados",
						"un.",
						"ha"
				));
		when(areaOfInterestRepository.aggregateAll())
				.thenReturn(aggregate(10L, new BigDecimal("20")));

		List<TotalizerResponse> result = totalizerService.getTotalizers(null);

		TotalizerResponse primary = result.stream()
				.filter(item -> TotalizerService.CODE_AREA_OF_INTEREST.equals(item.code()))
				.findFirst()
				.orElseThrow();
		assertEquals("Imóveis cadastrados", primary.name());
		assertEquals("un.", primary.unitOfMeasurement());
		assertEquals("ha", primary.subItemName());
	}

	@Test
	void getDetailByIdentifier_WhenKnown_ShouldReturnDetail() {
		AreaOfInterest areaOfInterest = buildSampleAreaOfInterest();
		when(areaOfInterestRepository.findById("DF-123")).thenReturn(Optional.of(areaOfInterest));

		DetailByIdentifierResponse result = totalizerService.getDetailByIdentifier("DF-123");

		assertEquals("DF-123", result.id());
		assertEquals("Distrito Federal", result.territory().level2().name());
		assertEquals("Brasília", result.territory().level3().name());
		assertEquals("DF", result.territory().level2().id());
		assertEquals("5300108", result.territory().level3().id());
		assertEquals("2020-01-10", result.registrationDate());
		assertEquals("2024-06-15", result.alterationDate());
		assertEquals(0, new BigDecimal("120.50").compareTo(result.area()));
		assertNotNull(result.latitude());
		assertNotNull(result.longitude());
	}

	@Test
	void getDetailByIdentifier_WhenUnknown_ShouldThrowNotFound() {
		when(areaOfInterestRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> totalizerService.getDetailByIdentifier("UNKNOWN")
		);

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	private static InstallationConfigResponse installationConfig(
			String label,
			String unitOfMeasurement,
			String optionalLabel
	) {
		KpiCardConfigResponse card = new KpiCardConfigResponse(
				TotalizerService.CODE_AREA_OF_INTEREST,
				label,
				unitOfMeasurement,
				optionalLabel,
				"#CED6E5",
				1,
				true
		);
		return new InstallationConfigResponse(
				List.of(),
				null,
				new HomeKpisConfigResponse(5, TotalizerService.CODE_AREA_OF_INTEREST, List.of(card)),
				new AreaOfInterestMeasuresConfigResponse("ha", "ha"),
				null
		);
	}

	private static AreaOfInterestAggregate aggregate(Long count, BigDecimal totalArea) {
		return new AreaOfInterestAggregate() {
			@Override
			public Long getCount() {
				return count;
			}

			@Override
			public BigDecimal getTotalArea() {
				return totalArea;
			}
		};
	}

	private static AreaOfInterest buildSampleAreaOfInterest() {
		TerritoryLevel2 level2 = new TerritoryLevel2();
		level2.setId("DF");
		level2.setName("Distrito Federal");

		TerritoryLevel3 level3 = new TerritoryLevel3();
		level3.setId("5300108");
		level3.setName("Brasília");
		level3.setParent(level2);

		GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 0);
		AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("DF-123");
		areaOfInterest.setRegistrationDate(LocalDateTime.of(2020, 1, 10, 8, 30));
		areaOfInterest.setAlterationDate(LocalDateTime.of(2024, 6, 15, 14, 0));
		areaOfInterest.setArea(new BigDecimal("120.50"));
		areaOfInterest.setTerritoryLevel3(level3);
		areaOfInterest.setCentroidCoordinates(
				factory.createPoint(new Coordinate(-47.85, -15.75))
		);
		return areaOfInterest;
	}
}
