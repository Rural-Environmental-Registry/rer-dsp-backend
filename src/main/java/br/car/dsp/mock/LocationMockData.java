package br.car.dsp.mock;

import br.car.dsp.dto.CityResponse;
import br.car.dsp.dto.DetailByIdentifierResponse;
import br.car.dsp.dto.RegionResponse;
import br.car.dsp.dto.StateResponse;
import br.car.dsp.dto.TotalizerResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Base em memória com amostra estilo Consulta Pública (UF / município / região / KPIs).
 */
public final class LocationMockData {

	private static final List<StateResponse> STATES = List.of(
			new StateResponse("AC", "Acre", "N", null),
			new StateResponse("AM", "Amazonas", "N", null),
			new StateResponse("PA", "Pará", "N", null),
			new StateResponse("BA", "Bahia", "NE", null),
			new StateResponse("PE", "Pernambuco", "NE", null),
			new StateResponse("CE", "Ceará", "NE", null),
			new StateResponse("DF", "Distrito Federal", "CW", null),
			new StateResponse("GO", "Goiás", "CW", null),
			new StateResponse("MT", "Mato Grosso", "CW", null),
			new StateResponse("MG", "Minas Gerais", "SE", null),
			new StateResponse("SP", "São Paulo", "SE", null),
			new StateResponse("RJ", "Rio de Janeiro", "SE", null),
			new StateResponse("PR", "Paraná", "S", null),
			new StateResponse("SC", "Santa Catarina", "S", null),
			new StateResponse("RS", "Rio Grande do Sul", "S", null)
	);

	private static final Map<String, List<CityResponse>> CITIES_BY_STATE = buildCities();

	private static final List<RegionResponse> REGIONS = List.of(
			region(1L, "Norte", "N"),
			region(2L, "Nordeste", "NE"),
			region(3L, "Centro-Oeste", "CW"),
			region(4L, "Sudeste", "SE"),
			region(5L, "Sul", "S")
	);

	private static final Map<String, DetailByIdentifierResponse> PROPERTIES_BY_CODE = Map.of(
			"DF123456789012",
			new DetailByIdentifierResponse(
					"DF123456789012",
					"-15.793889",
					"-47.882778",
					"-15.793889, -47.882778",
					"DF",
					"Distrito Federal",
					"Brasília",
					new BigDecimal("2.5"),
					"10/01/2020",
					"15/06/2024",
					new BigDecimal("120.50"),
					1001,
					null
			),
			"GO998877665544",
			new DetailByIdentifierResponse(
					"GO998877665544",
					"-16.686891",
					"-49.264794",
					"-16.686891, -49.264794",
					"GO",
					"Goiás",
					"Goiânia",
					new BigDecimal("4.0"),
					"22/03/2019",
					"01/02/2025",
					new BigDecimal("340.00"),
					1002,
					null
			),
			"MG112233445566",
			new DetailByIdentifierResponse(
					"MG112233445566",
					"-19.916681",
					"-43.934493",
					"-19.916681, -43.934493",
					"MG",
					"Minas Gerais",
					"Belo Horizonte",
					new BigDecimal("1.2"),
					"05/11/2018",
					"12/12/2023",
					new BigDecimal("85.75"),
					1003,
					null
			)
	);

	private LocationMockData() {
	}

	public static List<StateResponse> getAllStates() {
		return STATES;
	}

	public static List<StateResponse> getStatesByRegionCode(String regionCode) {
		String code = regionCode == null ? "" : regionCode.trim().toUpperCase(Locale.ROOT);
		return STATES.stream()
				.filter(state -> code.equalsIgnoreCase(state.region()))
				.toList();
	}

	public static List<CityResponse> getCitiesByState(String stateId) {
		if (stateId == null) {
			return List.of();
		}
		return CITIES_BY_STATE.getOrDefault(stateId.trim().toUpperCase(Locale.ROOT), List.of());
	}

	public static List<RegionResponse> getRegions() {
		return REGIONS;
	}

	public static Optional<DetailByIdentifierResponse> findByIdentifier(String identifier) {
		if (identifier == null || identifier.isBlank()) {
			return Optional.empty();
		}
		String normalized = identifier.replace(".", "").replace("-", "").trim().toUpperCase(Locale.ROOT);
		return Optional.ofNullable(PROPERTIES_BY_CODE.get(normalized))
				.or(() -> PROPERTIES_BY_CODE.entrySet().stream()
						.filter(entry -> entry.getKey().equalsIgnoreCase(normalized)
								|| entry.getValue().codeProperty().equalsIgnoreCase(identifier.trim()))
						.map(Map.Entry::getValue)
						.findFirst());
	}

