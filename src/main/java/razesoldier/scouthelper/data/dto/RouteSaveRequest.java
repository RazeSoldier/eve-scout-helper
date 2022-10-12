package razesoldier.scouthelper.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class RouteSaveRequest {
    private final String name;
    private final List<Long> points;
}
