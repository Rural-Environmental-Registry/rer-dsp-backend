package br.car.dsp.repository;

import br.car.dsp.model.TerritoryLevel2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerritoryLevel2Repository extends JpaRepository<TerritoryLevel2, String> {

	List<TerritoryLevel2> findByParent_Id(String parentId);
}
