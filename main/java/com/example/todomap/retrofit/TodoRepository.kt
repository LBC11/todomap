package com.example.todomap.retrofit

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.todomap.retrofit.model.TodoCreate
import com.example.todomap.retrofit.model.TodoEntity
import com.example.todomap.retrofit.model.TodoUpdate
import com.example.todomap.retrofit.service.RetrofitService.todoService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


//Repository: 여러 데이터 소스에 대한 액세스를 추상화하는 클래스, TodoRepository 클래스는 데이터 작업을 처리
//데이터 연결 후 수정 예정
class TodoRepository(application: Application){
//
//    suspend fun insert(todoCreate: TodoCreate): Long{
//
//        var result: Long = -1
//        runBlocking {
//            this.launch {
//                result = todoService.createTodo(todoCreate)
//            }
//        }
//        return result
//
//    }
//
//    suspend fun updateTodo(id: Long, todoUpdate: TodoUpdate): Long {
//        var result: Long = -1
//        runBlocking {
//            this.launch {
//                result = todoService.updateTodo(id, todoUpdate)
//            }
//        }
//        return result
//    }
//
//    fun getAllByDate(uid: String, date: String): LiveData<List<TodoEntity>?> {
//        var result: LiveData<List<TodoEntity>?> = MutableLiveData()
//        runBlocking {
//            this.launch {
//                result = todoService.getTodosByDate(uid, date)
//            }
//        }
//        return result
//    }
//
//    fun getAll(uid: String): LiveData<List<TodoEntity>>{
//        var result: LiveData<List<TodoEntity>> = MutableLiveData()
//        runBlocking {
//            this.launch {
//                result = todoService.getTodos(uid)
//            }
//        }
//        return result
//    }
//
//    suspend fun delete(id: Long): Long {
//        var result: Long = -1
//        runBlocking {
//            this.launch {
//                result = todoService.deleteTodo(id)
//            }
//        }
//        return result
//    }
}