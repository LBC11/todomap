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
    public void create(TodoCreationDto creationDto) {
        repository.save(creationDto.toEntity());
    }

    @Transactional(readOnly = true)
    public List<TodoReadDto> readAllByUid(String uid) {
        return repository.findAllByUid(uid)
                .stream().map(TodoReadDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TodoReadDto> readAllByUidAndDate(String uid, String date) {
        return repository.findAllByUidAndDate(uid, date)
                .stream().map(TodoReadDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, TodoUpdateDto updateDto) {
        Todo todo = repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("update error!!! id:"+id));
        todo.update(updateDto.getUid(), updateDto.getDate(), updateDto.getTime(), updateDto.getLocName(), updateDto.getLocLatitude(), updateDto.getLocLongitude(), updateDto.getDescription());
    }

    @Transactional
    public void delete(Long id) {
        Todo todo = repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("delete error!!! id:"+id));
        repository.delete(todo);
    }
}
