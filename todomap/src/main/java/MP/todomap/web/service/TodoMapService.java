package MP.todomap.web.service;

import MP.todomap.domain.model.Todo;
import MP.todomap.domain.repository.TodoRepository;
import MP.todomap.web.dto.TodoCreationDto;
import MP.todomap.web.dto.TodoUpdateDto;
import MP.todomap.web.dto.TodoReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoMapService {
    private final TodoRepository repository;

    @Transactional
    public Long create(TodoCreationDto creationDto) {
        return repository.save(creationDto.toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public List<TodoReadDto> readByUid(String uid) {
        return repository.findAllByTodoUid(uid)
                .stream().map(TodoReadDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long update(Long id, TodoUpdateDto updateDto) {
        Todo todo = repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("update error!!! id:"+id));
        todo.update(updateDto.getUid(), updateDto.getTime(), updateDto.getLocation(), updateDto.getBody());
        return id;
    }

    @Transactional
    public void delete(Long id) {
        Todo todo = repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("delete error!!! id:"+id));
        repository.delete(todo);
    }
}
