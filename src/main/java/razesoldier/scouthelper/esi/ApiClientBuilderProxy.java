package razesoldier.scouthelper.esi;

import net.troja.eve.esi.ApiClient;
import net.troja.eve.esi.ApiClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

/**
 * A proxy object to proxy request to {@link ApiClientBuilder}
 */
public class ApiClientBuilderProxy {
    private final String clientId;

    public ApiClientBuilderProxy(@NotNull ClientRegistration clientRegistration) {
        this.clientId = clientRegistration.getClientId();
    }

    public ApiClient build(@NotNull String refreshToken) {
        return new ApiClientBuilder().clientID(clientId).refreshToken(refreshToken).build();
    }
}
