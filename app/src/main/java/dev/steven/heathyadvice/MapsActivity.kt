package dev.steven.heathyadvice


import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import dev.steven.heathyadvice.databinding.ActivityMapsBinding
import android.os.AsyncTask
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import org.json.JSONObject
import java.net.URL

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient //Proveedor de mi ubicación más exacta
    private val bundle = Bundle()
    private var lat: Double = 0.0
    private var lon: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Verificación de los permisos para acceder a mi ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location!= null){
                val ubicacion = LatLng(location.latitude, location.longitude)
                //Toast.makeText(this,"Esta es tu latitud"+ location.latitude, Toast.LENGTH_SHORT).show()
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15f))
                lat = location.latitude
                lon = location.longitude
                weatherTask().execute()

            }


        }



    }
    // Funcion para mandar los datos al fragment1
    /*fun enviarDatosFr1(city: String, icon: String, state: String, message:String){
        bundle.putString("city", city)
        bundle.putString("icon", icon)
        bundle.putString("state", state)
        bundle.putString("message", message)
        val transaccion = supportFragmentManager.beginTransaction()
        val fragment = Weather()
        fragment.arguments = bundle
        transaccion.replace(R.id.contenedor, fragment)
        transaccion.addToBackStack(null)
        transaccion.commit()



    }
    // Función para mandar los datos al fragment 2
    fun enviarDatosFr2(temp: String, wind: Double, humidity: String, visibility: Int){
        bundle.putInt("visibility", visibility)
        bundle.putDouble("wind", wind)
        bundle.putString("humidity", humidity)
        bundle.putString("temp", temp)
        val transaccion = supportFragmentManager.beginTransaction()
        val fragment = weatherData()
        fragment.arguments = bundle
        transaccion.replace(R.id.contenedor2, fragment)
        transaccion.addToBackStack(null)
        transaccion.commit()
    }*/
    //Clase que se encarga de hacer la llamada al API y distribuir los datos
    inner class weatherTask(): AsyncTask<String, Void, String>()
    {
        override fun doInBackground(vararg p0: String?): String? {
            val api : String = "9a69da85115d24ec13c9721ca0a15f0d"
            var response: String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&lang=es&appid=$api")
                    .readText(Charsets.UTF_8)
            }catch (ex:Exception){
                response = null
                println(ex)

            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try{
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val visibility = jsonObj.getInt("visibility")
                var temp = main.getDouble("temp")
                var city = jsonObj.getString("name")
                var humedad = main.getInt("humidity")
                var clima = weather.getString("main")
                var description = weather.getString("description")
                var wind_speed = wind.getDouble("speed")
                var message = recommendOptions(visibility, clima)
                var weather_icon = weather.getString("icon")
                weather(weather_icon, message, city, description)
                weatherData(wind_speed,visibility,temp, humedad )




                //enviarDatosFr1(city,weather_icon,description,message)
                //enviarDatosFr2(temp,wind_speed,humedad,visibility)









            }catch (ex: Exception){
                println(ex)
            }

        }
        fun recommendOptions(vis: Int, weather:String): String {
            var recomendacion1: String = ""
            var recomendacion2: String = ""

            if (vis < 10000){
                recomendacion1 = "Se recomienda que maneje con las luces altas y despacio, debido a que hay poca visibilidad"
            }
            else{
                recomendacion1 = "Hay buena visibilidad, sin embargo no descuide ir atento a cualquier eventualidad"

            }
            recomendacion2 = when(weather){
                "Rain" -> "maneje despacio, el día esta lluvioso, las carreteras pueden ponerse resbalosas"
                "Thunderstorm" -> "procure no manejar, El día esta con tormenta. Puede causar un accidente."
                "Drizzle" -> "hay pequeñas lluvias. Maneje con cuidado y despacio."
                else -> "no se reportan lluvias en su ubicación."

            }
            val message : String = recomendacion1+" además "+recomendacion2
            return message

        }
        fun weather(icon:String, message: String, city:String, state:String){
            when (icon){
                "01d" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.sun)
                "01n" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.moon)
                "02d" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.clouds)
                "02n" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.clouds)
                "03d" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.clouds)
                "03n" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.clouds)
                "04d" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.muy_nublado)
                "04n" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.muy_nublado)
                "09d" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.nubes)
                "09n" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.nubes)
                "10d" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.water)
                "10n" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.water)
                "11d" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.thunderstorm)
                "11n" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.thunderstorm)
                "50d" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.vis)
                "50n" -> findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.vis)
                else ->  findViewById<ImageView>(R.id.imgClima).setImageResource(R.drawable.nieve)
            }
            findViewById<TextView>(R.id.txtMessage).text = message
            findViewById<TextView>(R.id.txtCity).text = city
            findViewById<TextView>(R.id.txtClima).text = state

            findViewById<ImageButton>(R.id.imgButtonSearch).setOnClickListener {
                startActivity(Intent(this@MapsActivity, SearchActivity::class.java))
            }
        }
        fun weatherData(wind:Double, visibility: Int, temp: Double, humidity: Int ){
            var wind = wind
            if (wind >= 1000){
                wind = wind/1000
                findViewById<TextView>(R.id.txtViento).text = "$wind km/h"
            }
            else{
                findViewById<TextView>(R.id.txtViento).text = "$wind m/s"
            }
            if (visibility >= 1000){
                findViewById<TextView>(R.id.txtVisibilidad).text ="Buena"
                findViewById<ImageView>(R.id.imgVisibility).setImageResource(R.drawable.visibilidad_verde)
            }
            else if (visibility < 1000 && visibility > 700){
                findViewById<TextView>(R.id.txtVisibilidad).text ="Media"
                findViewById<ImageView>(R.id.imgVisibility).setImageResource(R.drawable.visibilidad_amarillo)
            }
            else{
                findViewById<TextView>(R.id.txtVisibilidad).text ="Mala"
                findViewById<ImageView>(R.id.imgVisibility).setImageResource(R.drawable.visibilidad_rojo)
            }


            findViewById<TextView>(R.id.txtTemp).text= "$temp°C"
            findViewById<TextView>(R.id.txtHumedad).text = "$humidity%"
        }

    }


}