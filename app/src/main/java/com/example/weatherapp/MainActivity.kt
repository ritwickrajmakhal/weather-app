package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.round


class MainActivity : AppCompatActivity() {
    private var cityNameField : EditText? = null
    private val appId : String = "9ea95d7f67afd651695170a2a32e68cb"
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cityNameField = findViewById(R.id.cityNameField)
        // Check if location permission is granted, if not, request permission
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if(hasLocationPermission) {
            // Location permission has been granted
            // You can proceed with using location services
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val latitude: Double? = location?.latitude
            val longitude: Double? = location?.longitude
            if (latitude != null && longitude != null) {
                getData(latitude,longitude,"")
            }
            else{
                Toast.makeText(this,"Unable to locate try to search your location",Toast.LENGTH_SHORT).show()
            }

        } else {
            // Location permission has not been granted
            // You can request the permission from the user
            Toast.makeText(this,"Go to app info and give the permission for location",Toast.LENGTH_SHORT).show()
        }


    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getData(latitude:Double = 0.0, longitude:Double = 0.0, cityName:String = ""){
        val backgroundImg :ImageView = findViewById(R.id.backgroundImg)
        val temperatureField : TextView = findViewById(R.id.temperatureField)
        val description : TextView = findViewById(R.id.description)
        val dateMinMaxTemp : TextView = findViewById(R.id.date_min_max_temp)
        val degreeIcon : ImageView = findViewById(R.id.degreeIcon)

        var url : String? = null
        val city = cityNameField?.text
        url = if(cityName.length==0){
            "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$appId"
        } else{
            "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$appId"
        }
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                // display the response on screen
                cityNameField?.setText(response.getString("name"))

                val weather: JSONObject = response.getJSONArray("weather").getJSONObject(0)
                val icon = weather.getString("icon")
                Glide.with(this).load("https://openweathermap.org/img/wn/$icon@4x.png").into(backgroundImg)
                val desc = weather.getString("description")
                description.text = desc
                val main : JSONObject = response.getJSONObject("main")
                temperatureField.text = (main.getDouble("temp").toInt() - 273).toString()
                degreeIcon.visibility = View.VISIBLE
                val tempMin = round(main.getDouble("temp_min") - 273)
                val tempMax = round(main.getDouble("temp_max") - 273)
                val formatter = DateTimeFormatter.ofPattern("d MMM E", Locale.ENGLISH)
                val currentDate = LocalDate.now()
                val formattedDate = currentDate.format(formatter)
                val degreeSymbol = "\u00B0"
                dateMinMaxTemp.text = "$formattedDate $tempMax$degreeSymbol C / $tempMin$degreeSymbol C"
            },
            { _ ->
                // handle error
                Toast.makeText(this,"$city not found",Toast.LENGTH_SHORT).show()
            }
        )
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun search(view: View) {
        getData(0.0,0.0,cityNameField?.text.toString())
    }

}