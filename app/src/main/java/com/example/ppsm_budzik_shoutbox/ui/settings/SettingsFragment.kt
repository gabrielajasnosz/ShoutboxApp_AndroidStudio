package com.example.ppsm_budzik_shoutbox.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.ppsm_budzik_shoutbox.R
import com.example.ppsm_budzik_shoutbox.ui.shoutbox.ShoutboxFragment
import com.example.ppsm_budzik_shoutbox.ui.shoutbox.ShoutboxViewModel
import kotlinx.android.synthetic.main.activity_main.*

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var editText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val button = root.findViewById<Button>(R.id.loginButton)

        editText = root.findViewById<EditText>(R.id.loginInput)
        loadLogin()

        button.setOnClickListener {
            saveLogin()
            val bundle = Bundle()
            bundle.putString("login", editText.text.toString())
            val fragment: Fragment = ShoutboxFragment()
            fragment.arguments = bundle

            val fragmentManager: FragmentManager? = fragmentManager
            fragmentManager?.beginTransaction()
                ?.replace(R.id.nav_host_fragment, fragment)
                ?.commit()
        }
        return root
    }

    private fun saveLogin() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.saved_login), editText.text.toString())
            commit()
        }
    }

    private fun loadLogin() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val defaultValue = resources.getString(R.string.login)
        editText.setText(sharedPref.getString(getString(R.string.saved_login), defaultValue))
    }
}
