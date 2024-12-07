package com.dam2jms.gestiongastosapp.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL

class CurrencyConverter {

    //url para las solicitudes a la api
    private val baseURL = "https://api.frankfurter.app"

    /**metodo que obtiene las tasas de cambio para una moneda**/
    suspend fun obtenerTasasMonedas(moneda: String): Map<String, Double>{

        return withContext(Dispatchers.IO){
            try {
                val url = URL("$baseURL/latest?from=$moneda")

                //leo desde la url
                val respuesta = url.readText()

                //convierto la respuesta de la lectura en un json
                val jsonObject = JSONObject(respuesta)

                //extraigo del json las tasas de cambio
                val tasas = jsonObject.getJSONObject("rates")

                //devuelvo las tasas en una lista clave-valor
                tasas.keys().asSequence().associateWith { tasas.getDouble(it) }
            } catch (e: FileNotFoundException) {
                Log.e("CurrencyConverter", "URL no encontrada: ${e.message}")
                emptyMap<String, Double>()
            } catch (e: Exception) {
                Log.e("CurrencyConverter", "Error: ${e.message}")
                emptyMap<String, Double>()
            }
        }
    }

    /**metodo que hace el cambio de una moneda a otra*/
    suspend fun convertirMoneda(cantidad: Double, monedaOrigen: String, monedaDestino: String): Double {

        return withContext(Dispatchers.IO){
            try {
                val url = URL("$baseURL/laters?amount=$cantidad&from=$monedaOrigen&to=$monedaDestino")
                val respuesta = url.readText()
                val jsonObject = JSONObject(respuesta)
                jsonObject.getJSONObject("rates").getDouble(monedaDestino)
            }catch (e: Exception){
                Log.e("CurrencyConverter", "Error en la conversion: ${e.message}")
                0.0
            }
        }
    }

}


