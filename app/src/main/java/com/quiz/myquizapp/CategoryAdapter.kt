package com.quiz.myquizapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quiz.myquizapp.databinding.RecyclerRowBinding

class CategoryAdapter(private var categoryList: List<String>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.binding.textViewCategoryName.text = categoryList[position]
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }
    fun updateList(newList: List<String>) {
        categoryList = newList
        notifyDataSetChanged()
    }
}

