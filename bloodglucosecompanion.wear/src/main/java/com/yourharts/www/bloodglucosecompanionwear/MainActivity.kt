package com.yourharts.www.bloodglucosecompanionwear

import android.app.AlertDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.provider.SyncStateContract
import android.support.design.widget.CoordinatorLayout
import android.support.wearable.activity.WearableActivity
import android.text.format.Time
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.wearable.Node
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity() : WearableActivity() {
    private var _currentInputIndex: Int = 0
    private var _dialog: AlertDialog? = null
    private var _glucoseMeasurement: Float = 0.0f
    private var _shortLastingDose: Int = 0
    private var _longLastingDose: Int = 0
    private var _notes: String = ""
    private var dialogView: View? = null
    private var cancelBtn: ImageButton? = null
    private var okBtn: ImageButton? = null
    private var valuePicker: NumberPicker? = null
    private var decimalPicker: NumberPicker? = null
    private var decimalPointTv: TextView? = null
    private var _notesTB: EditText? = null
    private var _node : Node? = null
    private val CONNECTION_TIME_OUT_MS: Long = 100
    private val MESSAGE_PATH = "/glucose"
    private var _apiClient: GoogleApiClient? = null
    private var _statusTextView : TextView?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_main)

        setupViews()

        // Enables Always-on
        setAmbientEnabled()
        val addMeasurementBtn = findViewById<ImageButton>(R.id.addMeasurementBtn)
        addMeasurementBtn?.setOnClickListener {

            val builder = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setMessage("Blood Glucose")


            _dialog = builder.create()
            valuePicker?.minValue = 2
            valuePicker?.maxValue = 45
            decimalPicker?.minValue = 0
            decimalPicker?.maxValue = 9
            decimalPicker?.visibility = View.VISIBLE
            decimalPointTv?.visibility = View.VISIBLE
            setupListeners()
            _dialog?.show()
        }
        _currentInputIndex = 0
        initApi()
        _statusTextView = findViewById(R.id.statusTV)



    }

    private fun initApi() {
        _apiClient = getGoogleApiClient(this)
        retrieveDeviceNode()
    }
    private fun setupListeners() {
        okBtn?.setOnClickListener({
            _currentInputIndex++
            when (_currentInputIndex) {
                1 -> {
                    _glucoseMeasurement = valuePicker?.value?.toFloat()!! + ((decimalPicker?.value?.toFloat())!! / 10)
                    valuePicker?.minValue = 0
                    valuePicker?.maxValue = 45
                    valuePicker?.value = 0
                    _dialog?.setMessage("Corrective Dose")

                    decimalPicker?.visibility = View.GONE
                    decimalPointTv?.visibility = View.GONE
                    val params = CoordinatorLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.setMargins(0, 0, 0, 0)
                    params.height = 100
                    params.width = 40
                    params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
                    valuePicker?.layoutParams = params;


                }
                2 -> {
                    _dialog?.setMessage("Baseline Dose")
                    _shortLastingDose = valuePicker?.value!!
                    valuePicker?.minValue = 0
                    valuePicker?.maxValue = 100
                    valuePicker?.value = 0
                }
                3 -> {
                    _dialog?.setMessage("Notes")
                    _longLastingDose = valuePicker?.value!!
                    _notesTB?.visibility = View.VISIBLE
                    valuePicker?.visibility = View.GONE

                }
                4 -> {
                    _notes = _notesTB?.text.toString()
                   _currentInputIndex = 0
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                    val date = Date()
                    var message =  _glucoseMeasurement.toString()+"~"+dateFormat.format(date)+"~"+_shortLastingDose+"~"+_longLastingDose+"~"+_notes
                    sendToPhone(message)
                    _dialog?.dismiss()
                    setupViews()
                }


            }


        })
        cancelBtn?.setOnClickListener({
            _currentInputIndex = 0
            _dialog?.dismiss()
            setupViews()
        })
    }

    private fun sendToPhone(message: String) {

        if (_node != null) {
            sendMessage().execute(message)
        }
        else{
            System.out.println("Sending failed!")
            Toast.makeText(this, "Sending information failed. Are you connected to your watch?", Toast.LENGTH_LONG )
        }
    }
    inner class sendMessage : AsyncTask<String, Void, com.google.android.gms.common.api.Status?> (){


        override fun doInBackground(vararg message: String): com.google.android.gms.common.api.Status? {
            _apiClient?.connect()
            var packet = message[0].toByteArray()
            var status = Wearable.MessageApi.sendMessage(_apiClient, _node?.id, MESSAGE_PATH, packet).await()
            _apiClient?.disconnect()
            return status.status
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // ...
        }

        override fun onPostExecute(result: com.google.android.gms.common.api.Status?) {
            super.onPostExecute(result)
            if(result?.isSuccess == true){
                System.out.println("Sending to node "+_node +" was successful!")
                val builder = AlertDialog.Builder(this@MainActivity)
                        .setTitle("Success!")
                        .setMessage("Measurement successfully sent to watch.")
                        .setNegativeButton("Got it", { dialog, which ->
                    // close dialog
                })
                val dialog = builder.create()
                dialog.show()
            }
            else{

            }
        }
    }
    private fun setupViews() {
        dialogView = this.layoutInflater.inflate(R.layout.layout_add_measurement, null)
        cancelBtn = dialogView?.findViewById<ImageButton>(R.id.cancelBtn)
        okBtn = dialogView?.findViewById<ImageButton>(R.id.okBtn)
        valuePicker = dialogView?.findViewById<NumberPicker>(R.id.addMeasurementNP);
        decimalPicker = dialogView?.findViewById<NumberPicker>(R.id.addMeasurementDpNP)
        decimalPointTv = dialogView?.findViewById<TextView>(R.id.decimalPointTV);
        _notesTB = dialogView?.findViewById<EditText>(R.id.notesTB)
    }

    private fun getGoogleApiClient(context: Context): GoogleApiClient {
        return GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build()
    }

    private fun retrieveDeviceNode() {
        System.out.println("Retrieving nodes...")

        Thread(Runnable {
            _apiClient?.connect()

            val result = Wearable.NodeApi.getConnectedNodes(_apiClient).await()
            val nodes = result.nodes
            System.out.println("Nodes retrieved:"+nodes.size)
            if (nodes.size > 0) {
                System.out.println("We're taking node: "+(nodes[0].displayName))
                _node = nodes[0]
                this@MainActivity.runOnUiThread( java.lang.Runnable {  _statusTextView?.setText("Connected to "+_node?.displayName)})
            }
            _apiClient?.disconnect()
        }).start()
    }

}
