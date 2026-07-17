package br.car.dsp.service;

import br.car.dsp.dto.DetailByIdentifierResponse;
import br.car.dsp.dto.TotalizerFilterRequest;
import br.car.dsp.dto.TotalizerResponse;
import br.car.dsp.mock.LocationMockData;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TotalizerService {

	public List<TotalizerResponse> getTotalizers(TotalizerFilterRequest filter) {
		String idState = filter != null ? filter.getIdState() : null;
		List<Integer> idsCities = filter != null ? filter.getIdsCities() : List.of();
		return LocationMockData.buildTotalizers(idState, idsCities);
	}

	public DetailByIdentifierResponse getDetailByIdentifier(String identifier) {
		return LocationMockData.findByIdentifier(identifier)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Identificador não encontrado"
				));
	}
}
