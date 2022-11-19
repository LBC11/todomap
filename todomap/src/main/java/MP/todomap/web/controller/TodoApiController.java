package MP.todomap.web.controller;

import MP.todomap.domain.repository.TodoRepository;
import MP.todomap.web.dto.TodoCreationDto;
import MP.todomap.web.dto.TodoReadDto;
import MP.todomap.web.dto.TodoUpdateDto;
import MP.todomap.web.service.TodoMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TodoApiController {

    private final TodoRepository repository;
    private final TodoMapService todoMapService;

    @GetMapping("/")
    public String test() {
        return "test";
    }

    @GetMapping("/{uid}")
    public List<TodoReadDto> readByUid(@PathVariable("uid") String uid) {
        return todoMapService.readByUid(uid);
    }

    @PostMapping("/create")
    public Long create(@RequestBody TodoCreationDto creationDto) {
        return todoMapService.create(creationDto);
    }

    @PatchMapping("/{id}")
    public Long update(@PathVariable("id") Long id, @RequestBody TodoUpdateDto updateDto) {
        return todoMapService.update(id, updateDto);
    }

    @DeleteMapping("/{id}")
    public Long delete(@PathVariable("id") Long id) {
        todoMapService.delete(id);
        return id;
    }
}
