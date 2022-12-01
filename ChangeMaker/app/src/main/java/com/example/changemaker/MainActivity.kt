package com.example.changemaker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.changemaker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var price = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If there are stored data, restore it.
        if(savedInstanceState != null) {
            price = savedInstanceState.getString("price").toString()

            // To prevent the RuntimeException caused by the empty string
            if(price != "") {
                operate()
            }
        }

        // When each button is pressed, add the corresponding number
        // and call the operate() fun.
        binding.btn1.setOnClickListener{
            price += "1"
            operate()
        }
        binding.btn2.setOnClickListener{
            price += "2"
            operate()
        }
        binding.btn3.setOnClickListener{
            price += "3"
            operate()
        }
        binding.btn4.setOnClickListener{
            price += "4"
            operate()
        }
        binding.btn5.setOnClickListener{
            price += "5"
            operate()
        }
        binding.btn6.setOnClickListener{
            price += "6"
            operate()
        }
        binding.btn7.setOnClickListener{
            price += "7"
            operate()
        }
        binding.btn8.setOnClickListener{
            price += "8"
            operate()
        }
        binding.btn9.setOnClickListener{
            price += "9"
            operate()
        }
        binding.btn0.setOnClickListener{
            price += "0"
            operate()
        }

        // Change to Init value.
        binding.btnClear.setOnClickListener {
            binding.price.text = ""
            price = ""
            binding.D20.text = "0"
            binding.D10.text = "0"
            binding.D5.text = "0"
            binding.D1.text = "0"
            binding.C25.text = "0"
            binding.C10.text = "0"
            binding.C5.text = "0"
            binding.C1.text = "0"
        }
    }

    private fun operate() {
        // To express the decimal point
        binding.price.text = (price.toDouble()/100).toString()

        // Convert type to Long for calculation.
        var priceLong = price.toLong()

        // After calculating at each stage,
        // change the text to the result value.
        binding.D20.text = (priceLong / 2000).toString()
        priceLong %= 2000

        binding.D10.text = (priceLong / 1000).toString()
        priceLong %= 1000

        binding.D5.text = (priceLong / 500).toString()
        priceLong %= 500

        binding.D1.text = (priceLong / 100).toString()
        priceLong %= 100

        binding.C25.text = (priceLong / 25).toString()
        priceLong %= 25

        binding.C10.text = (priceLong / 10).toString()
        priceLong %= 10

        binding.C5.text = (priceLong / 5).toString()
        priceLong %= 5

        binding.C1.text = (priceLong).toString()
    }

    // Save price before activity is destroyed.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("price", price)
    }
}