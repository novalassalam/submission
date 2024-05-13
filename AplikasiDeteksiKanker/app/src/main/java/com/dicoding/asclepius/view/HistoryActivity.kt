package com.dicoding.asclepius.view

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.dicoding.asclepius.adapter.HistoryAdapter
import com.dicoding.asclepius.data.database.HistoryDatabase
import com.dicoding.asclepius.data.entity.HistoryEntity
import com.dicoding.asclepius.data.repository.HistoryRepository
import com.dicoding.asclepius.data.repository.NewsRepository
import com.dicoding.asclepius.databinding.ActivityHistoryBinding
import com.dicoding.asclepius.viewmodel.HistoryViewModel
import com.dicoding.asclepius.viewmodel.ViewModelFactory

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding

    private lateinit var viewModel: HistoryViewModel

    private val historyAdapter: HistoryAdapter by lazy { HistoryAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.myToolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val historyRepository = HistoryRepository(HistoryDatabase.getDatabaseInstance(applicationContext).historyDao())
        val newsRepository = NewsRepository()
        val viewModelFactory = ViewModelFactory(application, historyRepository, newsRepository)

        viewModel = ViewModelProvider(this, viewModelFactory)[HistoryViewModel::class.java]

        initRecyclerView()
        initHistory()
    }

    private fun initRecyclerView() {
        binding.historyRv.layoutManager = GridLayoutManager(this, 2)
        binding.historyRv.adapter = historyAdapter

        historyAdapter.setOnItemHoldCallback { history ->
            showDeleteDialog(history)
        }
    }

    private fun initHistory() {
        viewModel.getAllHistory()
        viewModel.allHistory.observe(this) { historyList ->
            if (historyList.isEmpty()) {
                binding.emptyHistoryTv.visibility = View.VISIBLE
                binding.historyRv.visibility = View.GONE
            } else {
                binding.emptyHistoryTv.visibility = View.GONE
                binding.historyRv.visibility = View.VISIBLE
                historyAdapter.setHistory(historyList)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog(historyEntity: HistoryEntity) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Hapus histori")
        builder.setMessage("Apakah Anda yakin ingin menghapus histori tersebut?")
        builder.setPositiveButton("Ya") { _, _ ->
            viewModel.deleteHistory(historyEntity)
        }
        builder.setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
}