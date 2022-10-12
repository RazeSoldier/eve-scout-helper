package razesoldier.scouthelper.util;

import net.troja.eve.esi.ApiException;
import net.troja.eve.esi.api.UniverseApi;
import net.troja.eve.esi.model.SystemResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Used to generate a sql file from ESI, then the file can be used to insert data to database
 */
public class SolarSystemDataGenerator {
    public static void main(String[] args) throws ApiException, IOException {
        UniverseApi universeApi = new UniverseApi();
        List<Integer> systems = universeApi.getUniverseSystems(null, null);
        List<SystemResponse> systemResponses = systems.parallelStream().map(id -> {
            try {
                return universeApi.getUniverseSystemsSystemId(id, null, null, null, null);
            } catch (ApiException e) {
                Logger.getLogger("SolarSystemDataGenerator").severe(String.valueOf(e));
                Logger.getLogger("SolarSystemDataGenerator").severe("ignore id " + id);
                return null;
            }
        }).filter(Objects::nonNull).toList();

        StringBuilder stringBuilder = new StringBuilder();
        for (SystemResponse system : systemResponses) {
            stringBuilder.append(
                    String.format("INSERT INTO solar_systems (id, name, security_status, constellation_id) VALUES (%d, '%s', %f, %d);",
                            system.getSystemId(), system.getName(), system.getSecurityStatus(), system.getConstellationId())
            ).append("\n");
        }
        Files.writeString(Path.of("solar_systems.data.sql"), stringBuilder.toString());
    }
}
