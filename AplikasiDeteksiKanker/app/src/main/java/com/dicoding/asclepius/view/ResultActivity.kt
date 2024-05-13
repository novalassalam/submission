package com.dicoding.asclepius.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.asclepius.R
import com.dicoding.asclepius.adapter.NewsAdapter
import com.dicoding.asclepius.data.database.HistoryDatabase
import com.dicoding.asclepius.data.entity.HistoryEntity
import com.dicoding.asclepius.data.repository.HistoryRepository
import com.dicoding.asclepius.data.repository.NewsRepository
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.util.Resource
import com.dicoding.asclepius.util.getCurrentDate
import com.dicoding.asclepius.util.showToast
import com.dicoding.asclepius.viewmodel.HistoryViewModel
import com.dicoding.asclepius.viewmodel.NewsViewModel
import com.dicoding.asclepius.viewmodel.ViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var newsViewModel: NewsViewModel

    private lateinit var newsAdapter: NewsAdapter

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val historyRepository = HistoryRepository(HistoryDatabase.getDatabaseInstance(applicationContext).historyDao())
        val newsRepository = NewsRepository()
        val viewModelFactory = ViewModelFactory(application, historyRepository, newsRepository)

        newsViewModel = ViewModelProvider(this, viewModelFactory)[NewsViewModel::class.java]
        historyViewModel = ViewModelProvider(this, viewModelFactory)[HistoryViewModel::class.java]

        initRecyclerView()

        setSupportActionBar(binding.myToolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        imageUri?.let {
            Log.d("Image URI", "$it")

            binding.resultImage.setImageURI(it)

            val resultLabel = intent.getStringExtra(EXTRA_RESULT_LABEL)
            val convertedResultLabel = resultLabel?.replace("\\s".toRegex(), "")
                ?.lowercase(Locale.ROOT)
            Log.d("Result Label", convertedResultLabel ?: "Result label is null")

            when (convertedResultLabel) {
                "cancer" -> {
                    binding.resultText.text = getString(R.string.result_text_cancer)
                }
                "noncancer" -> {
                    binding.resultText.text = getString(R.string.result_text_no_cancer)
                }
                else -> {
                    binding.resultText.text = getString(R.string.result_text_unknown)
                }
            }
        }

        binding.saveResultFab.setOnClickListener { saveResult() }

        newsViewModel.news.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    binding.articleRv.visibility = View.VISIBLE
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    binding.noArticleTv.visibility = View.VISIBLE
                    response.message?.let { message ->
                        binding.noArticleTv.text = buildString {
                            append("An error occurred: ")
                            append(message)
                        }
                        Log.e("NewsViewModel", "An error occurred: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        newsAdapter.setOnItemClickListener(object : NewsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                Log.i("NewsAdapter", "Item clicked at position $position")

                val article = newsAdapter.differ.currentList[position]
                val articleUrl = article.url
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl))
                startActivity(intent)
            }
        })
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT_LABEL = "extra_result_label"
        const val EXTRA_RESULT_DISPLAY_NAME = "extra_result_display_name"
        const val EXTRA_RESULT_SCORE = "extra_result_SCORE"
    }

    private fun saveResult() {
        val imagePath = saveImage(binding.resultImage.drawable)

        val history = HistoryEntity(
            label = intent.getStringExtra(EXTRA_RESULT_LABEL) ?: "",
            score = intent.getFloatExtra(EXTRA_RESULT_SCORE, 0f),
            image = imagePath,
            date = getCurrentDate()
        )

        historyViewModel.insertHistory(history)

        showToast(this, getString(R.string.result_saved_successfully))

        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_history) {
            Log.i("Toolbar", "History menu clicked")
            return true
        } else if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun saveImage(drawable: Drawable): String {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val fileName = "result_image_${System.currentTimeMillis()}.jpg"
        val file = File(getExternalFilesDir(null), fileName)

        try {
            FileOutputStream(file).use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                outStream.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.absolutePath
    }

    private fun hideProgressBar() {
        binding.progressIndicator.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.progressIndicator.visibility = View.VISIBLE
        isLoading = true
    }

//    val scrollListener = object : RecyclerView.OnScrollListener() {
//        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//            super.onScrolled(recyclerView, dx, dy)
//
//            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
//            val visibleItemCount = layoutManager.childCount
//            val totalItemCount = layoutManager.itemCount
//
//            val isNoError = !isError
//            val isNotLoading = !isLoading
//            val isLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
//            val isNotAtBeginning = firstVisibleItemPosition >= 0
//            val isTotalMoreThanVisible = totalItemCount >= 5
//            val shouldPaginate = isNoError && isNotLoading && isLastItem && isNotAtBeginning &&
//                    isTotalMoreThanVisible && isScrolling
//            if (shouldPaginate) {
//                newsViewModel.getNews("id")
//                isScrolling = false
//            }
//        }
//
//        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//            super.onScrollStateChanged(recyclerView, newState)
//
//            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//                isScrolling = true
//            }
//        }
//    }

    private fun initRecyclerView() {
        newsAdapter = NewsAdapter()

        binding.articleRv.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(this@ResultActivity)
        }
    }
}