package razesoldier.scouthelper.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SolarSystemRepository extends CrudRepository<SolarSystem, Long> {
    List<SolarSystem> findByNameStartingWith(String name);
}
