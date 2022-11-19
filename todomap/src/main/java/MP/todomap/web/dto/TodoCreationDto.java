package MP.todomap.web.dto;

import MP.todomap.domain.model.Todo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoCreationDto {

    private String uid;
    private String time;
    private String location;
    private String body;

    @Builder
    public TodoCreationDto(String uid, String time, String location, String body) {
        this.uid = uid;
        this.time = time;
        this.location = location;
        this.body = body;
    }

    public Todo toEntity() {
        return Todo.builder().uid(uid).time(time).location(location).body(body).build();
    }
}
