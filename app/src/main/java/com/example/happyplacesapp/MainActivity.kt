package com.example.happyplacesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplacesapp.data.Dao
import com.example.happyplacesapp.data.HappyPlace
import com.example.happyplacesapp.data.HappyPlaceApp
import com.example.happyplacesapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var databaseDao: Dao? = null
    private lateinit var placeData: ArrayList<HappyPlace>
    private lateinit var happyPlaceAdapter: HappyPlaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbActionBar1)

        //creating database dao object
        databaseDao = (application as HappyPlaceApp).db?.dao()

        //setting up place data and recyclerview to show happy places
        CoroutineScope(Dispatchers.IO).launch {
            getAllHappyPlacesAndDisplayRV()
        }


        //adding happy place button functionality
        binding.fabAddHappyPlace.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setUpRV(data: ArrayList<HappyPlace>) {
        val happyPlaceAdapter = HappyPlaceAdapter(data)
        binding.recyclerView.apply {
            adapter = happyPlaceAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        }
        happyPlaceAdapter.setOnItemClickListener(object : HappyPlaceAdapter.OnItemClickListener{
            override fun OnItemClick(position: Int) {
                Toast.makeText(this@MainActivity, "yes", Toast.LENGTH_SHORT).show()
            }
        })

        //creating itemtouchhelper object to delete and move happyplaces on recyclerview
        val itemTouchHelper: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                when(direction) {
                    ItemTouchHelper.LEFT -> {
                        val removed = placeData[position]
                        CoroutineScope(Dispatchers.IO).launch {
                            databaseDao?.deletePlace(removed)
                            placeData.remove(removed)
                            withContext(Dispatchers.Main) {

                            }

                        }
                        //undo functionality with snackbar
                        Snackbar.make(binding.recyclerView, "Happy Place Removed.", Snackbar.LENGTH_LONG).setAction("Undo", View.OnClickListener {
                            CoroutineScope(Dispatchers.IO).launch {
                                databaseDao?.addPlace(removed)
                                placeData.add(position, removed)
                                withContext(Dispatchers.Main) {
                                    happyPlaceAdapter.notifyItemInserted(position)
                                }
                            }
                        }).show()
                    }
                }
            }
        }
        //itemtouchhelper object, which was created above, is using here and attached to recyclerview
        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(binding.recyclerView)
    }

    private suspend fun getAllHappyPlacesAndDisplayRV() {
        placeData = ArrayList<HappyPlace>()
        lifecycleScope.launch {
            databaseDao?.getAllPlaces()?.collect{
                placeData = ArrayList(it)
                withContext(Dispatchers.Main) {
                    setUpRV(placeData)
                }
            }
        }
    }
}