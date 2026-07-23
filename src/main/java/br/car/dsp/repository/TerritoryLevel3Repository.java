package br.car.dsp.repository;

import br.car.dsp.model.TerritoryLevel3;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerritoryLevel3Repository extends JpaRepository<TerritoryLevel3, String> {

	List<TerritoryLevel3> findByParent_Id(String parentId);
}
