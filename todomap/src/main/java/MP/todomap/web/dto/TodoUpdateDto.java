package MP.todomap.web.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TodoUpdateDto {

    private String uid;
    private String date;
    private String time;
    private String locName;
    private String locLatitude;
    private String locLongitude;
    private String description;

    @Builder
    public TodoUpdateDto(String uid, String date, String time, String locName, String locLatitude, String locLongitude, String description) {
        this.uid = uid;
        this.date = date;
        this.time = time;
        this.locName = locName;
        this.locLatitude = locLatitude;
        this.locLongitude = locLongitude;
        this.description = description;
    }
}