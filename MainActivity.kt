package com.starkprojects.upidemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var UPI_PAYMENT:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buSend.setOnClickListener {
            var amount = etAmount.text.toString()
            var note = etNote.text.toString()
            var name = etName.text.toString()
            var upiID = etUpi.text.toString()
            payUsingUpi(amount, upiID, name, note)
        }
    }

    fun payUsingUpi (amount:String, upiID:String, name:String, note:String ){
        var uri:Uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiID)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn",note)
            .appendQueryParameter("am",amount)
            .appendQueryParameter("cu","INR")
            .build()
        var upiPayIntent:Intent = Intent(Intent.ACTION_VIEW)
        upiPayIntent.setData(uri)

        // will always show a dialog to user to choose an app
        var chooser:Intent = Intent.createChooser(upiPayIntent,"Pay with")

        // check if intent resolves
        if(null != chooser.resolveActivity(packageManager)){
            startActivityForResult(chooser,UPI_PAYMENT)
        }
        else{
            Toast.makeText(this@MainActivity,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            UPI_PAYMENT -> {
                if((Activity.RESULT_OK == resultCode) || (resultCode == 11)){
                    if(data!= null){
                        var trxt:String = data.getStringExtra("response");
                        Log.d("UPI","onActivityResult: "+trxt)
                        var dataList:ArrayList<String> = ArrayList()
                        dataList.add(trxt)
                        upiPaymentDataOperation(dataList)
                    }
                    else{
                        Log.d("UPI", "onActivityResult: " + "Return data is null")
                        var dataList:ArrayList<String> = ArrayList()
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList)
                    }
                }

                else{
                    Log.d("UPI", "onActivityResult: " + "Return data is null");
                    //when user simply back without payment
                    var dataList:ArrayList<String> = ArrayList()
                    dataList.add("nothing")
//                    upiPaymentDataOperation(dataList)
                    upiPaymentDataOperation(dataList)
                }
            }
        }
    }

    private fun upiPaymentDataOperation(data: ArrayList<String>) {
        if (isConnectionAvailable(this@MainActivity)) {
            var str = data[0]
            Log.d("UPIPAY", "upiPaymentDataOperation: $str")
            var paymentCancel = ""
            if (str == null) str = "discard"
            var status = ""
            var approvalRefNo = ""
            val response = str.split("&".toRegex()).toTypedArray()
            for (i in response.indices) {
                val equalStr =
                    response[i].split("=".toRegex()).toTypedArray()
                if (equalStr.size >= 2) {
                    if (equalStr[0].toLowerCase() == "Status".toLowerCase()) {
                        status = equalStr[1].toLowerCase()
                    } else if (equalStr[0]
                            .toLowerCase() == "ApprovalRefNo".toLowerCase() || equalStr[0]
                            .toLowerCase() == "txnRef".toLowerCase()
                    ) {
                        approvalRefNo = equalStr[1]
                    }
                } else {
                    paymentCancel = "Payment cancelled by user."
                }
            }
            if (status == "success") {
                //Code to handle successful transaction here.
                Toast.makeText(this@MainActivity, "Transaction successful.", Toast.LENGTH_SHORT)
                    .show()
                Log.d("UPI", "responseStr: $approvalRefNo")
            } else if ("Payment cancelled by user." == paymentCancel) {
                Toast.makeText(this@MainActivity, "Payment cancelled by user.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Transaction failed.Please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this@MainActivity,
                "Internet connection is not available. Please check and try again",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    fun isConnectionAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork:Boolean = connectivityManager.isDefaultNetworkActive
        return activeNetwork
    }
}
