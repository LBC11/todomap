package com.example.todomap.profile

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.todomap.calendar.TodoListAdapter
import com.example.todomap.databinding.TodoitemRecyclerBinding
import com.example.todomap.databinding.UseritemRecyclerBinding
import com.example.todomap.user.UserAccount

class UsersearchAdapter(var users: ArrayList<UserAccount>, var context: Context) : RecyclerView.Adapter<UsersearchAdapter.ViewHolder>(), Filterable {

    private var filteredUser = ArrayList<UserAccount>()
    private var itemFilter = ItemFilter()
    val TAG = "UsersearchAdapter"

    //filteredUser에 추가
    init {
        filteredUser.addAll(users)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersearchAdapter.ViewHolder {
        val binding = UseritemRecyclerBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setUI(filteredUser[position])
    }

    override fun getItemCount(): Int {
        return filteredUser.size
    }


    inner class ViewHolder(private val binding: UseritemRecyclerBinding): RecyclerView.ViewHolder(binding.root){
        fun setUI(user: UserAccount){
            // user 정보 띄우기
            binding.userName.text = user.userName
            binding.profileImg.setImageURI(Uri.parse(user.profileImgUrl))

            binding.root.setOnClickListener {
                // user 클릭하면 뭘 할건지
            }
        }
    }

    override fun getFilter(): Filter {
        return itemFilter
    }

    inner class ItemFilter: Filter(){

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterString = constraint.toString()
            val results = FilterResults()
            Log.d(TAG, "charSequence : $constraint")

            //검색이 필요없을 경우를 위해 원본 배열을 복제
            val filteredList: ArrayList<UserAccount> = ArrayList<UserAccount>()

            if (filterString.trim { it <= ' ' }.isEmpty()) {
                //공백제외 아무런 값이 없을 경우 -> 원본 배열
                results.values = users
                results.count = users.size
                return results
            } else if (filterString.trim { it <= ' ' }.length >= 2) {
                //공백 제외 2글자이상인 경우 검색
                for (user in users) {
                    if (user.userName.contains(filterString)) {
                        filteredList.add(user)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size

            return results
        }
        
        // 결과 보여주깅
        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredUser.clear()
            filteredUser.addAll(results?.values as ArrayList<UserAccount>)
            notifyDataSetChanged()
        }

    }

}