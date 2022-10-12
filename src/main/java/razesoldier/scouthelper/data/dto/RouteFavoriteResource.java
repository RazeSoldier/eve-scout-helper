package razesoldier.scouthelper.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class RouteFavoriteResource {
    private final Long id;
    private final String name;
    private final List<String> points;
}
