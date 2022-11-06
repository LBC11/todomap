package com.example.todomap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class TodoViewModel(application: Application): AndroidViewModel(application) {

    private val repository = TodoRepository(application)
    private val _date = MutableLiveData<String>()

    val date: LiveData<String>
        get() = _date

    fun insert(todo: TodoEntity) {
        repository.insert(todo)
    }

    fun delete(todo: TodoEntity){
        repository.delete(todo)
    }

    fun getAllByDate(date: String): LiveData<List<TodoEntity>?>{
        return repository.getAllByDate(date)
    }

    fun getAll(): LiveData<List<TodoEntity>>{
        return repository.getAll()
    }

    fun updateDate(date: String){
        _date.value = date
    }
}