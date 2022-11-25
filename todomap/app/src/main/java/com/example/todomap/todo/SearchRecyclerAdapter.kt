package com.example.todomap.todo

import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.example.todomap.databinding.PlaceitemRecyclerBinding

class SearchRecyclerAdapter: RecyclerView.Adapter<SearchRecyclerAdapter.SearchResultViewHolder>() {

    private var searchResultList: List<SearchResultEntity> = listOf()
    var currentPage = 1
    var currentSearchString = ""

    private lateinit var searchResultClickListener: OnItemClickListener

    inner class SearchResultViewHolder(private val binding: PlaceitemRecyclerBinding,
                                       private val searchResultClickListener: (SearchResultEntity) -> Unit): RecyclerView.ViewHolder(binding.root){
        fun bindData(data: SearchResultEntity){
            binding.root.setOnClickListener {
                searchResultClickListener(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}