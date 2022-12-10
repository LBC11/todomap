package com.example.todomap.todo

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.todomap.retrofit.model.TodoCreate
import com.example.todomap.retrofit.model.TodoEntity
import com.example.todomap.retrofit.model.TodoUpdate
import com.example.todomap.retrofit.service.RetrofitService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TodoRepository() {

    private val _todoList = MutableLiveData<List<TodoEntity>>()
    private var _tempList = emptyList<TodoEntity>()

    suspend fun insert(todoCreate: TodoCreate){
        runBlocking {
            this.launch {
                RetrofitService.todoService.createTodo(todoCreate)
            }
        }
    }

    suspend fun update(id: Long, todoUpdate: TodoUpdate){
        runBlocking {
            this.launch {
                RetrofitService.todoService.updateTodo(id, todoUpdate)
            }
        }
    }

    suspend fun getAllByDate(uid: String, date: String): LiveData<List<TodoEntity>> {
        updateList(uid, date)
        _todoList.value = _tempList
        return _todoList
    }

     private suspend fun updateList(uid: String, date: String) {
        runBlocking {
            this.launch {
                _tempList = RetrofitService.todoService.getTodosByDate(uid, date)
            }
        }
    }

    suspend fun delete(id: Long){
        runBlocking {
            this.launch {
                RetrofitService.todoService.deleteTodo(id)
            }
        }
    }


    fun getAll(uid: String): List<TodoEntity>{
        var result = emptyList<TodoEntity>()
        runBlocking {
            this.launch {
                result = RetrofitService.todoService.getTodos(uid)
            }
        }
        return result
    }
}