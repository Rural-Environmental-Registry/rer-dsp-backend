package br.car.dsp.service;

import br.car.dsp.dto.MockItemResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockDataService {

	public List<MockItemResponse> listarItens() {
		return List.of(
				new MockItemResponse(1L, "Imóvel mock Alfa", "DF"),
				new MockItemResponse(2L, "Imóvel mock Beta", "GO"),
				new MockItemResponse(3L, "Imóvel mock Gama", "MG")
		);
	}
}
