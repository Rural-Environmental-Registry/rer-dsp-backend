package br.car.dsp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;

/**
 * Unidade territorial genérica — nível 2 (tabela dsp.level2 do core).
 */
@Entity
@Table(
		schema = "dsp",
		name = "level2",
		indexes = @Index(name = "idx_dsp_level2_level1_id", columnList = "level1_id")
)
@Getter
@Setter
public class TerritoryLevel2 {

	@Id
	@Column(name = "id", length = 64, nullable = false)
	private String id;

	@Column(name = "label", length = 255, nullable = false)
	private String label;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "level1_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "dsp_level2_level1_id_fkey")
	)
	private TerritoryLevel1 parent;

	@Column(name = "geometry", columnDefinition = "geometry")
	private Geometry geometry;
}
