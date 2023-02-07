package razesoldier.scouthelper.security.oauth2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth2")
@Getter
@Setter
public class OAuth2Properties {
    private Proxy proxy;

    @Getter
    @Setter
    public static class Proxy {
        private String host;
        private Integer port;
    }
}
