package MP.todomap.web.dto;

import MP.todomap.domain.model.Todo;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoReadDto {

    private Long id;
    private String uid;
    private String time;
    private String location;
    private String body;

    public TodoReadDto(Todo todo) {
        this.id = todo.getId();
        this.uid = todo.getUid();
        this.time = todo.getTime();
        this.location = todo.getLocation();
        this.body = todo.getBody();
    }
}