package razesoldier.scouthelper.web.controller;

import lombok.extern.slf4j.Slf4j;
import net.troja.eve.esi.ApiClient;
import net.troja.eve.esi.ApiException;
import net.troja.eve.esi.api.UserInterfaceApi;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;
import razesoldier.scouthelper.data.*;
import razesoldier.scouthelper.data.dto.RouteFavoriteResource;
import razesoldier.scouthelper.data.dto.RouteSaveRequest;
import razesoldier.scouthelper.data.dto.RouteSaveResponse;
import razesoldier.scouthelper.esi.ApiClientBuilderProxy;
import razesoldier.scouthelper.esi.RouteBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping(value = "/api/routes", produces = "application/json")
@Slf4j
public class RouteController {
    private final RouteFavoriteRepository routeFavoriteRepository;
    private final UserRepository userRepository;
    private final SolarSystemRepository solarSystemRepository;
    private final ApiClientBuilderProxy apiClientBuilderProxy;

    public RouteController(RouteFavoriteRepository routeFavoriteRepository,
                           UserRepository userRepository,
                           SolarSystemRepository solarSystemRepository,
                           ApiClientBuilderProxy apiClientBuilderProxy) {
        this.routeFavoriteRepository = routeFavoriteRepository;
        this.userRepository = userRepository;
        this.solarSystemRepository = solarSystemRepository;
        this.apiClientBuilderProxy = apiClientBuilderProxy;
    }

    @GetMapping("/")
    public List<RouteFavoriteResource> all(@RegisteredOAuth2AuthorizedClient @NotNull OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        return routeFavoriteRepository.findAllByUserId(userRepository.findByName(oAuth2AuthorizedClient.getPrincipalName()).getId())
                .stream()
                .map(f -> new RouteFavoriteResource(
                            f.getId(),
                            f.getName(),
                            f.getPoints().stream().map(id -> solarSystemRepository.findById(id).get().getName()).toList()
                        )
                ).toList();
    }

    @PostMapping("/build")
    public ResponseEntity<String> build(@RequestBody List<Long> routes, @RegisteredOAuth2AuthorizedClient @NotNull OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        ApiClient apiClient = buildApiClientFromAuthorizedClient(oAuth2AuthorizedClient);
        RouteBuilder builder = new RouteBuilder(new UserInterfaceApi(apiClient));
        builder.addPoints(routes);
        try {
            builder.build();
            return ResponseEntity.noContent().build();
        } catch (ApiException e) {
            log.error("applyRouteFavoriteToGame failed");
            log.error("ApiException: message: {}, status code: {}", e.getResponseBody(), e.getCode());
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
    }

    @PostMapping("/apply/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<String> applyRouteFavoriteToGame(@PathVariable("id") Long id,
                                                           @RegisteredOAuth2AuthorizedClient @NotNull OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        Optional<RouteFavorite> favoriteOptional = getRouteFavoriteByIdAndUserId(id, oAuth2AuthorizedClient);
        AtomicBoolean failed = new AtomicBoolean(false);
        favoriteOptional.ifPresent(favorite -> {
            ApiClient apiClient = buildApiClientFromAuthorizedClient(oAuth2AuthorizedClient);
            RouteBuilder routeBuilder = new RouteBuilder(new UserInterfaceApi(apiClient));
            routeBuilder.addPoints(favorite.getPoints());
            try {
                routeBuilder.build();
            } catch (ApiException e) {
                log.error("applyRouteFavoriteToGame failed");
                log.error("ApiException: message: {}, status code: {}", e.getResponseBody(), e.getCode());
                failed.set(true);
            }
        });
        if (failed.get()) {
            return ResponseEntity.internalServerError().body("Internal Server Error");
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<RouteSaveResponse> save(@RequestBody @NotNull RouteSaveRequest request, @RegisteredOAuth2AuthorizedClient @NotNull OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        User user = userRepository.findByName(oAuth2AuthorizedClient.getPrincipalName());
        RouteFavorite favorite = routeFavoriteRepository.save(new RouteFavorite(user, request.getName(), request.getPoints()));
        log.info("{} saved RouteFavorite id: {}", oAuth2AuthorizedClient.getPrincipalName(), favorite.getId());
        return new ResponseEntity<>(new RouteSaveResponse(favorite.getId()), HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id, @RegisteredOAuth2AuthorizedClient @NotNull OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        getRouteFavoriteByIdAndUserId(id, oAuth2AuthorizedClient)
                .ifPresent(entity -> {
                    routeFavoriteRepository.delete(entity);
                    log.info("{} deleted route favorite: {}", oAuth2AuthorizedClient.getPrincipalName(), entity.getId());
                });
    }

    private Optional<RouteFavorite> getRouteFavoriteByIdAndUserId(Long id, @NotNull OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        return routeFavoriteRepository.findByIdAndUserId(id, userRepository.findByName(oAuth2AuthorizedClient.getPrincipalName()).getId());
    }

    private ApiClient buildApiClientFromAuthorizedClient(@NotNull OAuth2AuthorizedClient oAuth2AuthorizedClient) {
        return apiClientBuilderProxy.build(Objects.requireNonNull(oAuth2AuthorizedClient.getRefreshToken()).getTokenValue());
    }
}
