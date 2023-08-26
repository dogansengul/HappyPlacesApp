package com.example.happyplacesapp

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.happyplacesapp.data.Dao
import com.example.happyplacesapp.data.HappyPlace
import com.example.happyplacesapp.data.HappyPlaceApp
import com.example.happyplacesapp.databinding.ActivityAddPlaceBinding
import com.example.happyplacesapp.databinding.LayoutCameraOrGalleryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddPlaceActivity : AppCompatActivity() {
    private var binding: ActivityAddPlaceBinding? = null
    private lateinit var cal: Calendar
    private var happyPlaceCount = 0
    private var permissionRequestCount = 0
    private var permissionResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            var cameraPermission = false
            var mediaPermission = false
            permissions.entries.forEach {
                val permission = it.key
                val isGranted = it.value
                if(isGranted){
                    if(permission == android.Manifest.permission.CAMERA){
                        //Toast.makeText(this, "Camera permission is granted.",Toast.LENGTH_SHORT).show()
                        cameraPermission = true
                    } else {
                        //Toast.makeText(this, "Media permission is granted.",Toast.LENGTH_SHORT).show()
                        mediaPermission = true
                    }
                } else {
                    if(permission == android.Manifest.permission.CAMERA){
                        //Toast.makeText(this, "Camera permission is not granted.",Toast.LENGTH_SHORT).show()
                        cameraPermission = false
                    } else {
                        //Toast.makeText(this, "Media permission is not granted.",Toast.LENGTH_SHORT).show()
                        mediaPermission = false
                    }
                }

            }
            if(cameraPermission && mediaPermission) {
                showCameraOrGalleryDialog()
            }
        }
    private val openGalleryResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                binding?.ivPlaceImage?.setImageURI(selectedImageUri)

            }
        }
    private val openCameraResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val data: Intent? = result.data
                val imageBitmap: Bitmap = data?.extras?.get("data") as Bitmap
                CoroutineScope(Dispatchers.IO).launch { saveBitmapFile(imageBitmap) }
                binding?.ivPlaceImage?.setImageBitmap(imageBitmap)

            }
        }
    private  var databaseDao: Dao? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.tbActionBar2)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        databaseDao = (application as HappyPlaceApp).db?.dao()


        binding?.tbActionBar2?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        cal = Calendar.getInstance()
        binding?.etDate?.setOnClickListener {
            showDatePickerDialog()
        }
        binding?.tvAddImage?.setOnClickListener {
            permissionRequestCount++
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) ||
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_MEDIA_IMAGES)) {
                showRationaleDialog()
            } else if (permissionRequestCount > 2 && ((checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED))) {
                showSettingsDialog()
            } else if ((checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)) {
                showCameraOrGalleryDialog()
            } else {
                permissionResultLauncher.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES))
            }
        }

        binding?.btnSave?.setOnClickListener {
            val title = binding!!.etTitle.text.toString()
            val description = binding!!.etDescription.text.toString()
            val date = binding!!.etDate.text.toString()
            val location = binding!!.etLocation.text.toString()
            val drawable = binding!!.ivPlaceImage.drawable
            val image: Bitmap? = if (drawable is VectorDrawable) {
                val vectorDrawable = drawable as VectorDrawable
                val bitmap = Bitmap.createBitmap(
                    vectorDrawable.intrinsicWidth,
                    vectorDrawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
                vectorDrawable.draw(canvas)
                bitmap
            } else if (drawable is BitmapDrawable) {
                (drawable as BitmapDrawable).bitmap
            } else {
                null
            }

            if((title.isNotEmpty()) && (description.isNotEmpty()) && (date.isNotEmpty()) && (location.isNotEmpty()) && (image!= null)) {
                lifecycleScope.launch {
                    happyPlaceCount = databaseDao?.getPlaceCount() ?: 0
                    databaseDao?.addPlace(HappyPlace(title = title,
                        description = description,
                        date = date,
                        location = location,
                        imageBitmap = image))
                    onBackPressedDispatcher.onBackPressed()
                }
            } else {
                Toast.makeText(this, "Her alanı doldurun", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun showCameraOrGalleryDialog() {
        val dialog = Dialog(this)
        val dialogBinding = LayoutCameraOrGalleryBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.setTitle("Pick Image with")
        val withCameraButton = dialogBinding.tvOpenCamera
        val withGalleryButton = dialogBinding.tvOpenGallery
        withCameraButton.setOnClickListener {
            openCamera()
            dialog.dismiss()
        }
        withGalleryButton.setOnClickListener {
            openGallery()
            dialog.dismiss()
        }
        dialog.show()

    }

    private suspend fun saveBitmapFile(bitmap: Bitmap?): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if(bitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
                    val file = File(
                        externalCacheDir?.absoluteFile.toString()
                                + File.separator + "HappyPlacesApp_"
                                + System.currentTimeMillis() / 1000 + ".png"
                    )

                    val fo = FileOutputStream(file)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    result = file.absolutePath
                    runOnUiThread {
                        if (result.isNotEmpty()) {
                            Toast.makeText(
                                applicationContext,
                                "File saved succesfully. $result",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Something went wrong.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        openCameraResultLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        openGalleryResultLauncher.launch(intent)
    }

    private fun setViewToET() {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val formattedDate = sdf.format(cal.time)
        binding?.etDate?.setText(formattedDate)
    }
    private fun showDatePickerDialog() {
        val onDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            setViewToET()
        }
        val datePickerDialog = DatePickerDialog(this, onDateSetListener
            , cal.get(Calendar.YEAR)
            , cal.get(Calendar.MONTH)
            , cal.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showRationaleDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Permission Request")
            .setMessage("You have denied camera or media permissions before. Please grant the permissions.")
            .setPositiveButton("Okay", DialogInterface.OnClickListener {
                    dialog, which -> permissionResultLauncher.launch(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES)) })
        alertDialog.show()
    }

    private fun showSettingsDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Permission Request")
            .setMessage("You have denied camera or media permissions multiple times. Please go to app settings and grant the permissions.")
            .setPositiveButton("Okay", DialogInterface.OnClickListener {
                    dialog, which -> openAppSettings() })
        //alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}