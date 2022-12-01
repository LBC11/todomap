package com.example.lotto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lotto.databinding.LottoBinding

class LottoAdapter(private val lottos: MutableList<Lotto>): RecyclerView.Adapter<LottoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LottoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lottos[position])
    }

    override fun getItemCount(): Int {
        return lottos.size
    }

    class ViewHolder(private val binding: LottoBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(lotto: Lotto) {
            binding.num1.text = lotto.num1.toString()
            binding.num2.text = lotto.num2.toString()
            binding.num3.text = lotto.num3.toString()
            binding.num4.text = lotto.num4.toString()
            binding.num5.text = lotto.num5.toString()
            binding.num6.text = lotto.num6.toString()
        }
    }
}