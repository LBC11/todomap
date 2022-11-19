package MP.todomap.domain.repository;

import MP.todomap.domain.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query(" select t from Todo t where t.uid = :uid ")
    public List<Todo> findAllByTodoUid(@Param("uid") String uid);
}
