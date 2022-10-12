package razesoldier.scouthelper.data;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Entity
@Table(name = "solar_systems")
public class SolarSystem {
    @Id
    private final Long id;

    private final String name;

    @Column(name = "security_status")
    private final double securityStatus;

    @Column(name = "constellation_id")
    private final Long constellationId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SolarSystem that = (SolarSystem) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
