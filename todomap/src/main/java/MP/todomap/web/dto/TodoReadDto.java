package MP.todomap.web.dto;

import MP.todomap.domain.model.Todo;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoReadDto {

    private Long id;
    private String uid;
    private String date;
    private String time;
    private String locName;
    private String locLatitude;
    private String locLongitude;
    private String description;

    public TodoReadDto(Todo todo) {
        this.id = todo.getId();
        this.uid = todo.getUid();
        this.date = todo.getDate();
        this.time = todo.getTime();
        this.locName = todo.getLocName();
        this.locLatitude = todo.getLocLatitude();
        this.locLongitude = todo.getLocLongitude();
        this.description = todo.getDescription();
    }
}