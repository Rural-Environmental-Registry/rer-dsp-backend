package br.car.dsp.dto;

import java.util.ArrayList;
import java.util.List;

public class TotalizerFilterRequest {

	private List<String> level2Ids = new ArrayList<>();
	private List<String> level3Ids = new ArrayList<>();

	public List<String> getLevel2Ids() {
		return level2Ids;
	}

	public void setLevel2Ids(List<String> level2Ids) {
		this.level2Ids = level2Ids != null ? level2Ids : new ArrayList<>();
	}

	public List<String> getLevel3Ids() {
		return level3Ids;
	}

	public void setLevel3Ids(List<String> level3Ids) {
		this.level3Ids = level3Ids != null ? level3Ids : new ArrayList<>();
	}
}
