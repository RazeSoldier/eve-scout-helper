package razesoldier.scouthelper.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RouteFavoriteRepository extends CrudRepository<RouteFavorite, Long> {
    List<RouteFavorite> findAllByUserId(Long userId);
    Optional<RouteFavorite> findByIdAndUserId(Long id, Long userId);
}