	public static List<TotalizerResponse> buildTotalizers(String idState, List<Integer> idsCities) {
		double factor = 1.0;
		if (idState != null && !idState.isBlank()) {
			factor = 0.12;
		}
		if (idsCities != null && !idsCities.isEmpty()) {
			factor = 0.03 * idsCities.size();
		}

		long properties = Math.round(128_450 * factor);
		long areaHa = Math.round(2_456_789 * factor);
		long legalReserve = Math.round(820_100 * factor);
		long app = Math.round(310_400 * factor);
		long nativeVeg = Math.round(1_102_300 * factor);
		long consolidated = Math.round(990_200 * factor);

		List<TotalizerResponse> items = new ArrayList<>();
		items.add(new TotalizerResponse(
				"Imóveis cadastrados",
				"REGISTERED_AREA",
				(double) properties,
				"ha",
				areaHa,
				"un."
		));
		items.add(new TotalizerResponse(
				"Reserva Legal",
				"LEGAL_RESERVE",
				(double) legalReserve,
				null,
				null,
				"ha"
		));
		items.add(new TotalizerResponse(
				"Área de Preservação Permanente",
				"PERMANENT_PRESERVATION_AREA",
				(double) app,
				null,
				null,
				"ha"
		));
		items.add(new TotalizerResponse(
				"Vegetação Nativa",
				"NATIVE_VEGETATION",
				(double) nativeVeg,
				null,
				null,
				"ha"
		));
		items.add(new TotalizerResponse(
				"Área Consolidada",
				"CONSOLIDATED_AREA",
				(double) consolidated,
				null,
				null,
				"ha"
		));
		return items;
	}

	private static RegionResponse region(Long id, String name, String code) {
		List<StateResponse> states = getStatesByRegionCode(code);
		return new RegionResponse(id, name, code, states);
	}

	private static Map<String, List<CityResponse>> buildCities() {
		Map<String, List<CityResponse>> map = new LinkedHashMap<>();
		map.put("DF", List.of(
				new CityResponse(5300108, "Brasília", null)
		));
		map.put("GO", List.of(
				new CityResponse(5208707, "Goiânia", null),
				new CityResponse(5201405, "Anápolis", null),
				new CityResponse(5218805, "Rio Verde", null)
		));
		map.put("MG", List.of(
				new CityResponse(3106200, "Belo Horizonte", null),
				new CityResponse(3118601, "Contagem", null),
				new CityResponse(3170206, "Uberlândia", null)
		));
		map.put("SP", List.of(
				new CityResponse(3550308, "São Paulo", null),
				new CityResponse(3509502, "Campinas", null),
				new CityResponse(3549904, "São José dos Campos", null)
		));
		map.put("RJ", List.of(
				new CityResponse(3304557, "Rio de Janeiro", null),
				new CityResponse(3303500, "Nova Iguaçu", null)
		));
		map.put("BA", List.of(
				new CityResponse(2927408, "Salvador", null),
				new CityResponse(2910800, "Feira de Santana", null)
		));
		map.put("AM", List.of(
				new CityResponse(1302603, "Manaus", null)
		));
		map.put("PR", List.of(
				new CityResponse(4106902, "Curitiba", null),
				new CityResponse(4113700, "Londrina", null)
		));
		map.put("SC", List.of(
				new CityResponse(4205407, "Florianópolis", null),
				new CityResponse(4209102, "Joinville", null)
		));
		map.put("RS", List.of(
				new CityResponse(4314902, "Porto Alegre", null),
				new CityResponse(4305108, "Caxias do Sul", null)
		));
		map.put("PE", List.of(
				new CityResponse(2611606, "Recife", null)
		));
		map.put("CE", List.of(
				new CityResponse(2304400, "Fortaleza", null)
		));
		map.put("PA", List.of(
				new CityResponse(1501402, "Belém", null)
		));
		map.put("AC", List.of(
				new CityResponse(1200401, "Rio Branco", null)
		));
		map.put("MT", List.of(
				new CityResponse(5103403, "Cuiabá", null)
		));
		return Map.copyOf(map);
	}
}
