package com.example.umeal.home.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.umeal.adapter.ArticleAdapter
import com.example.umeal.data.response.ResultsItem
import com.example.umeal.databinding.ArticleItemBinding

class ArticlesAdapter : RecyclerView.Adapter<ArticlesAdapter.MyViewHolder>() {
    private var articles: List<ResultsItem> = listOf()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<ResultsItem>) {
        articles = newList
        notifyDataSetChanged()
    }

    class MyViewHolder(private val binding: ArticleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(articles: ResultsItem) {
            binding.apply {
                Glide.with(itemView.context)
                    .load(articles.imageUrl)
                    .into(ivArticleImage)
                tvArticleTitle.text = articles.title
                tvArticleDesc.text = articles.description
            }
            itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(articles.link)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ArticleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(articles[position])
    }


}