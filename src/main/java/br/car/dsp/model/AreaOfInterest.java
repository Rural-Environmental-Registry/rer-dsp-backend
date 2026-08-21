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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

/**
 * Generic area of interest (example: property).
 * Full geometry lives in the GeoServer exhibition database.
 */
@Entity
@Table(
		schema = "dsp",
		name = "area_of_interest",
		indexes = @Index(name = "idx_area_of_interest_territory_level_3_id", columnList = "territory_level_3_id")
)
@Getter
@Setter
public class AreaOfInterest {

	@Id
	@Column(name = "id", length = 255, nullable = false)
	private String id;

	@Column(name = "registration_date", nullable = false)
	private LocalDateTime registrationDate;

	@Column(name = "updated_at")
	private LocalDateTime alterationDate;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(
			name = "territory_level_3_id",
			nullable = true,
			foreignKey = @ForeignKey(name = "fk_area_of_interest_territory_level_3")
	)
	private TerritoryLevel3 territoryLevel3;

	// Area migrated from source (media unit = installationConfig.areaOfInterest).
	@Column(name = "area")
	private BigDecimal area;

	/** Optional KPI slot THEME_1 (label from installation-config). */
	@Column(name = "theme_1")
	private BigDecimal theme1;

	/** Optional KPI slot THEME_2. */
	@Column(name = "theme_2")
	private BigDecimal theme2;

	/** Optional KPI slot THEME_3. */
	@Column(name = "theme_3")
	private BigDecimal theme3;

	/** Optional KPI slot THEME_4. */
	@Column(name = "theme_4")
	private BigDecimal theme4;

	@Column(name = "boundary_box", columnDefinition = "geometry")
	private Polygon boundaryBox;

	@Column(name = "centroid_coordinates", columnDefinition = "geometry")
	private Point centroidCoordinates;
}
