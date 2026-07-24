package br.car.dsp.dto;

import java.util.ArrayList;
import java.util.List;

public class TotalizerFilterRequest {

	private String level2Id;
	private List<String> level3Ids = new ArrayList<>();

	public String getLevel2Id() {
		return level2Id;
	}

	public void setLevel2Id(String level2Id) {
		this.level2Id = level2Id;
	}

	public List<String> getLevel3Ids() {
		return level3Ids;
	}

	public void setLevel3Ids(List<String> level3Ids) {
		this.level3Ids = level3Ids != null ? level3Ids : new ArrayList<>();
	}
}
