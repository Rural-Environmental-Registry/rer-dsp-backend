package br.car.dsp.controller;

import br.car.dsp.dto.MockItemResponse;
import br.car.dsp.service.MockDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mock")
@Tag(name = "Mock", description = "Dados mocados para teste local")
public class MockDataController {

	private final MockDataService mockDataService;

	public MockDataController(MockDataService mockDataService) {
		this.mockDataService = mockDataService;
	}

	@GetMapping("/items")
	@Operation(summary = "Lista itens mocados")
	public List<MockItemResponse> listarItens() {
		return mockDataService.listarItens();
	}
}
