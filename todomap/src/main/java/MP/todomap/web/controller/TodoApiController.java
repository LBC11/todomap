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

    //test
//    @GetMapping("/")
//    public void test() {
//        for(int i=0; i<5; i++) {
//            todoMapService.create(new TodoCreationDto(
//                    "uid" +1,
//                    "date" +1,
//                    "time" +1,
//                    "la" +i,
//                    "lo" +1,
//                    "des" +1
//            ));
//        }
//
//        for(int i=0; i<5; i++) {
//            todoMapService.create(new TodoCreationDto(
//                    "uid",
//                    "date" +1,
//                    "time" +1,
//                    "la" +i,
//                    "lo" +1,
//                    "des" +1
//            ));
//        }
//
//        for(int i=0; i<5; i++) {
//            todoMapService.create(new TodoCreationDto(
//                    "uid",
//                    "date",
//                    "time" +1,
//                    "la" +i,
//                    "lo" +1,
//                    "des" +1
//            ));
//        }
//    }


    @GetMapping("/{uid}")
    public List<TodoReadDto> readByUid(@PathVariable("uid") String uid) {
        return todoMapService.readAllByUid(uid);
    }

    @GetMapping("/{uid}/{date}")
    public List<TodoReadDto> readByUidAndDate(@PathVariable("uid") String uid, @PathVariable("date") String date) {
        return todoMapService.readAllByUidAndDate(uid,date);
    }

    @PostMapping("/create")
    public void create(@RequestBody TodoCreationDto creationDto) {
        todoMapService.create(creationDto);
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable("id") Long id, @RequestBody TodoUpdateDto updateDto) {
        todoMapService.update(id, updateDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        todoMapService.delete(id);
    }
}
