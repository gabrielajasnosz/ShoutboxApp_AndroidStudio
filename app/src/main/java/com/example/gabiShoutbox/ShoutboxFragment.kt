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
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ShoutboxFragment : Fragment(), MessageAdapter.OnItemClickListener {
    private lateinit var infoToast: Toast
    private lateinit var messagesData: ArrayList<Message>
    private lateinit var jsonPlaceholderAPI: JsonPlaceholderAPI
    private lateinit var retrofit: Retrofit
    private lateinit var addMessage: ImageButton
    private lateinit var enterMessage: EditText
    private lateinit var recyclerView: RecyclerView
    private val baseUrl: String = "http://tgryl.pl/"
    private var login: String = ""



    val ex = Executors.newSingleThreadScheduledExecutor()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_shoutbox, container, false)

        recyclerView= root.findViewById(R.id.recyclerView)
        addMessage = root.findViewById(R.id.addMessage)
        enterMessage = root.findViewById(R.id.enterMessage)

        retrofit = Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(
                GsonConverterFactory
                    .create()
            )
            .build()
        jsonPlaceholderAPI = retrofit.create(JsonPlaceholderAPI::class.java)

        refreshData()

        addMessage.setOnClickListener {
            val prefs =
                requireActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
            login= prefs.getString("login","").toString();
            val mess = Message(login!!, enterMessage.text.toString())
            if (networkConnection()) {
                sendMessage(mess)
            }
            else{
                makeToast("No internet connection")
            }
        }

        var swipeRefresh: SwipeRefreshLayout = root.findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            if (networkConnection()) {
                getData(jsonPlaceholderAPI)
                swipeRefresh.isRefreshing = false
                makeToast("Data refreshed.")
            } else {
                swipeRefresh.isRefreshing = false
                makeToast("No internet connection.")
            }
        }
        return root
    }

    fun update() {
        if (recyclerView != null) {
            messagesData.reverse();
            recyclerView.adapter = MessageAdapter(messagesData, this)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.setHasFixedSize(true)

            val prefs =
                requireActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
            login= prefs.getString("login","").toString();

            val swipeHandler = object : SwipeToDeleteCallBack(context){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = recyclerView.adapter as MessageAdapter
                    val mess = adapter.getItem(viewHolder.adapterPosition)
                    if (networkConnection()) {
                        if (login == mess.login) {
                            mess.id?.let { deleteData(it) };
                            adapter.removeAt(viewHolder.adapterPosition)
                            makeToast("Data deleted.")
                        } else {
                            makeToast("This is not your message")
                            getData(jsonPlaceholderAPI);
                        }
                    }
                    else{
                        makeToast("No internet connection.")
                    }
                }
            }

            val itemTouchHelper = ItemTouchHelper(swipeHandler)
            itemTouchHelper.attachToRecyclerView(recyclerView)

        }
    }

    fun getData(jsonPlaceholderAPI: JsonPlaceholderAPI) {
        val call = jsonPlaceholderAPI.getMessageArray()
        call!!.enqueue(object : Callback<ArrayList<Message>?> {
            override fun onResponse(
                call: Call<ArrayList<Message>?>,
                response: Response<ArrayList<Message>?>
            ) {
                if (!response.isSuccessful) {
                    println("Code: " + response.code())
                    return
                }
                messagesData = response.body()!!
                update()
            }

            override fun onFailure(
                call: Call<ArrayList<Message>?>,
                t: Throwable
            ) { println(t.message)
            }
        })
    }

    fun networkConnection(): Boolean {
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
        val prefs =
            requireActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        login= prefs.getString("login","").toString();
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
            makeToast("You can't change this message.")
        }
    }

    fun refreshData() {
        ex.scheduleAtFixedRate({
            if (networkConnection()) {
                getData(jsonPlaceholderAPI)
                makeToast("Data refreshed automatically.")
            } else {
                makeToast("Can't refresh data.")
            }
        }, 0, 30, TimeUnit.SECONDS)
    }


    private fun sendMessage(MyMessage: Message) {

        val call = jsonPlaceholderAPI.createPost(MyMessage)
        call.enqueue(object : Callback<Message> {
            override fun onFailure(call: Call<Message>,
                                   t: Throwable
            ) {
                makeToast("Can't send message")
            }
            override fun onResponse(
                call: Call<Message>,
                response: Response<Message>
            ) {
                if (!response.isSuccessful) {
                    println("Code: " + response.code())
                    return
                }
                makeToast("Message sent.")
            }
        })
    }

    private fun deleteData(id:String) {
        val call = jsonPlaceholderAPI.createDelete(id)
        call.enqueue(object : Callback<Message> {
            override fun onResponse(
                call: Call<Message>,
                response: Response<Message>
            ) {
                if (!response.isSuccessful) {
                    println("Code: " + response.code())
                    return
                }
            }
            override fun onFailure(
                call: Call<Message>,
                t: Throwable
            ) {
                println(t.message)
            }
        })
    }

}




