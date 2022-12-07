package dev.steven.heathyadvice

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.net.URL

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val imgBtn = findViewById<ImageButton>(R.id.imgButtonMain).setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }
        findViewById<Button>(R.id.button).setOnClickListener {
            searchTask().execute()
        }



    }
    inner class searchTask(): AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg p0: String?): String? {
            val api : String = "9a69da85115d24ec13c9721ca0a15f0d"
            val name: String = findViewById<EditText>(R.id.etSearch).text.toString()
            val idSantaAna = 3621630
            val idSanJose = 3621841
            var code: Int
            when(name){
                "Santa Ana" -> code = idSantaAna
                "San José" -> code = idSanJose
                else -> code = 3624060
            }


            var response: String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?id=$code&units=metric&lang=es&appid=$api")
                    .readText(Charsets.UTF_8)
            }catch (ex:Exception){
                response = null
                println(ex)

            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
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
                weather(weather_icon, message, city,description)
                weatherDataSearch(wind_speed,visibility,temp,humedad)
                if (visibility<10000 || clima == "Rain" || clima == "Thunderstorm" || clima == "Drizzle"){
                    findViewById<TextView>(R.id.txtAlerta).text = "Se reportan incidentes en la zona"
                }else{
                    findViewById<TextView>(R.id.txtAlerta).text = "No se reportan incidentes en la zona"
                }


            }catch (ex:Exception){
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
        fun weather(icon:String, message: String, city:String, state: String){
            when (icon){
                "01d" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_sun_search)
                "01n" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_moon)
                "02d" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_clouds)
                "02n" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_clouds)
                "03d" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_clouds)
                "03n" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_clouds)
                "04d" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_cloud)
                "04n" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_cloud)
                "09d" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_cloud)
                "09n" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_cloud)
                "10d" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_water)
                "10n" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_water)
                "11d" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_thunderstorm)
                "11n" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_thunderstorm)
                "50d" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_vis)
                "50n" -> findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.big_vis)
                else ->  findViewById<ImageView>(R.id.imgWeather).setImageResource(R.drawable.nieve)
            }
            findViewById<TextView>(R.id.txtMessageSearch).text = message
            findViewById<TextView>(R.id.txtCityS).text = city
            findViewById<TextView>(R.id.txtState).text = state
            //findViewById<TextView>(R.id.txtClima).text = state


        }
        fun weatherDataSearch(wind:Double, visibility: Int, temp: Double, humidity: Int ){
            var wind = wind
            if (wind >= 1000){
                wind = wind/1000
                findViewById<TextView>(R.id.txtVientoS).text = "$wind km/h"
            }
            else{
                findViewById<TextView>(R.id.txtVientoS).text = "$wind m/s"
            }
            if (visibility >= 1000){
                findViewById<TextView>(R.id.txtVisibilidadS).text ="Buena"
                //findViewById<ImageView>(R.id.imgVisibilityS).setImageResource(R.drawable.visibilidad_verde)
            }
            else if (visibility < 1000 && visibility > 700){
                findViewById<TextView>(R.id.txtVisibilidadS).text ="Media"
                //findViewById<ImageView>(R.id.imgVisibility).setImageResource(R.drawable.visibilidad_amarillo)
            }
            else{
                findViewById<TextView>(R.id.txtVisibilidadS).text ="Mala"
                //findViewById<ImageView>(R.id.imgVisibility).setImageResource(R.drawable.visibilidad_rojo)
            }


            findViewById<TextView>(R.id.txtTempS).text= "$temp°C"
            findViewById<TextView>(R.id.txtHumedadS).text = "$humidity%"
        }

    }

}