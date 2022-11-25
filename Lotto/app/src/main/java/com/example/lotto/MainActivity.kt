package com

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lotto.R
import com.example.lotto.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lottos :MutableList<Lotto> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lottoAdapter = LottoAdapter(lottos)
        binding.LottoRecycler.adapter = lottoAdapter
        binding.LottoRecycler.layoutManager = LinearLayoutManager(this)
        binding.LottoRecycler.setHasFixedSize(true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGenerator.setOnClickListener {
            lottos.add(createLotto())
            lottoAdapter.notifyItemInserted(lottos.size)

        }

        binding.btnReset.setOnClickListener {
            lottos.clear()
            lottoAdapter.notifyItemRangeRemoved(0,lottos.size)
        }
    }

    private fun createLotto(): Lotto {
        val temp = (1..45).shuffled().take(6).sorted()
        return(Lotto(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5]))
    }


}