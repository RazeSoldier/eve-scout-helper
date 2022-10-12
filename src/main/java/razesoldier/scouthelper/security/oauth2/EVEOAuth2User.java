package razesoldier.scouthelper.security.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class EVEOAuth2User extends DefaultOAuth2User {
    private final Long characterId;

    /**
     * Constructs a {@code EVEOAuth2User} using the provided parameters.
     *
     * @param authorities      the authorities granted to the user
     * @param attributes       the attributes about the user
     * @param nameAttributeKey the key used to access the user's &quot;name&quot; from
     *                         {@link #getAttributes()}
     */
    public EVEOAuth2User(Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes, String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
        characterId = (Long) attributes.get("characterId");
    }

    public Long getCharacterId() {
        return characterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EVEOAuth2User that = (EVEOAuth2User) o;
        return characterId.equals(that.characterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), characterId);
    }
}
