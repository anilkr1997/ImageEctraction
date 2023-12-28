
package com.mrappbuilder.imageectraction

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.GpsDirectory
import com.drew.metadata.jpeg.JpegDescriptor
import com.drew.metadata.jpeg.JpegDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var button: AppCompatButton
    lateinit var imageView: AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button=findViewById(R.id.btn_select)
        imageView=findViewById(R.id.img_select)

        button.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermessionLauncher.launch(READ_MEDIA_IMAGES)
            }else{
                requestPermessionLauncher.launch(READ_EXTERNAL_STORAGE)
            }



        }
    }
val requestPermessionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){
    if(it){
launchNewPhotoPicker()
    }else{
Toast.makeText(this,"Give permission",Toast.LENGTH_LONG).show()
    }
}

    private fun launchNewPhotoPicker(){
        newPhotoPiker.launch("image/*")
    }
    val newPhotoPiker=registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
        imageView.setImageURI(uri)
        lifecycleScope.launch (Dispatchers.Main){
            val result=async (Dispatchers.IO){
                val intputStream=uri?.let { contentResolver.openInputStream(it) }
                val metadata=ImageMetadataReader.readMetadata(intputStream)
                for (directory in metadata.directories){
                    for (tag in directory.tags){
                        Log.i("TAG", "out here : $tag")
                    }
                }


                val jpegDirectory=metadata.getFirstDirectoryOfType(JpegDirectory::class.java)

                val imageHeight= jpegDirectory?.getString(JpegDirectory.TAG_IMAGE_HEIGHT)

                val exifDirectory=metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
                val model=exifDirectory?.getString(ExifIFD0Directory.TAG_MODEL)
                val gpsDirectory=metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
                val latitude=gpsDirectory?.geoLocation?.latitude
                val longitude=gpsDirectory?.geoLocation?.longitude
                MyResultData(imageHeight,model,latitude,longitude)

            }
            val (imageHeight,model,latitude,longitude)=result.await()

            Log.i("TAG", "imageHeight:  $imageHeight")
            Log.i("TAG", "model:  $model")
            Log.i("TAG", "latitude:  $latitude")
            Log.i("TAG", "longitude:  $longitude")

        }

    }
}