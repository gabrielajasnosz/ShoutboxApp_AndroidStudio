package com.example.gabiShoutbox

import JsonPlaceholderAPI
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_shoutbox.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ShoutboxFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_shoutbox, container, false)

        //////////json
        val baseUrl = "http://tgryl.pl/"
        val retrofit = Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(
                GsonConverterFactory
                    .create()
            )
            .build()
        val jsonPlaceholderAPI = retrofit.create(JsonPlaceholderAPI::class.java)
        createJsonGet(jsonPlaceholderAPI)
        ////API
        return root
    }

    fun createJsonGet(jsonPlaceholderAPI: JsonPlaceholderAPI) {
        val call = jsonPlaceholderAPI.getMessageArray()
        call!!.enqueue(object : Callback<Array<Message>?> {
            override fun onResponse(
                call: Call<Array<Message>?>,
                response: Response<Array<Message>?>
            ) {
                if (!response.isSuccessful) {
                    println("Code: " + response.code())
                    return
                }
                val messagesData = response.body()!!
                recyclerView.adapter = MessageAdapter(messagesData)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.setHasFixedSize(true)
            }

            override fun onFailure(
                call: Call<Array<Message>?>,
                t: Throwable
            ) {
                println(t.message)
            }
        })
    }

}
