package com.example.todomap

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


//Repository: 여러 데이터 소스에 대한 액세스를 추상화하는 클래스, TodoRepository 클래스는 데이터 작업을 처리
//데이터 연결 후 수정 예정
class TodoRepository(application: Application){
    private val todoDAO: TodoDAO

    init {
        var db = TodoDatabase.getInstance(application)
    }

    fun insert(todo: TodoEntity){
        todoDAO.insert(todo)
    }

    fun getAllByDate(date: String): LiveData<List<TodoEntity>?> {
        return todoDAO.getAllByDate(date)
    }

    fun getAll(): LiveData<List<TodoEntity>>{
        return todoDAO.getAll()
    }

    fun delete(todo: TodoEntity){
        GlobalScope.launch(Dispatchers.IO) {
            todoDAO.delete(todo)
        }
    }
}