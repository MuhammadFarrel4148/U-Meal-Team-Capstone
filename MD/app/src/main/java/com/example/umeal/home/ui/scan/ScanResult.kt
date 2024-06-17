package com.example.umeal.home.ui.scan

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.umeal.R
import com.example.umeal.adapter.ScanResultAdapter
import com.example.umeal.data.ResultState
import com.example.umeal.data.repository.DataRepository
import com.example.umeal.data.response.DetectedFoodsItem
import com.example.umeal.data.response.ScanFoodResponse
import com.example.umeal.data.retrofit.ApiConfig
import com.example.umeal.databinding.ActivityScanResultBinding
import com.example.umeal.home.HomeActivity
import com.example.umeal.utils.PreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ScanResult : AppCompatActivity() {

    private lateinit var viewModel: ScanImageViewModel
    private lateinit var preferenceManager: PreferenceManager
    private var uploadJob: Job = Job()
    private lateinit var binding: ActivityScanResultBinding

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        initializeViewModel()

        setSupportActionBar(binding.toolbarScan)
        supportActionBar?.title = getString(R.string.hasil_scan)

        showLoading(true)

        binding.rvDetectedFood.layoutManager = LinearLayoutManager(this)

        val imageUriString = intent.getStringExtra("CROPPED_IMAGE_URI")
        imageUriString?.let {
            val imageUri = Uri.parse(it)
            val selectedFile = uriToFile(imageUri, this)
            Handler(Looper.getMainLooper()).postDelayed({
                uploadImage(selectedFile)
                binding.lottieLoading.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .withEndAction {
                        showLoading(false)
                        binding.ivImageScan.setImageURI(imageUri)
                    }
            }, 3000)
        }

    }

    private fun initializeViewModel() {
        val apiService = ApiConfig.getApiService()
        val dataRepository = DataRepository(apiService)
        val factory = ScanImageViewModelFactory(dataRepository)
        viewModel = ViewModelProvider(this, factory)[ScanImageViewModel::class.java]
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.lottieLoading.visibility = View.VISIBLE
            binding.backgroundOverlay.visibility = View.VISIBLE
            binding.cvImageScan.visibility = View.GONE
            binding.tvDetectedFood.visibility = View.GONE
            binding.rvDetectedFood.visibility = View.GONE
        } else {
            binding.backgroundOverlay.visibility = View.GONE
            binding.lottieLoading.visibility = View.GONE
            binding.cvImageScan.visibility = View.VISIBLE
            binding.tvDetectedFood.visibility = View.VISIBLE
            binding.rvDetectedFood.visibility = View.VISIBLE
        }
    }

    private fun uploadImage(file: File) {
        val initialFileSize = file.length()
        Log.d("FileSize", "Selected file size: $initialFileSize bytes")

        val reducedFile = reduceFileImage(file)
        val finalFileSize = reducedFile.length()
        Log.d("FileSize", "Reduced file size: $finalFileSize bytes")

        val requestImageFile = reducedFile.asRequestBody("image/jpg".toMediaTypeOrNull())
        val imageMultipart = MultipartBody.Part.createFormData(
            "image",
            reducedFile.name,
            requestImageFile
        )

        lifecycle.coroutineScope.launchWhenResumed {
            if (uploadJob.isActive) uploadJob.cancel()
            uploadJob = launch {
                viewModel.scanImage(preferenceManager.token, imageMultipart).collect {
                    handleUploadResult(it)
                }
            }
        }
    }

    private fun handleUploadResult(result: ResultState<ScanFoodResponse>) {
        when (result) {
            is ResultState.Success -> {
                Toast.makeText(this, R.string.success_upload, Toast.LENGTH_SHORT).show()
                val scanData = result.data?.data
                if (scanData != null) {
                    displayScanResults(scanData.detectedFoods)
                }
            }

            is ResultState.Loading -> {
                showLoading(true)
            }

            is ResultState.Error -> {
                Toast.makeText(this, R.string.error_upload, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }


    private fun displayScanResults(results: List<DetectedFoodsItem?>) {
        if (results.isEmpty()) {
            binding.rvDetectedFood.visibility = View.GONE
            binding.tvNoFoodDetected.visibility = View.VISIBLE
            binding.ivNoFoodDetected.visibility = View.VISIBLE
        } else {
            val adapter = ScanResultAdapter(results)
            binding.rvDetectedFood.adapter = adapter
        }

    }

    private fun reduceFileImage(file: File): File {
        val initialFileSize = file.length()
        Log.d("FileSize", "Initial file size: $initialFileSize bytes")
        val bitmap = BitmapFactory.decodeFile(file.path)

        val width = bitmap.width
        val height = bitmap.height
        val maxWidth = 800
        val maxHeight = 800
        val aspectRatio = width.toFloat() / height.toFloat()
        val scaledWidth: Int
        val scaledHeight: Int
        if (width > height) {
            scaledWidth = maxWidth
            scaledHeight = (scaledWidth / aspectRatio).toInt()
        } else {
            scaledHeight = maxHeight
            scaledWidth = (scaledHeight * aspectRatio).toInt()
        }
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

        var compressQuality = 70
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 900000)

        FileOutputStream(file).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, out)
        }

        val finalFileSize = file.length()
        Log.d("FileSize", "Final file size: $finalFileSize bytes")

        return file
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        contentResolver.openInputStream(selectedImg)?.use { inputStream ->
            FileOutputStream(myFile).use { outputStream ->
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) {
                    outputStream.write(buf, 0, len)
                }
            }
        }

        return myFile
    }

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "IMG_",
            ".jpg",
            storageDir
        )
    }
}
