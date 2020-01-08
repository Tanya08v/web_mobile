package com.example.ha2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import me.dm7.barcodescanner.zxing.ZXingScannerView
import com.google.zxing.Result
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {


    private var mScannerView: ZXingScannerView? = null
    private val client = OkHttpClient()
    private var token: String? = ""
    private var ts: Long = 0
    private var r: Long = 0


    companion object {
        var isEntrance: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        changeLabel()
        Log.e("on view", "token")
        val intent = intent
        this.token = intent.getStringExtra("token")
        this.ts = intent.getLongExtra("l", 0)
        this.r = intent.getLongExtra("r", 0)

        mScannerView = ZXingScannerView(this)   // Programmatically initialize the scanner view
        setContentView(mScannerView)
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera()          // Start camera on resume
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()           // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        // Do something with the result here
        // Log.v("tag", rawResult.getText()); // Prints scan results
        // Log.v("tag", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)


        this.run(
            rawResult.text.
                plus("&l=").
                plus(MainActivity.login).
                plus("&hash=").
                plus(this.token).
                plus("&ts=").
                plus(this.ts).
                plus("&p=").
                plus(MainActivity.accessPoint).
                plus("&t=").
                plus(if (ScannerActivity.isEntrance)  1  else 2)
        )
        onBackPressed()

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }

    fun reload(message: String) {
        val intent = Intent(this, this::class.java)
        if (message != "") {
            intent.putExtra("message", message)
        }
        startActivity(intent)
    }

    fun onCheckboxClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            when (view.id) {
                R.id.is_enter -> {
                    ScannerActivity.isEntrance = checked
                    changeLabel()
                }
            }
        }
    }

    private fun changeLabel() {
        val entranceLabel: TextView = findViewById(R.id.is_entrance_label) as TextView
        if (ScannerActivity.isEntrance) {
            entranceLabel.text = "Enter"
        } else {
            entranceLabel.text = "Exit"
        }
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
                        var status: String
                        if (json.getBoolean("has_access")) {
                            status = "Has ccess"
                        } else {
                            status = "Has not access"
                        }
//                        val responseData = json.getJSONObject("response_data")
                        val intent = Intent(this@ScannerActivity, ScannerActivity::class.java)
                        intent.putExtra("message", status)
//                        intent.putExtra("token", responseData.getString("token"))
//                        intent.putExtra("ts", responseData.getLong("ts"))
//                        intent.putExtra("r", responseData.getLong("r"))
//                        Log.e("access_point", MainActivity.accessPoint.toString())
                        startActivity(intent)
                    } else {
                        this@ScannerActivity.reload("Incorrect request")
                    }
                } else {
                    this@ScannerActivity.reload("Incorrect request")
                }
            }
        })
    }
}
