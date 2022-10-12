package razesoldier.scouthelper.data;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class User {
    @Id
    private final Long id;

    private final String name;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToOne(targetEntity = Token.class, orphanRemoval = true)
    @ToString.Exclude
    private final Token token;

    @PrePersist
    void createAt() {
        createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}