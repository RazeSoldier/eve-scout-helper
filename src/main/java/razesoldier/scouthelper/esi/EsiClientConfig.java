package razesoldier.scouthelper.esi;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class EsiClientConfig {
    @Bean
    public ApiClientBuilderProxy apiClientBuilderProxy(ClientRegistration clientRegistration) {
        return new ApiClientBuilderProxy(clientRegistration);
    }

    /**
     * Put "eve" ClientRegistration to default implementation.
     * "eve" ClientRegistration config defined at application.yaml
     */
    @Bean
    public ClientRegistration clientRegistration(@NotNull ClientRegistrationRepository clientRegistrationRepository) {
        return clientRegistrationRepository.findByRegistrationId("eve");
    }
}
