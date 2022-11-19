package MP.todomap.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoUpdateDto {

    private String uid;
    private String time;
    private String location;
    private String body;

    @Builder
    public TodoUpdateDto(String uid, String time, String location, String body) {
        this.uid = uid;
        this.time = time;
        this.location = location;
        this.body = body;
    }
}