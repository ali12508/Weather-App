package com.ui.weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ui.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
      //  binding.currentlocation.visibility=View.GONE
        supportActionBar?.hide()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocationn()
        binding.getcityname.setOnEditorActionListener({ v,actionId,KeyEvent ->
            if(actionId==EditorInfo.IME_ACTION_SEARCH) {
                val et=binding.getcityname.text.toString()
                if (TextUtils.isEmpty(et)){
                    //et.error
                    Toast.makeText(this,"City name required..!!!",Toast.LENGTH_LONG).show()
                }else if(et.length<4 ){
                    Toast.makeText(this,"Too short name..!!!",Toast.LENGTH_LONG).show()

                }else if(et.length>12 ){
                Toast.makeText(this,"Too long name..!!!",Toast.LENGTH_LONG).show()

            }
                else{
                    getCityWeather(et)
                }
              //  getCityWeather(binding.getcityname.text.toString())
                val view =this.currentFocus
                if (view!=null){
                    val inm:InputMethodManager=getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inm.hideSoftInputFromWindow(view.windowToken,0)
                    binding.getcityname.clearFocus()
                }
                true
            }else false
            })



    }

    private fun getCityWeather(cityName: String) {
        binding.pbloading.visibility = View.VISIBLE
        ApiUtilities.getAPiInterface()?.getCityWeatherdata(cityName, API_KEY)?.enqueue(object :Callback<ModalClass> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ModalClass>, response: Response<ModalClass>) {
                setDataOnViews(response.body())
            }

            override fun onFailure(call: Call<ModalClass>, t: Throwable) {
                Toast.makeText(applicationContext,t.localizedMessage.toString(),Toast.LENGTH_LONG).show()
            }

        })

    }

    private fun getCurrentLocationn() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                //final latitudevand longitude
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task->
                    val location:Location?=task.result
                    if (location==null){
                        Toast.makeText(applicationContext,"null location",Toast.LENGTH_LONG).show()

                    }else{
//                        Toast.makeText(applicationContext,"Get success",Toast.LENGTH_LONG).show()
//                        val textlat=findViewById<TextView>(R.id.tvDaymaxTemp)
//                        val textlong=findViewById<TextView>(R.id.tvDayminTemp)
//                        textlat.text="" + location.longitude
//                        textlong.text="" + location.latitude

                        //fetch weather
                        val long =location.longitude.toString()
                        val lati =location.latitude.toString()
                        fetchCurrentLocation(long,lati)
                    }

                }

            } else {

                Toast.makeText(applicationContext,"Granted",Toast.LENGTH_LONG).show()
                val intent =Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)

            }

        } else {
            //request permission
            requestPermissions()

        }
    }

    private fun fetchCurrentLocation(long: String, lati: String) {
        binding.pbloading.visibility=View.VISIBLE
        ApiUtilities.getAPiInterface()
            ?.getCurrentWeatherdata(lati,long,API_KEY)?.enqueue(object : Callback<ModalClass> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<ModalClass>, response: Response<ModalClass>) {
                    if (response.isSuccessful){
                        setDataOnViews(response.body())
                    }
                }

                override fun onFailure(call: Call<ModalClass>, t: Throwable) {
                    Toast.makeText(applicationContext,t.localizedMessage.toString(),Toast.LENGTH_LONG).show()
                }

            })

       // 3788cd45f9ea94064385865f5cb763d7
       // https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataOnViews(body: ModalClass?) {
        val sdf=SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentdate=sdf.format(Date())
        binding.tvdatetime.text=currentdate
        binding.tvDaymaxTemp.text="Day  " + kelvinToCelcius(body!!.main.temp_max)+"°"
        //binding.ivweathericon.setImageResource(body!!.weather[0].icon)
        binding.tvDayminTemp.text="Night  " +  kelvinToCelcius(body!!.main.temp_min)+"°"
        binding.tvmainTemp.text=""+kelvinToCelcius(body!!.main.temp)+"°"
        binding.tvfeelslike.text="Feels Like " +kelvinToCelcius(body!!.main.feels_like)+"°"
        binding.tvweatherType.text=body.weather[0].main
        binding.tvSunrise.text=timeStampLocaldate(body.sys.sunrise.toLong())
        binding.tvSunset.text=timeStampLocaldate(body.sys.sunset.toLong())
        binding.tvPressure.text=body.main.pressure.toString()
        binding.tvHumidity.text=body.main.humidity.toString()+"%"
        binding.tvWind.text=body.wind.speed.toString()+"m/s"
        binding.tvTemperature.text=body.weather[0].description.toString()
      //  binding.tvTemperature.text=""+((kelvinToCelcius(body.main.temp)).times(1.8).plus(32).roundToInt())
        binding.getcityname.setText(body.name)


        updateUI(body.weather[0].id)

    }

    private fun updateUI(id: Int) {
        if (id in 200..232){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.thunderstorm)
            binding.rltoolbar.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.thunderstormbg)

            binding.rlsub.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.thunderstormbg)
            binding.llmainbgabove.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.thunderstormbg)
            binding.llmainbgbelow.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.thunderstormbg)
            binding.ivweathericon.setImageResource(R.drawable.thunderstormm)

        }else if(id in 300..321){

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.dizzle)
            binding.rltoolbar.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.dizzle_bg)

            binding.rlsub.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.dizzle_bg)
            binding.llmainbgabove.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.dizzle_bg)
            binding.llmainbgbelow.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.dizzle_bg)
            binding.ivweathericon.setImageResource(R.drawable.clouds)


        }

        else if(id in 500..531){

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.rain)
            binding.rltoolbar.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.rainy_bg)

            binding.rlsub.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.rainy_bg)
            binding.llmainbgabove.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.rainy_bg)
            binding.llmainbgbelow.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.rainy_bg)
            binding.ivweathericon.setImageResource(R.drawable.rain)


        }else if(id in 600..620){

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.snow)
            binding.rltoolbar.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.snowbg)

            binding.rlsub.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.snowbg)
            binding.llmainbgabove.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.snowbg)
            binding.llmainbgbelow.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.snowbg)
            binding.ivweathericon.setImageResource(R.drawable.snow)


        }else if(id in 700..781){

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.atmosphere)
            binding.rltoolbar.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.atmosphere_bg)

            binding.rlsub.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.atmosphere_bg)
            binding.llmainbgabove.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.atmosphere_bg)
            binding.llmainbgbelow.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.atmosphere_bg)
            binding.ivweathericon.setImageResource(R.drawable.clear)


        }
        else if(id == 80){

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.clear)
            binding.rltoolbar.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.clearbg)

            binding.rlsub.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.clearbg)
            binding.llmainbgabove.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.clearbg)
            binding.llmainbgbelow.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.clearbg)
            binding.ivweathericon.setImageResource(R.drawable.clear)


        }else {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.clouds)
            binding.rltoolbar.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.cloudbg)

            binding.rlsub.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.cloudbg)
            binding.llmainbgabove.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.dizzle_bg)
            binding.llmainbgbelow.background=ContextCompat.getDrawable(this@MainActivity,R.drawable.dizzle_bg)
            binding.ivweathericon.setImageResource(R.drawable.clear)


        }

          //binding.pbloading.visibility=View.GONE
       // binding.rlmain.visibility=View.VISIBLE

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private  fun timeStampLocaldate(timeStamp:Long):String{
        val localtime = timeStamp.let {
            Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).toLocalTime()
        }
        return localtime.toString()
    }

    private fun kelvinToCelcius(temp:Double):Double{
        var intTemp=temp
        intTemp=intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1,RoundingMode.UP).toDouble()

    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION)
    }

    companion object {
            private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
            const val API_KEY="3788cd45f9ea94064385865f5cb763d7"

        }

    private fun checkPermission():Boolean{
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
        return false
        }
 private   fun isLocationEnabled():Boolean{
     val locatiomanager :LocationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
     return locatiomanager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locatiomanager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

 }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== PERMISSION_REQUEST_ACCESS_LOCATION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Toast.makeText(applicationContext,"Granted",Toast.LENGTH_LONG).show()
                getCurrentLocationn()
            }else{
                Toast.makeText(applicationContext,"enable location...",Toast.LENGTH_LONG).show()
            }

        }
    }
}