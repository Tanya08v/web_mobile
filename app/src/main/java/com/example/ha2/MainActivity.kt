package com.example.ha2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.ha2.api.Api
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity(),AdapterView.OnItemSelectedListener {


    private var points = arrayOf("Hostel 1", "Hostel 2", "Hostel 3")
    private val client = OkHttpClient()
    private var spinner:Spinner? = null

    companion object {
        lateinit var login: String
        var accessPoint: Long = 0
        const val server = "http://ha.zzz.com.ua/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinner = this.access_point
        spinner!!.setOnItemSelectedListener(this)
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, points)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner!!.setAdapter(aa)

        val intent = intent
        val message = findViewById<TextView>(R.id.message)
        message.text = intent.getStringExtra("message")

        var et_user_name = findViewById(R.id.et_user_name) as EditText
        var et_password = findViewById(R.id.et_password) as EditText
        var btn_reset = findViewById(R.id.btn_reset) as Button
        var btn_submit = findViewById(R.id.btn_submit) as Button

        btn_reset.setOnClickListener {
            et_user_name.setText("")
            et_password.setText("")
        }

        btn_submit.setOnClickListener {
            val login = et_user_name.text.toString()
            val password = et_password.text.toString()
            MainActivity.login = login


            Timer("SettingUp", false).schedule(1 * 60 * 1000) {
                if (MainActivity.login != "") {
                    MainActivity.login = ""
                    this@MainActivity.reload("Login expired")
                }
            }
            val ts = System.currentTimeMillis() / 1000
            val helper = Helper()
            val api = Api()
            run(
                api.getRequestUrl(
                    "api/user_token?ts=".plus(ts.toString()).plus("&l=").plus(login).plus("&hash=").plus(
                        helper.sha1(
                            login.plus(" ").plus(
                                helper.md5(password)
                            ).plus(ts.toString())
                        )
                            .plus("&ap=")
                            .plus(MainActivity.accessPoint.toString())
                    )
                )
            )
        }
    }

    override fun onItemSelected(arg0: AdapterView<*>, arg1: View, position: Int, id: Long) {
        MainActivity.accessPoint = id + 1
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {

    }

    fun reload(message: String) {
        val intent = Intent(this, this::class.java)
        if (message != "") {
            intent.putExtra("message", message)
        }
        startActivity(intent)
    }

    private fun run(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonResponse = response.body()?.string()
                if (jsonResponse != null) {
                    val json = JSONObject(jsonResponse)
                    Log.e("status", json.getInt("status").toString())
                    if (json.getInt("status") == 200) {
                        val responseData = json.getJSONObject("response_data")
                        val intent = Intent(this@MainActivity, ScannerActivity::class.java)
                        intent.putExtra("token", responseData.getString("token"))
                        intent.putExtra("ts", responseData.getLong("ts"))
                        intent.putExtra("r", responseData.getLong("r"))
                        Log.e("access_point", MainActivity.accessPoint.toString())
                        startActivity(intent)
                    } else {
                        this@MainActivity.reload("Incorrect login or password")
                    }
                } else {
                    this@MainActivity.reload("Incorrect login or password")
                }
            }
        })
    }
}