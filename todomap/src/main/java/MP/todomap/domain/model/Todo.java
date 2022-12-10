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

    @Column(nullable = false)
    private String uid;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String time;

    @Column(nullable = false)
    private String locName;

    @Column(nullable = false)
    private String locLatitude;

    @Column(nullable = false)
    private String locLongitude;

    @Column(nullable = false)
    private String description;

    @Builder
    public Todo(String uid, String date, String time, String locName,
                String locLatitude, String locLongitude,
                String description) {
        this.uid = uid;
        this.date = date;
        this.time = time;
        this.locName = locName;
        this.locLatitude = locLatitude;
        this.locLongitude = locLongitude;
        this.description = description;
    }

    public void update(String uid, String date, String time, String locName,
                       String locLatitude, String locLongitude,
                       String description) {
        this.uid = uid;
        this.date = date;
        this.time = time;
        this.locName = locName;
        this.locLatitude = locLatitude;
        this.locLongitude = locLongitude;
        this.description = description;
    }
}
