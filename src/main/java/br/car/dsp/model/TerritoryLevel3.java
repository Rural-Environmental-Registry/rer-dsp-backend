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
 * Generic territorial unit — level 3.
 */
@Entity
@Table(
		schema = "dsp",
		name = "territory_level_3",
		indexes = @Index(name = "idx_territory_level_3_parent_id", columnList = "parent_id")
)
@Getter
@Setter
public class TerritoryLevel3 {

	@Id
	@Column(name = "id", length = 64, nullable = false)
	private String id;

	@Column(name = "name", length = 255, nullable = false)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(
			name = "parent_id",
			nullable = true,
			foreignKey = @ForeignKey(name = "fk_territory_level_3_parent")
	)
	private TerritoryLevel2 parent;

	@Column(name = "geometry", columnDefinition = "geometry")
	private Geometry geometry;
}
