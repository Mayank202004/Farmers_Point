package com.example.farmerspoint

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.media.ThumbnailUtils
import android.net.Uri
import android.widget.TextView
import com.example.farmerspoint.ml.Cotton
import com.example.farmerspoint.ml.Cucumber
import com.example.farmerspoint.ml.Grapes
import com.example.farmerspoint.ml.Guava
import com.example.farmerspoint.ml.Potato
import com.example.farmerspoint.ml.Tomato
import com.example.farmerspoint.ml.Wheat
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import androidx.appcompat.widget.SwitchCompat
import com.example.farmerspoint.ml.Sugarcane
import com.google.android.material.textfield.TextInputLayout


class MainActivity : AppCompatActivity() {
    companion object {
        private const val MY_CAMERA_PERMISSION_CODE = 123
        private const val MY_GALLERY_PERMISSION_CODE = 124
    }

    private lateinit var bitmap :Bitmap
    private lateinit var imageView:ImageView
    private lateinit var imageViewBackground:ImageView
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Find views
        val buttonSelect = findViewById<Button>(R.id.button)
        val buttonPredict = findViewById<Button>(R.id.button2)
        val textView = findViewById<TextView>(R.id.ResultText)
        val yourSwitch: SwitchCompat = findViewById(R.id.switch1)
        val autoComplete : AutoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        val textInputLayout = findViewById<TextInputLayout>(R.id.textInputLayout)
        imageView = findViewById(R.id.imageView3)
        imageViewBackground = findViewById(R.id.imageView2)

