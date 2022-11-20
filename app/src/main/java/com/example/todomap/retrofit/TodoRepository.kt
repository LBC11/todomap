package com.example.todomap.retrofit

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.todomap.retrofit.dto.TodoCreate
import com.example.todomap.retrofit.dto.TodoEntity
import com.example.todomap.retrofit.service.RetrofitService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


//Repository: 여러 데이터 소스에 대한 액세스를 추상화하는 클래스, TodoRepository 클래스는 데이터 작업을 처리
//데이터 연결 후 수정 예정
class TodoRepository(application: Application){

    fun insert(todoCreate: TodoCreate){
        RetrofitService.todoService.createTodo(todoCreate).enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                if(response.isSuccessful) {
                    Log.d("ITM", "${response.body()}")

                }
            }

            override fun onFailure(call: Call<Long>, t: Throwable) {
                Log.d("ITM", t.toString())
            }
        })
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