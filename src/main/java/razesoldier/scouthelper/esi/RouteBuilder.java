package razesoldier.scouthelper.esi;

import net.troja.eve.esi.ApiException;
import net.troja.eve.esi.api.UserInterfaceApi;

import java.util.LinkedList;
import java.util.List;

/**
 * Used to build route in game
 */
public class RouteBuilder {
    private final UserInterfaceApi api;
    private final LinkedList<Long> points = new LinkedList<>();

    public RouteBuilder(UserInterfaceApi api) {
        this.api = api;
    }

    public void addPoint(Long point) {
        points.add(point);
    }

    public void addPoints(List<Long> pointList) {
        points.addAll(pointList);
    }

    public void build() throws ApiException {
        for (Long point: points) {
            api.postUiAutopilotWaypoint(false, false, point, null, null);
        }
    }
}
