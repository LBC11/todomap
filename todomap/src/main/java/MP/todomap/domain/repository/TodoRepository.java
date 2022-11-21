package MP.todomap.domain.repository;

import MP.todomap.domain.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findAllByUid(String uid);

    List<Todo> findAllByUidAndDate(String uid, String date);


}
