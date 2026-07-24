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
 * Unidade territorial genérica — nível 3 (tabela dsp.level3 do core).
 */
@Entity
@Table(
		schema = "dsp",
		name = "level3",
		indexes = @Index(name = "idx_dsp_level3_level2_id", columnList = "level2_id")
)
@Getter
@Setter
public class TerritoryLevel3 {

	@Id
	@Column(name = "id", length = 64, nullable = false)
	private String id;

	@Column(name = "label", length = 255, nullable = false)
	private String label;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "level2_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "dsp_level3_level2_id_fkey")
	)
	private TerritoryLevel2 parent;

	@Column(name = "geometry", columnDefinition = "geometry")
	private Geometry geometry;
}
