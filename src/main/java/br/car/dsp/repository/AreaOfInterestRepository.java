package br.car.dsp.repository;

import br.car.dsp.model.AreaOfInterest;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaOfInterestRepository extends JpaRepository<AreaOfInterest, String> {

	@EntityGraph(attributePaths = {"territoryLevel3", "territoryLevel3.parent"})
	@Override
	Optional<AreaOfInterest> findById(String id);
}