        // Set up ArrayAdapter for Spinner
        val plantTypes = resources.getStringArray(R.array.plant_types)
        val adapter = ArrayAdapter(this,R.layout.dropdown_menu, plantTypes)
        autoComplete.setAdapter(adapter)
        autoComplete.onItemClickListener = AdapterView.OnItemClickListener{adapterView,view,i,l ->
            val itemSelected = adapterView.getItemAtPosition(i)
            Toast.makeText(this,"$itemSelected selected",Toast.LENGTH_SHORT).show()
        }
        yourSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            val switch = buttonView as SwitchCompat // Cast the buttonView to SwitchCompat
            imageView.setImageDrawable(null)
            textView.setText("")
            if (isChecked) {
                autoComplete.setText(getString(R.string.select_plant_type_marathi))
                textInputLayout.hint = getString(R.string.select_plant_type_marathi)
                val adapter: ArrayAdapter<String> = ArrayAdapter(this,R.layout.dropdown_menu, resources.getStringArray(R.array.plant_types_marathi))
                autoComplete.setAdapter(adapter)
                buttonSelect.text=getString(R.string.selectm)
                buttonPredict.text=getString(R.string.predictm)


                // Perform actions when the switch is ON
            } else {
                autoComplete.setText(getString(R.string.select_plant_type))
                textInputLayout.hint = getString(R.string.select_plant_type)
                val adapter: ArrayAdapter<String> = ArrayAdapter(this,R.layout.dropdown_menu, plantTypes)
                autoComplete.setAdapter(adapter)
                buttonSelect.text=getString(R.string.select)
                buttonPredict.text=getString(R.string.predict)


                // Perform actions when the switch is OFF
            }
        }
        imageView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            // Check if imageViewOverlay has a drawable
            if (imageView.drawable != null) {
                // Display the blurred image in imageView2
                imageViewBackground.setImageResource(R.drawable.background2blur)
            } else {
                // Display the regular image in imageView2
                imageViewBackground.setImageResource(R.drawable.background2)
            }
        }
        autoComplete.setOnItemClickListener { _, _, _, _ ->
            // Clear focus when an item is selected
            autoComplete.clearFocus()
        }

        buttonSelect.setOnClickListener {
            imageView.setImageDrawable(null)
            textView.setText("")
            showImageSelectionDialog()
        }
        buttonPredict.setOnClickListener {
            // Get the selected plant type
            val selectedPlantType = autoComplete.text.toString()
            if (selectedPlantType.isEmpty()) {
                if(!yourSwitch.isChecked){
                    Toast.makeText(this, "Please select the crop type", Toast.LENGTH_SHORT).show()}
                else{
                    Toast.makeText(this, "कृपया पीक प्रकार निवडा", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            if (!::bitmap.isInitialized) {
                if(!yourSwitch.isChecked){
                    Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()}
                else{
                    Toast.makeText(this, "कृपया एक फोटो निवडा", Toast.LENGTH_SHORT).show()
                }
                return@setOnClickListener
            }

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true)
            // Convert the resized bitmap to a TensorImage
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(resizedBitmap)
            // Get the ByteBuffer from the TensorImage
            val byteBuffer = tensorImage.buffer

            if(selectedPlantType=="cotton" || selectedPlantType=="कापूस"){
                val model = Cotton.newInstance(this)
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                var max = getMax(outputFeature0.floatArray)
                val confidence = outputFeature0.floatArray[max] * 100
                if(!yourSwitch.isChecked){
                    val cottonlbl = application.assets.open("cotton.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "Result:\n${cottonlbl[max]}\nConfidence: ${"%.2f".format(confidence)}%"
                    textView.text = resultText}
                else{
                    val cottonlbl = application.assets.open("marathi/cotton_m.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "निकाल:\n${cottonlbl[max]}\nखात्री: ${"%.2f".format(confidence)}%"
                    textView.text = resultText
                }
                model.close()
            }
            else if(selectedPlantType=="cucumber" || selectedPlantType=="काकडी"){
                val model = Cucumber.newInstance(this)
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                var max = getMax(outputFeature0.floatArray)
                val confidence = outputFeature0.floatArray[max] * 100
                if(!yourSwitch.isChecked){
                    val cucumberlbl = application.assets.open("cucumber.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "Result:\n${cucumberlbl[max]}\nConfidence: ${"%.2f".format(confidence)}%"
                    textView.text = resultText}
                else{
                    val cucumberlbl = application.assets.open("marathi/cucumber_m.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "निकाल:\n${cucumberlbl[max]}\nखात्री: ${"%.2f".format(confidence)}%"
                    textView.text = resultText
                }
                model.close()
            }
            else if(selectedPlantType=="grapes" || selectedPlantType=="द्राक्षे"){
                val model = Grapes.newInstance(this)
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                var max = getMax(outputFeature0.floatArray)
                val confidence = outputFeature0.floatArray[max] * 100
                if(!yourSwitch.isChecked){
                    val grapeslbl = application.assets.open("grapes.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "Result:\n${grapeslbl[max]}\nConfidence: ${"%.2f".format(confidence)}%"
                    textView.text = resultText}
                else{
                    val grapeslbl = application.assets.open("marathi/grapes_m.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "निकाल:\n${grapeslbl[max]}\nखात्री: ${"%.2f".format(confidence)}%"
                    textView.text = resultText
                }
                model.close()
            }
            else if(selectedPlantType=="guava" || selectedPlantType=="पेरू"){
                val model = Guava.newInstance(this)
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                var max = getMax(outputFeature0.floatArray)
                val confidence = outputFeature0.floatArray[max] * 100
                if(!yourSwitch.isChecked){
                    val guavalbl = application.assets.open("guava.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "Result:\n${guavalbl[max]}\nConfidence: ${"%.2f".format(confidence)}%"
                    textView.text = resultText}
                else{
                    val guavalbl = application.assets.open("marathi/guava_m.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "निकाल:\n${guavalbl[max]}\nखात्री: ${"%.2f".format(confidence)}%"
                    textView.text = resultText
                }
                model.close()
            }
            else if(selectedPlantType=="potato" || selectedPlantType=="बटाटा"){
                val model = Potato.newInstance(this)
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                var max = getMax(outputFeature0.floatArray)
                val confidence = outputFeature0.floatArray[max] * 100
                if(!yourSwitch.isChecked){
                    val potatolbl = application.assets.open("potato.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "Result:\n${potatolbl[max]}\nConfidence: ${"%.2f".format(confidence)}%"
                    textView.text = resultText}
                else{
                    val potatolbl = application.assets.open("marathi/potato_m.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "निकाल:\n${potatolbl[max]}\nखात्री: ${"%.2f".format(confidence)}%"
                    textView.text = resultText
                }
                model.close()
            }
            else if(selectedPlantType=="tomato" || selectedPlantType=="टोमॅटो"){
                val model = Tomato.newInstance(this)
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                var max = getMax(outputFeature0.floatArray)
                val confidence = outputFeature0.floatArray[max] * 100
                if(!yourSwitch.isChecked){
                    val tomatolbl = application.assets.open("tomato.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "Result:\n${tomatolbl[max]}\nConfidence: ${"%.2f".format(confidence)}%"
                    textView.text = resultText}
                else{
                    val tomatolbl = application.assets.open("marathi/tomato_m.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "निकाल:\n${tomatolbl[max]}\nखात्री: ${"%.2f".format(confidence)}%"
                    textView.text = resultText
                }
                model.close()
            }
            else if(selectedPlantType=="wheat" || selectedPlantType=="गहू"){
                val model = Wheat.newInstance(this)
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                var max = getMax(outputFeature0.floatArray)
                val confidence = outputFeature0.floatArray[max] * 100
                if(!yourSwitch.isChecked){
                    val wheatlbl = application.assets.open("wheat.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "Result:\n${wheatlbl[max]}\nConfidence: ${"%.2f".format(confidence)}%"
                    textView.text = resultText}
                else{
                    val wheatlbl = application.assets.open("marathi/wheat_m.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "निकाल:\n${wheatlbl[max]}\nखात्री: ${"%.2f".format(confidence)}%"
                    textView.text = resultText
                }
                model.close()
            }
            else if(selectedPlantType=="sugarcane" || selectedPlantType=="ऊस"){
                val model = Sugarcane.newInstance(this)
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                var max = getMax(outputFeature0.floatArray)
                val confidence = outputFeature0.floatArray[max] * 100
                if(!yourSwitch.isChecked){
                    val sugarcanelbl = application.assets.open("sugarcane.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "Result:\n${sugarcanelbl[max]}\nConfidence: ${"%.2f".format(confidence)}%"
                    textView.text = resultText}
                else{
                    val sugarcanelbl = application.assets.open("marathi/sugarcane_m.txt").bufferedReader().use { it.readText() }.split("\n")
                    val resultText = "निकाल:\n${sugarcanelbl[max]}\nखात्री: ${"%.2f".format(confidence)}%"
                    textView.text = resultText
                }
                model.close()
            }
            else{
                if(!yourSwitch.isChecked){
                    Toast.makeText(this, "Please select correct crop type", Toast.LENGTH_SHORT).show()}
                else{
                    Toast.makeText(this, "कृपया योग्य पीक प्रकार निवडा", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showImageSelectionDialog() {
        val yourSwitch: SwitchCompat = findViewById(R.id.switch1)
        var options = arrayOf("Camera", "Gallery")
        if(yourSwitch.isChecked){
            options = arrayOf("कॅमेरा", "गॅलरी")}

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image From:")
            .setItems(options) { dialog: DialogInterface?, which: Int ->
                when (which) {
                    0 -> // Check camera permission
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            // Request the permission
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.CAMERA),
                                MY_CAMERA_PERMISSION_CODE
                            )
                        } else {
                            // Permission already granted, proceed with camera access
                            dispatchTakePictureIntent()
                        }

                    1 -> {
                        openGallery()
                    }
                }
            }

        builder.show()
    }



    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }


    private fun getMax(arr: FloatArray): Int {
        var ind = 0
        var min = Float.MIN_VALUE

        for (i in arr.indices) {
            if (arr[i] > min) {
                min = arr[i]
                ind = i
            }
        }

        return ind
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val yourSwitch: SwitchCompat = findViewById(R.id.switch1)
        if( requestCode==REQUEST_IMAGE_CAPTURE){
            try {
                // Check if data.extras is not null
                val extras = data?.extras
                if (extras != null && extras.containsKey("data")) {
                    // Retrieve the image from data.extras
                    var imageBitmap = extras["data"] as Bitmap
                    val dimension = Math.min(imageBitmap.width, imageBitmap.height)
                    imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, 256, 256)
                    bitmap=imageBitmap
                    imageView.setImageBitmap(imageBitmap)
                    // Now 'bitmap' holds the selected image in a Bitmap format.
                } else if (data?.data != null) {
                    // If data.extras is null, the full-size image might be available at data.data
                    try {
                        val uri: Uri = data.data!!
                        imageView.setImageURI(uri)
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        // Now 'bitmap' holds the selected image in a Bitmap format.
                    } catch (e: IOException) {
                        e.printStackTrace()

                        if(!yourSwitch.isChecked){
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()}
                        else{
                            Toast.makeText(this, "फोटो लोड करताना त्रुटी", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    if(!yourSwitch.isChecked){
                        Toast.makeText(this, "Image Capture Cancelled", Toast.LENGTH_SHORT).show()}
                    else{
                        Toast.makeText(this, "फोटो कॅप्चर रद्द केले", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == REQUEST_IMAGE_PICK) {
            if (resultCode == RESULT_OK && data != null) {
                try {
                    val uri: Uri? = data.data
                    if (uri != null) {
                        imageView.setImageURI(uri)
                        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    } else {
                        // Handle the case where the URI is null (user didn't select an image)
                        // Show an error message or perform any other necessary action
                        if(!yourSwitch.isChecked){
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()}
                        else{
                            Toast.makeText(this, "फोटो लोड करताना त्रुटी", Toast.LENGTH_SHORT).show()
                        }

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                // Handle the case where the user canceled the image selection
                // Show an error message or perform any other necessary action
                if(!yourSwitch.isChecked){
                    Toast.makeText(this, "Image Selection Canceled", Toast.LENGTH_SHORT).show()}
                else{
                    Toast.makeText(this, "फोटो निवड रद्द केली", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
