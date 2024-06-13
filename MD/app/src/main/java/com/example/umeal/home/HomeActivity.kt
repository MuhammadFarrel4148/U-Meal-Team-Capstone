package com.example.umeal.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.umeal.R
import com.example.umeal.databinding.ActivityHomeBinding
import com.example.umeal.home.ui.scan.ResponseScanImage
import com.example.umeal.home.ui.scan.ScanImageViewModel
import com.example.umeal.home.ui.scan.ScanImageViewModelFactory
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import com.example.umeal.data.retrofit.ApiConfig
import com.example.umeal.home.ui.scan.ScanResult
import com.example.umeal.utils.PreferenceManager
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var currentPhotoPath: String
    private var selectedFile: File? = null
    private var uploadJob: Job = Job()
    private lateinit var viewModel: ScanImageViewModel

    private lateinit var preferenceManager: PreferenceManager

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startTakePhoto()
        } else {
            Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.hide()

        currentPhotoPath = ""

        preferenceManager = PreferenceManager(this)

        initializeViewModel()
        setupNavigation()

        binding.buttonScan.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this)
            .setTitle("Choose Image Source")
            .setItems(options) { dialog: DialogInterface, which: Int ->
                when (which) {
                    0 -> startTakePhoto()
                    1 -> startGallery()
                }
            }
            .show()
    }
    private fun initializeViewModel() {
        val apiService = ApiConfig.getApiService()
        val dataRepository = DataRepository(apiService)
        val factory = ScanImageViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this, factory)[ScanImageViewModel::class.java]
    }

    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment_activity_home)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_history, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun startTakePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            createCustomTempFile(application).also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.umeal.fileprovider",
                    it
                )
                currentPhotoPath = it.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                launcherIntentCamera.launch(takePictureIntent)
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            selectedFile = myFile
            val uri = Uri.fromFile(myFile)
            startCrop(uri)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            selectedFile = myFile
            val uri = Uri.fromFile(myFile)
            startCrop(uri)
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true)
        }
        UCrop.of(uri, destinationUri)
            .withOptions(options)
            .withAspectRatio(1f, 1f)
            .start(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                selectedFile = uriToFile(it, this)
                uploadImage()
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            UCrop.getError(data!!)?.printStackTrace()
        }
    }

    private fun createCustomTempFile(application: Application): File {
        val storageDir: File? = application.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    private fun uploadImage() {
        selectedFile?.let { file ->
            val reducedFile = reduceFileImage(file)
            val requestImageFile = reducedFile.asRequestBody("image/jpg".toMediaTypeOrNull())
            val imageMultipart = MultipartBody.Part.createFormData(
                "photo",
                reducedFile.name,
                requestImageFile
            )

            lifecycle.coroutineScope.launchWhenResumed {
                if (uploadJob.isActive) uploadJob.cancel()
                uploadJob = launch {
                    viewModel.scanImage(preferenceManager.token, imageMultipart).collect { result ->
                        handleUploadResult(result)
                    }
                }
            }
        } ?: Toast.makeText(this, R.string.error_no_image, Toast.LENGTH_SHORT).show()
    }

    private fun handleUploadResult(result: ResultState<ResponseScanImage>) {
        when (result) {
            is ResultState.Success -> {
                Toast.makeText(this, R.string.success_add_story, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ScanResult::class.java))
                finishAffinity()
            }
            is ResultState.Loading -> {
                // Show a loading indicator if necessary
            }
            is ResultState.Error -> {
                Toast.makeText(this, R.string.error_add_story, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 900000) // Ensure the size is below 900 KB

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, out)
        }

        return file
    }

    private val FILENAME_FORMAT = "dd-MMM-yyyy"

    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(myFile).use { outputStream ->
                    val buf = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buf).also { len = it } > 0) {
                        outputStream.write(buf, 0, len)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return myFile
    }
}