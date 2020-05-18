package com.example.gabiShoutbox

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_shoutbox.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ShoutboxFragment : Fragment(), MessageAdapter.OnItemClickListener {
    private lateinit var infoToast: Toast
    private lateinit var messagesData: Array<Message>
    private val baseUrl: String = "http://tgryl.pl/"
    private lateinit var login: String
    private lateinit var jsonPlaceholderAPI: JsonPlaceholderAPI
    private lateinit var retrofit: Retrofit

    val thread = Executors.newSingleThreadScheduledExecutor()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_shoutbox, container, false)

        //////////json
        retrofit = Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(
                GsonConverterFactory
                    .create()
            )
            .build()
        jsonPlaceholderAPI = retrofit.create(JsonPlaceholderAPI::class.java)
        ////json
        login = arguments?.getString("login").toString()
        beginRefreshing()

        var swipeRefresh: SwipeRefreshLayout = root.findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            if (checkNetworkConnection()) {
                getAndShowData(jsonPlaceholderAPI)
                swipeRefresh.isRefreshing = false
                makeToast("Odswiezono wiadomosci")
            } else {
                makeToast("Brak polaczenia z internetem")
            }
        }
        return root
    }

    fun updateData() {
        if (recyclerView != null) {
            messagesData.reverse();
            recyclerView.adapter = MessageAdapter(messagesData, this)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.setHasFixedSize(true)
        }
    }

    fun getAndShowData(jsonPlaceholderAPI: JsonPlaceholderAPI) {
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
                messagesData = response.body()!!
                updateData()
            }

            override fun onFailure(
                call: Call<Array<Message>?>,
                t: Throwable
            ) {
                println(t.message)
            }
        })
    }

    fun checkNetworkConnection(): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    fun makeToast(myToastText: String) {
        infoToast = Toast.makeText(
            context,
            myToastText,
            Toast.LENGTH_SHORT
        )
        infoToast.setGravity(Gravity.TOP, 0, 200)
        infoToast.show()
    }

    override fun onItemClick(//dzialanie edycji - klikniecia na cokolwiek z listy wiadomosci
        item: Message, position: Int
    ) {
        if (login == item.login) {
            val bundle = Bundle()
            bundle.putString("login", item.login)
            bundle.putString("id", item.id)
            bundle.putString("date_hour", item.date)
            bundle.putString("content", item.content)
            val fragment: Fragment = EditFragment()
            fragment.arguments = bundle
            val fragmentManager: FragmentManager? = fragmentManager
            fragmentManager?.beginTransaction()
                ?.replace(R.id.nav_host_fragment, fragment)
                ?.remove(this)
                ?.commit()
        } else {
            makeToast("You can only edit your own messages!!!")
        }
    }

    fun beginRefreshing() {
        thread.scheduleAtFixedRate({
            if (checkNetworkConnection()) {
                getAndShowData(jsonPlaceholderAPI)
                Log.d("Executors thread: ", "Odswiezono wiadomosci")
            } else {
                Log.d("Executors thread: ", "Brak polaczenia z internetem")
            }
        }, 0, 10, TimeUnit.SECONDS)
    }

}

