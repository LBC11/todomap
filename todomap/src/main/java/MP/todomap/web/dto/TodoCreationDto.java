package MP.todomap.web.dto;

import MP.todomap.domain.model.Todo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoCreationDto {

    private String uid;
    private String date;
    private String time;
    private String locLatitude;
    private String locLongitude;
    private String description;

    @Builder
    public TodoCreationDto(String uid, String date, String time, String locLatitude, String locLongitude, String description) {
        this.uid = uid;
        this.date = date;
        this.time = time;
        this.locLatitude = locLatitude;
        this.locLongitude = locLongitude;
        this.description = description;
    }

    public Todo toEntity() {
        return Todo.builder().uid(uid).date(date).time(time).locLatitude(locLatitude).locLongitude(locLongitude).description(description).build();
    }
}
