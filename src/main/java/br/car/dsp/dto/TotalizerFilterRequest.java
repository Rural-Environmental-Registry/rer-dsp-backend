package br.car.dsp.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Espelha SimpleFilterTotalizerDTO do Consulta Pública.
 */
public class TotalizerFilterRequest {

	private String idState;
	private List<Integer> idsCities = new ArrayList<>();

	public String getIdState() {
		return idState;
	}

	public void setIdState(String idState) {
		this.idState = idState;
	}

	public List<Integer> getIdsCities() {
		return idsCities;
	}

	public void setIdsCities(List<Integer> idsCities) {
		this.idsCities = idsCities != null ? idsCities : new ArrayList<>();
	}
}
