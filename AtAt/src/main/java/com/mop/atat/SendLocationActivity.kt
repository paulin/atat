package com.mop.atat

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.telephony.SmsManager
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_location_request.*
import kotlinx.android.synthetic.main.activity_send_location.*

/**
 * This was an Activity I was using to test out a bunch of code. Consider it a playground.
 */
class SendLocationActivity : AppCompatActivity(), View.OnClickListener, com.google.android.gms.location.LocationListener {

    private val ZOOM_LEVEL = 12
    private val MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 41
    private val MY_PERMISSIONS_REQUEST_SEND_SMS = 42
    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 43

    private val FAKE_PHONE_NUMBER = "YOURPHONENUMBER"

    private val MAP_STRING_TEST = "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=48.71518,-122.107856&sll=47.61357,-122.33139&sspn=0.471215,1.242828&ie=UTF8&z=" + ZOOM_LEVEL
    private val CONTENT_URI_SENT_MESSAGES = "content://sms/sent";


    private var REQUEST_LOCATION_CODE = 101
    private var mGoogleApiClient: GoogleApiClient? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient;

    var id = 0

    override fun onLocationChanged(location: Location?) {
        // You can now create a LatLng Object for use with maps
        // val latLng = LatLng(location.latitude, location.longitude)
    }


    override fun onClick(v: View?) {
        if (!checkGPSEnabled()) {
            return
        }
        getLocation()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_location)

        try {
            var bundle: Bundle = intent.extras
            id = bundle.getInt("MainActId", 0)
            if (id != 0) {
                edtContent.setText(bundle.getString("MainActContent"))
            }
        } catch (ex: Exception) {
        }

        getAllPermissions()

        btFakeRequest.setOnClickListener {
            Toast.makeText(this, "Fake Request Added", Toast.LENGTH_LONG).show()
            makeFakeRequest()
        }

        btGetGeo.setOnClickListener(this)

        btFormURL.setOnClickListener {
            Toast.makeText(this, "Form URL", Toast.LENGTH_LONG).show()


//            tvLongURL.setText("http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=${tvLatitude.text},${tvLongitude.text}&ie=UTF8&z=$ZOOM_LEVEL")

            var bodyData = "<html> <a href=\\\"http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=${tvLatitude.text},${tvLongitude.text}&ie=UTF8&z=$ZOOM_LEVEL\">Click HERE</a> </html>"
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tvLongURL.setText(Html.fromHtml(bodyData, Html.FROM_HTML_MODE_LEGACY));
            } else {
                tvLongURL.setText(Html.fromHtml(bodyData));
            }
            tvLongURL.setMovementMethod(LinkMovementMethod.getInstance())
            tvLongURL.setOnClickListener {
                Toast.makeText(this, "I'm in the shizzle", Toast.LENGTH_LONG).show()

            }
            //values.put("Content", edtContent.text.toString())
        }

        btShrink.setOnClickListener {
            Toast.makeText(this, "Shrinking URL", Toast.LENGTH_LONG).show()

            var values = ContentValues()

            //values.put("Content", edtContent.text.toString())
        }

        btSend.setOnClickListener {
            Toast.makeText(this, "Sending Geo", Toast.LENGTH_LONG).show()
            sendTextMessages()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvLatitude.text = "Mattitude"
        tvLongitude.text = "Mongitude"

        buildGoogleApiClient()

//        Bitly.initialize(this, "YOUR_APP_ID", Arrays.asList("yourdomain.com","yourotherdomain.com"), Arrays.asList("yourscheme"), new Bitly.Callback() {
//        Bitly.Callback() {
//
//        }
//            public void onResponse(Response response) {
//                // response provides a Response object which contains the full URL information
//                // response includes a status code
//                // Your custom logic goes here...
//            }
//
//            public void onError(Error error) {
//                // error provides any errors in retrieving information about the URL
//                // Your custom logic goes here...
//            }
//        });
    }

    fun makeFakeRequest() {
        //Create a new location request
        var values = ContentValues()
        val tempState = LocationRequestState.NEW.type
        val tempDate = System.currentTimeMillis()
        values.put(LocationRequestDbManager.COL_STATE, tempState)
        values.put(LocationRequestDbManager.COL_NUMBER, FAKE_PHONE_NUMBER)
        values.put(LocationRequestDbManager.COL_REQUESTER, "Fakey McFake Face")
        values.put(LocationRequestDbManager.COL_NOTE, "This isn't real")
        values.put(LocationRequestDbManager.COL_REQUEST_DATE, tempDate)
        values.put(LocationRequestDbManager.COL_LATITUDE, "47.6062")
        values.put(LocationRequestDbManager.COL_LONGITUDE, "-122.3321")

        //Add it to the database
        var dbManager = LocationRequestDbManager(this)
        val mID = dbManager.insert(values)

    }

    private fun getAllPermissions() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.SEND_SMS)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.SEND_SMS),
                            this.MY_PERMISSIONS_REQUEST_SEND_SMS)
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // Permission has already been granted
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.RECEIVE_SMS)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.RECEIVE_SMS),
                            this.MY_PERMISSIONS_REQUEST_RECEIVE_SMS)
                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //Request Location Permission
                checkLocationPermission()
            }


            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                //Request Contact List Permissions
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS)
            } else  {
                //Location Permission already granted
                loadContacts()
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_LONG).show()
                }
                return
            }
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContacts()
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Contact List Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadContacts() {
        var builder = StringBuilder()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS)
            //callback onRequestPermissionsResult
        } else {
            builder = getContacts()
            tvContacts.text = builder.toString()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
                        })
                        .create()
                        .show()

            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    private fun getContacts(): StringBuilder {
        val builder = StringBuilder()
        val resolver: ContentResolver = contentResolver;
        val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                null)

        if (cursor.count > 0) {
            while (cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber = (cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))).toInt()

                if (phoneNumber > 0) {
                    val cursorPhone = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", arrayOf(id), null)

                    if(cursorPhone.count > 0) {
                        while (cursorPhone.moveToNext()) {
                            val phoneNumValue = cursorPhone.getString(
                                    cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            builder.append("Contact: ").append(name).append(", Phone Number: ").append(
                                    phoneNumValue).append("\n\n")
                            Log.e("Name ===>",phoneNumValue);
                        }
                    }
                    cursorPhone.close()
                }
            }
        } else {
            //   toast("No contacts available!")
        }
        cursor.close()
        return builder
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        mFusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        tvLatitude.text = location!!.latitude.toString()
                        tvLongitude.text = location!!.longitude.toString()
                    } else {
                        Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
                    }
                }

    }


    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build()

        mGoogleApiClient!!.connect()
    }

    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    public fun sendTextMessages() {
        val msg = "Hello from Matt"
        val contactAddress = "2062261460"


        val sms : SmsManager = SmsManager.getDefault ()
        sms.sendTextMessage(contactAddress, null, msg, null, null)
        Toast.makeText(this, "Message Sent", Toast.LENGTH_LONG).show()
    }

    /**
     * This makes sure that the message is displayed as an outgoing message to the user
     */
    public fun insertSMS(context : Context, address : String, body : String ) {

        val resolver = this.getContentResolver()
        val values = ContentValues()
        values.put("address", address)
        values.put("body", body)
        resolver.insert(Uri.parse(CONTENT_URI_SENT_MESSAGES), values)
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }


}