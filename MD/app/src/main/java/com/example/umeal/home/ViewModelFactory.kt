package com.example.umeal.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.umeal.data.repository.ArticleRepository
import com.example.umeal.di.Injection
import com.example.umeal.home.ui.home.HomeViewModel

class ViewModelFactory(private val repository: ArticleRepository) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }

            else -> throw IllegalArgumentException("unknown ViewModel Class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null
        fun getInstance(context: Context): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(Injection.provideArticleRepository(context))
            }.also { instance = it }
    }

}