package com.example.todomap.calendar

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todomap.databinding.TodoitemRecyclerBinding
import com.example.todomap.retrofit.model.TodoEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TodoListAdapter(val todoViewModel: TodoViewModel, var context: CalendarFragment) :
    RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {
    private var todoList = ArrayList<TodoEntity>()
    private val TAG = "TodoListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding =
            TodoitemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setTodoListUI(todoList[position])
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    // CalendarFragment에서 todolist 보여줄 때 사용
    fun setTodoList(todo: List<TodoEntity>?) {
        if (todo != null) {
            todoList.clear()
            todoList.addAll(todo)
        } else {
            todoList.clear()
        }
    }

    inner class ViewHolder(private val binding: TodoitemRecyclerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setTodoListUI(todo: TodoEntity) {
            binding.todoDescription.text = todo.description
            binding.alartTimeView.text = todo.time
//            binding.alartLocationView.text = 위치 이름으로 바꿔줘야 함

            binding.todoDeleteBtn.setOnClickListener {
                Log.d(TAG, "${todo.id}")

                GlobalScope.launch {
                    todoViewModel.delete(todo.id)
                }
                val index = todoList.indexOf(todo)
                todoList.remove(todo)
                notifyItemRemoved(index)
            }

        }
    }
}