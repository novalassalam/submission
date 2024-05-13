package com.dicoding.asclepius.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import com.dicoding.asclepius.BuildConfig
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar)

        Log.d("API_KEY", "BuildConfig.NEWS_API_KEY: ${BuildConfig.NEWS_API_KEY}")

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            startUCrop(uri)
        } else {
            Log.e("Photo Picker", "No media selected")
            showToast(getString(R.string.no_media_selected))
        }
    }

    private fun startUCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))

        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .start(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            currentImageUri = resultUri
            showImage()
            binding.welcomeCl.visibility = View.GONE
            binding.analyzeButton.visibility = View.VISIBLE
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Log.e("uCrop", "Image crop failed: $cropError")
            showToast(getString(R.string.image_crop_failed))
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.visibility = View.VISIBLE
            Log.i("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun clearImage() {
        currentImageUri = null
        binding.previewImageView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_place_holder, null))
    }

    private fun analyzeImage() {
        currentImageUri?.let {
            binding.progressIndicator.visibility = View.VISIBLE

            val imageClassifierHelper = ImageClassifierHelper(
                context = this,
                classifierListener = object : ImageClassifierHelper.ClassifierListener {
                    override fun onError(error: String) {
                        binding.progressIndicator.visibility = View.GONE
                        showToast(error)
                    }

                    override fun onResults(results: List<Classifications>?) {
                        if (results != null) {
                            Log.i("Image Classification", "onResults: $results")
                            val topResult = results[0].categories[0]

                            moveToResult(topResult)

                            clearImage()
                        }
                        binding.welcomeCl.visibility = View.VISIBLE

                        binding.previewImageView.visibility = View.GONE
                        binding.progressIndicator.visibility = View.GONE
                    }
                }
            )
            imageClassifierHelper.classifyStaticImage(it)
        } ?: showToast(getString(R.string.no_image_selected))
    }

    private fun moveToResult(analyzedResult: Category) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_RESULT_LABEL, analyzedResult.label)
        intent.putExtra(ResultActivity.EXTRA_RESULT_DISPLAY_NAME, analyzedResult.displayName)
        intent.putExtra(ResultActivity.EXTRA_RESULT_SCORE, analyzedResult.score)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_history) {
            Intent(this, HistoryActivity::class.java).also {
                startActivity(it)
            }
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}