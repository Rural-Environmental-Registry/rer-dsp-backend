package br.car.dsp.repository;

import br.car.dsp.dto.AreaOfInterestAggregate;
import br.car.dsp.dto.ThemeTotalsAggregate;
import br.car.dsp.model.AreaOfInterest;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AreaOfInterestRepository extends JpaRepository<AreaOfInterest, String> {

	@EntityGraph(attributePaths = {"territoryLevel3", "territoryLevel3.parent"})
	@Override
	Optional<AreaOfInterest> findById(String id);

	@Query(value = """
			SELECT
				COUNT(1) AS count,
				COALESCE(SUM(a.area), 0) AS totalArea
			FROM dsp.area_of_interest a
			""", nativeQuery = true)
	AreaOfInterestAggregate aggregateAll();

	@Query(value = """
			SELECT
				COUNT(1) AS count,
				COALESCE(SUM(a.area), 0) AS totalArea
			FROM dsp.area_of_interest a
			JOIN dsp.territory_level_3 l3 ON l3.id = a.territory_level_3_id
			WHERE l3.parent_id = :level2Id
			""", nativeQuery = true)
	AreaOfInterestAggregate aggregateByLevel2Id(@Param("level2Id") String level2Id);

	@Query(value = """
			SELECT
				COUNT(1) AS count,
				COALESCE(SUM(a.area), 0) AS totalArea
			FROM dsp.area_of_interest a
			WHERE a.territory_level_3_id IN (:level3Ids)
			""", nativeQuery = true)
	AreaOfInterestAggregate aggregateByLevel3Ids(@Param("level3Ids") Collection<String> level3Ids);

	@Query(value = """
			SELECT
				COALESCE(SUM(a.theme_1), 0) AS theme1,
				COALESCE(SUM(a.theme_2), 0) AS theme2,
				COALESCE(SUM(a.theme_3), 0) AS theme3,
				COALESCE(SUM(a.theme_4), 0) AS theme4
			FROM dsp.area_of_interest a
			""", nativeQuery = true)
	ThemeTotalsAggregate sumThemesAll();

	@Query(value = """
			SELECT
				COALESCE(SUM(a.theme_1), 0) AS theme1,
				COALESCE(SUM(a.theme_2), 0) AS theme2,
				COALESCE(SUM(a.theme_3), 0) AS theme3,
				COALESCE(SUM(a.theme_4), 0) AS theme4
			FROM dsp.area_of_interest a
			JOIN dsp.territory_level_3 l3 ON l3.id = a.territory_level_3_id
			WHERE l3.parent_id = :level2Id
			""", nativeQuery = true)
	ThemeTotalsAggregate sumThemesByLevel2Id(@Param("level2Id") String level2Id);

	@Query(value = """
			SELECT
				COALESCE(SUM(a.theme_1), 0) AS theme1,
				COALESCE(SUM(a.theme_2), 0) AS theme2,
				COALESCE(SUM(a.theme_3), 0) AS theme3,
				COALESCE(SUM(a.theme_4), 0) AS theme4
			FROM dsp.area_of_interest a
			WHERE a.territory_level_3_id IN (:level3Ids)
			""", nativeQuery = true)
	ThemeTotalsAggregate sumThemesByLevel3Ids(@Param("level3Ids") Collection<String> level3Ids);
}
