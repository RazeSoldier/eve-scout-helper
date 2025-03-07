package razesoldier.scouthelper.security;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Socks5Proxy;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JettyClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import razesoldier.scouthelper.data.TokenRepository;
import razesoldier.scouthelper.data.UserRepository;
import razesoldier.scouthelper.security.oauth2.EVEOAuth2UserService;
import razesoldier.scouthelper.security.oauth2.JpaOAuth2AuthorizedClientService;
import razesoldier.scouthelper.security.oauth2.OAuth2Properties;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(OAuth2Properties.class)
public class WebSecurityConfig {
    private final OAuth2Properties oAuth2Properties;

    public WebSecurityConfig(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
    }

    @Bean
    public SecurityFilterChain filterChain(@NotNull HttpSecurity http) throws Exception {
        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(this.oAuth2UserService()) // Override Spring's built-in implementations for OAuth2UserService
                        ).tokenEndpoint(endpointConfig -> endpointConfig
                                .accessTokenResponseClient(accessTokenResponseClient())
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/drifter-report/build").permitAll()
                        .requestMatchers("/api/system/name-suggest").permitAll()
                        .requestMatchers("/styles.css", "/fonts/**").permitAll()
                        .anyRequest().authenticated()
                );
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
     * Override Spring's built-in implementation for OAuth2AuthorizedClientManager (default is {@link DefaultOAuth2AuthorizedClientManager})
     */
    @Bean
    public OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        var tokenResponseClient = new RestClientRefreshTokenTokenResponseClient();
        tokenResponseClient.setRestClient(prepareProxiedRestClient());

        // Create ClientCredentialsOAuth2AuthorizedClientProvider and override default setAccessTokenResponseClient
        // with the one we created in this method
        final var authorizedClientProvider = new RefreshTokenOAuth2AuthorizedClientProvider();
        authorizedClientProvider.setAccessTokenResponseClient(tokenResponseClient);

        final var authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
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

    /**
     * Custom DefaultAuthorizationCodeTokenResponseClient, use customized {@link RestClient} that provided by {@link #prepareProxiedRestClient()}
     */
    private @NotNull OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        var client = new RestClientAuthorizationCodeTokenResponseClient();
        client.setRestClient(prepareProxiedRestClient());
        return client;
    }

    private @NotNull RestTemplate prepareProxiedRestTemplate() {
        return new RestTemplateBuilder()
                .messageConverters(Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()))
                .errorHandler(new OAuth2ErrorResponseErrorHandler())
                .requestFactory(() -> {
                    var proxyConfig = oAuth2Properties.getProxy();
                    HttpClient httpClient = new HttpClient();
                    // Determine whether there is a proxy configured
                    if (proxyConfig != null) {
                        httpClient.getProxyConfiguration().addProxy(new Socks5Proxy(proxyConfig.getHost(), proxyConfig.getPort()));
                    }
                    return new JettyClientHttpRequestFactory(httpClient);
                }).build();
    }

    private @NotNull RestClient prepareProxiedRestClient() {
        return RestClient.create(prepareProxiedRestTemplate());
    }
}
