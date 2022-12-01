package com.example.lotto

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lotto.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.prefs.Preferences

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lottos :MutableList<Lotto> = arrayListOf()

    // For the convert the lotto list to String.
    private val token: TypeToken<MutableList<Lotto>> = object : TypeToken<MutableList<Lotto>>() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // To save the data in the preference
        val prefer = getSharedPreferences("saveLotto",MODE_PRIVATE)

        // Call the load function and assign the data to lotto list.
        lottos = load(prefer)

        // Setting for the Recycler view
        val lottoAdapter = LottoAdapter(lottos)
        binding.LottoRecycler.adapter = lottoAdapter
        binding.LottoRecycler.layoutManager = LinearLayoutManager(this)
        binding.LottoRecycler.setHasFixedSize(true)

        binding.btnGenerator.setOnClickListener {
            // When the generator button is clicked, create a lotto instance and save it in the list.
            lottos.add(createLotto())
            // Notify the adapter that the list has been changed and save the list in the preference.
            lottoAdapter.notifyItemInserted(lottos.size-1)
            save(prefer)
        }

        binding.btnReset.setOnClickListener {
            // When the Reset button is clicked, empty the lotto list.
            val size = lottos.size
            lottos.clear()
            // Notify the adapter that the list has been changed and save the list in the preference.
            lottoAdapter.notifyItemRangeRemoved(0,size)
            save(prefer)
        }
    }

    private fun createLotto(): Lotto {
        // Create a Lotto instance with 6 random numbers between 1 and 45
        val temp = (1..45).shuffled().take(6).sorted()
        return(Lotto(temp[0],temp[1],temp[2],temp[3],temp[4],temp[5]))
    }

    private fun load(prefer: SharedPreferences): MutableList<Lotto> {
        // Return the Data existing in the preference, if there are not, return the empty list.
        return try{
            Gson().fromJson(prefer.getString("saveLotto",""), token.type)
        }catch (e: NullPointerException) {
            arrayListOf()
        }
    }

    private fun save(prefer: SharedPreferences) {
        // Save the lotto list in the preference
        with (prefer.edit()) {
            putString("saveLotto", Gson().toJson(lottos, token.type))
            apply()
        }
    }
}