package br.car.dsp.repository;

import br.car.dsp.dto.TerritoryEnvelopeProjection;
import br.car.dsp.model.TerritoryLevel1;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TerritoryLevel1Repository extends JpaRepository<TerritoryLevel1, String> {

	@Query("SELECT t.id FROM TerritoryLevel1 t WHERE t.id IN :ids")
	List<String> findIdsPresent(@Param("ids") Collection<String> ids);

	@Query(value = """
			SELECT
				public.ST_XMin(ext) AS minX,
				public.ST_YMin(ext) AS minY,
				public.ST_XMax(ext) AS maxX,
				public.ST_YMax(ext) AS maxY
			FROM (
				SELECT public.ST_Extent(public.ST_Transform(t.boundary_box, 4326)) AS ext
				FROM dsp.territory_level_1 t
				WHERE t.boundary_box IS NOT NULL
			) sub
			""", nativeQuery = true)
	TerritoryEnvelopeProjection findEnvelopeAll();

	@Query(value = """
			SELECT
				public.ST_XMin(ext) AS minX,
				public.ST_YMin(ext) AS minY,
				public.ST_XMax(ext) AS maxX,
				public.ST_YMax(ext) AS maxY
			FROM (
				SELECT public.ST_Extent(public.ST_Transform(t.boundary_box, 4326)) AS ext
				FROM dsp.territory_level_1 t
				WHERE t.boundary_box IS NOT NULL
				  AND t.id IN (:ids)
			) sub
			""", nativeQuery = true)
	TerritoryEnvelopeProjection findEnvelopeByIds(@Param("ids") Collection<String> ids);
}
