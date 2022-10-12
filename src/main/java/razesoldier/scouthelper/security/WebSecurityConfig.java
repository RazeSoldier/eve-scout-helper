package razesoldier.scouthelper.security;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import razesoldier.scouthelper.data.TokenRepository;
import razesoldier.scouthelper.data.UserRepository;
import razesoldier.scouthelper.security.oauth2.EVEOAuth2UserService;
import razesoldier.scouthelper.security.oauth2.JpaOAuth2AuthorizedClientService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(@NotNull HttpSecurity http) throws Exception {
        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(this.oAuth2UserService()) // Override Spring's built-in implementations for OAuth2UserService
			    )
			).authorizeHttpRequests().anyRequest().hasRole("USER").and();
        return http.build();
    }

    /**
     * Override Spring's built-in implementation for OAuth2AuthorizedClientService (default is {@link InMemoryOAuth2AuthorizedClientService})<br>
     * The application use database to store authentication information.
     */
    @Bean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(UserRepository userRepository, TokenRepository tokenRepository, @NotNull ClientRegistrationRepository clientRegistrationRepository) {
        return new JpaOAuth2AuthorizedClientService(userRepository, tokenRepository, clientRegistrationRepository.findByRegistrationId("eve"));
    }

    /**
     * Override Spring's built-in implementations.<br>
     * Because these implementations will try to send a request to the OAuth2 server to query the user's information,
     * but the OAuth2 server of ESI does not support this method, so use access token (JWT) to parse out character information.
     */
    @Contract(value = " -> new", pure = true)
    private @NotNull OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() {
        return new EVEOAuth2UserService();
    }
}
