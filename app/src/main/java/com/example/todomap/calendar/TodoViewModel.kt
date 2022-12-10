package com.example.todomap.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todomap.retrofit.model.TodoCreate
import com.example.todomap.retrofit.model.TodoEntity
import com.example.todomap.retrofit.model.TodoUpdate
import com.example.todomap.todo.TodoRepository

class TodoViewModel: ViewModel() {

    private val _date = MutableLiveData<String>()
    private val repository = TodoRepository()

    val date: LiveData<String>
        get() = _date

    suspend fun insert(todoCreate: TodoCreate){
        repository.insert(todoCreate)
    }

    suspend fun update(id: Long, todoUpdate: TodoUpdate){
        repository.update(id, todoUpdate)
    }

    suspend fun delete(id: Long){
        repository.delete(id)
    }

    suspend fun getAllByDateLive(uid: String, date: String): LiveData<List<TodoEntity>> {
        return repository.getAllByDateLive(uid, date)
    }

    suspend fun getAllByDate(uid: String, date: String): List<TodoEntity> {
        return repository.getAllByDate(uid, date)
    }

    fun updateDate(date: String){
        _date.value = date
    }
}