package com.example.iotapp
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request.Method
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
class MainActivity2 : AppCompatActivity(), ItemListener {
    private lateinit var rvList: RecyclerView
    private lateinit var btnSensorAdd: Button
    private lateinit var btnSensorRefresh: Button
    private lateinit var sesion: SharedPreferences
    private lateinit var lista: Array<Array<String?>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        rvList = findViewById(R.id.rvList)
        btnSensorAdd = findViewById(R.id.btnSensorAdd)
        btnSensorRefresh = findViewById(R.id.btnSensorRefresh)
        sesion = getSharedPreferences("sesion", 0)
        rvList.setHasFixedSize(true)
        rvList.itemAnimator= DefaultItemAnimator()
        rvList.layoutManager = LinearLayoutManager(this)
        fill()
        btnSensorAdd.setOnClickListener {
            startActivity(Intent(this, MainActivity3::class.java))
        }
        btnSensorRefresh.setOnClickListener {
            fill()
        }
    }
    private fun fill(){
        val  url = Uri.parse(Config.URL + "sensors")
            .buildUpon()
            .build().toString()
        val peticion = object :JsonObjectRequest(Method.GET, url, null, {
            response -> Log.e("Lista Sensores", response.toString())
            val datos = response.getJSONArray("data")
            lista = Array(datos.length()){
                arrayOfNulls(5) }
            for(i in 0  until  datos.length()){
                val sensor = datos.getJSONObject(i)
                lista[i][0] = sensor.getString("id")
                lista[i][1] = sensor.getString("name")
                lista[i][2] = sensor.getString("type")
                lista[i][3] = sensor.getString("value")
                lista[i][4] = sensor.getString("date")
            }
            rvList.adapter = MyAdapter(lista, this)
        }, {
                error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
        }){
            override fun getHeaders():Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Authorization"] = sesion.getString("jwt", "").toString()
                return params
            }
        }
        MySingleton.getInstance(applicationContext).addToRequestQueue(peticion)
    }
    override fun onClick(v: View?, position: Int) {
        Toast.makeText(this, "Click en posicion $position", Toast.LENGTH_SHORT).show()
    }
    override fun onEdit(v: View?, position: Int) {
       val i = Intent(this, MainActivity3::class.java)
        i.putExtra("id", lista[position][0])
        i.putExtra("name", lista[position][1])
        i.putExtra("type", lista[position][2])
        i.putExtra("value", lista[position][3])
        startActivity(i)
    }
    override fun onDel(v: View?, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿En serio eliminar ${lista[position][1]}")
            .setPositiveButton("Si"){
                    dialog, which ->
                val url = Uri.parse(Config.URL + "sensors/" + lista[position][0])
                    .buildUpon()
                    .build().toString()
                val peticion =object: StringRequest(com.android.volley.Request.Method.DELETE, url, {
                        response -> fill()
                },{
                        error -> Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
                    fill()
                } ){
                    override fun getHeaders():Map<String, String>{
                        val params: MutableMap<String, String> = HashMap()
                        params["Authorization"] = sesion.getString("jwt", "").toString()
                        return params
                    }
                }
                MySingleton.getInstance(applicationContext).addToRequestQueue(peticion)
            }
            .setNegativeButton("No", null).show()
    }
}