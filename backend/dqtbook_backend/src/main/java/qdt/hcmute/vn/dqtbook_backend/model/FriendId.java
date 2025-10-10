package qdt.hcmute.vn.dqtbook_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class FriendId implements Serializable {
    @Column(name = "user_id_1", nullable = false)
    private Integer userId1;

    @Column(name = "user_id_2", nullable = false)
    private Integer userId2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendId friendId = (FriendId) o;
        return Objects.equals(userId1, friendId.userId1) && Objects.equals(userId2, friendId.userId2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId1, userId2);
    }
}


