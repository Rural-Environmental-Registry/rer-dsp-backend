package br.car.dsp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;

/**
 * Unidade territorial genérica — nível 1 (dsp.territory_level_1).
 */
@Entity
@Table(schema = "dsp", name = "territory_level_1")
@Getter
@Setter
public class TerritoryLevel1 {

	@Id
	@Column(name = "id", length = 64, nullable = false)
	private String id;

	@Column(name = "name", length = 255, nullable = false)
	private String name;

	@Column(name = "geometry", columnDefinition = "geometry")
	private Geometry geometry;
}
