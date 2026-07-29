package br.car.dsp.service;

import br.car.dsp.dto.AreaOfInterestAggregate;
import br.car.dsp.dto.AreaOfInterestMeasuresConfigResponse;
import br.car.dsp.dto.DetailByIdentifierResponse;
import br.car.dsp.dto.HomeKpisConfigResponse;
import br.car.dsp.dto.InstallationConfigResponse;
import br.car.dsp.dto.KpiCardConfigResponse;
import br.car.dsp.dto.ThemeTotalsAggregate;
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
				.thenReturn(installationConfigWithThemes(
						"Registered properties",
						"un.",
						"ha"
				));
		lenient().when(areaOfInterestRepository.sumThemesAll())
				.thenReturn(themes(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
		lenient().when(areaOfInterestRepository.sumThemesByLevel2Id(org.mockito.ArgumentMatchers.anyString()))
				.thenReturn(themes(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
		lenient().when(areaOfInterestRepository.sumThemesByLevel3Ids(anyCollection()))
				.thenReturn(themes(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
	}

	@Test
	void getTotalizers_WhenFilterNull_ShouldUseRealAreaOfInterestAggregate() {
		when(areaOfInterestRepository.aggregateAll())
				.thenReturn(aggregate(508L, new BigDecimal("160652.4")));
		when(areaOfInterestRepository.sumThemesAll())
				.thenReturn(themes(
						new BigDecimal("10.4"),
						new BigDecimal("20.6"),
						BigDecimal.ZERO,
						new BigDecimal("5")
				));

		List<TotalizerResponse> result = totalizerService.getTotalizers(null);

		assertEquals(5, result.size());
		TotalizerResponse primary = result.getFirst();
		assertEquals(TotalizerService.CODE_AREA_OF_INTEREST, primary.code());
		assertEquals("Registered properties", primary.name());
		assertEquals(508.0, primary.value());
		assertEquals(160652L, primary.subItemValue());
		assertEquals("un.", primary.unitOfMeasurement());
		assertEquals("ha", primary.subItemName());

		assertEquals(TotalizerService.CODE_THEME_1, result.get(1).code());
		assertEquals(10.0, result.get(1).value());
		assertEquals(TotalizerService.CODE_THEME_2, result.get(2).code());
		assertEquals(21.0, result.get(2).value());
		assertEquals(0.0, result.get(3).value());
		assertEquals(5.0, result.get(4).value());
		verify(areaOfInterestRepository).aggregateAll();
		verify(areaOfInterestRepository).sumThemesAll();
	}

	@Test
	void getTotalizers_WhenStateProvided_ShouldAggregateByLevel2() {
		when(areaOfInterestRepository.aggregateByLevel2Id("DF"))
				.thenReturn(aggregate(12L, new BigDecimal("100.6")));

		TotalizerFilterRequest filter = new TotalizerFilterRequest();
		filter.setLevel2Id("DF");

		List<TotalizerResponse> result = totalizerService.getTotalizers(filter);

		TotalizerResponse primary = result.getFirst();
		assertEquals(12.0, primary.value());
		assertEquals(101L, primary.subItemValue());
		verify(areaOfInterestRepository).aggregateByLevel2Id("DF");
		verify(areaOfInterestRepository).sumThemesByLevel2Id("DF");
	}

	@Test
	void getTotalizers_WhenCitiesProvided_ShouldAggregateByLevel3() {
		when(areaOfInterestRepository.aggregateByLevel3Ids(anyCollection()))
				.thenReturn(aggregate(3L, new BigDecimal("45")));

		TotalizerFilterRequest filter = new TotalizerFilterRequest();
		filter.setLevel2Id("ES");
		filter.setLevel3Ids(List.of("3200607"));

		List<TotalizerResponse> result = totalizerService.getTotalizers(filter);

		TotalizerResponse primary = result.getFirst();
		assertEquals(3.0, primary.value());
		assertEquals(45L, primary.subItemValue());
		verify(areaOfInterestRepository).aggregateByLevel3Ids(eq(List.of("3200607")));
		verify(areaOfInterestRepository).sumThemesByLevel3Ids(eq(List.of("3200607")));
	}

	@Test
	void getTotalizers_WhenConfigLabelChanges_ShouldUseLabelFromConfig() {
		when(installationConfigService.getInstallationConfig())
				.thenReturn(installationConfigWithThemes(
						"Imóveis cadastrados",
						"un.",
						"ha"
				));
		when(areaOfInterestRepository.aggregateAll())
				.thenReturn(aggregate(10L, new BigDecimal("20")));

		List<TotalizerResponse> result = totalizerService.getTotalizers(null);

		TotalizerResponse primary = result.getFirst();
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

	private static InstallationConfigResponse installationConfigWithThemes(
			String label,
			String unitOfMeasurement,
			String optionalLabel
	) {
		List<KpiCardConfigResponse> cards = List.of(
				new KpiCardConfigResponse(
						TotalizerService.CODE_AREA_OF_INTEREST,
						label,
						unitOfMeasurement,
						optionalLabel,
						"#CED6E5",
						1,
						true
				),
				new KpiCardConfigResponse(TotalizerService.CODE_THEME_1, "Theme 1", "ha", null, "#C1D2F2", 2, false),
				new KpiCardConfigResponse(TotalizerService.CODE_THEME_2, "Theme 2", "ha", null, "#98B7EC", 3, false),
				new KpiCardConfigResponse(TotalizerService.CODE_THEME_3, "Theme 3", "ha", null, "#97CCE3", 4, false),
				new KpiCardConfigResponse(TotalizerService.CODE_THEME_4, "Theme 4", "ha", null, "#B6C3D9", 5, false)
		);
		return new InstallationConfigResponse(
				List.of(),
				null,
				new HomeKpisConfigResponse(5, TotalizerService.CODE_AREA_OF_INTEREST, cards),
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

	private static ThemeTotalsAggregate themes(
			BigDecimal t1,
			BigDecimal t2,
			BigDecimal t3,
			BigDecimal t4
	) {
		return new ThemeTotalsAggregate() {
			@Override
			public BigDecimal getTheme1() {
				return t1;
			}

			@Override
			public BigDecimal getTheme2() {
				return t2;
			}

			@Override
			public BigDecimal getTheme3() {
				return t3;
			}

			@Override
			public BigDecimal getTheme4() {
				return t4;
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
