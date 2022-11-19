package MP.todomap.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Todo {

    @Id
    @GeneratedValue
    private Long id;

    //@Column(nullable = false)
    private String uid;

    //@Column(nullable = false)
    private String time;

    //@Column(nullable = false)
    private String location;

    //@Column(nullable = false)
    private String body;

    @Builder
    public Todo(String uid, String time, String location, String body) {
        this.uid = uid;
        this.time = time;
        this.location = location;
        this.body = body;
    }

    public void update(String uid, String time, String location, String body) {
        this.uid = uid;
        this.time = time;
        this.location = location;
        this.body = body;
    }
}
