package com.quiz.myquizapp

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quiz.myquizapp.databinding.RecyclerRowBinding

class CategoryAdapter(
    private var categoryList: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.textViewCategoryName.text = category.name

        // Arka plana Glide ile görsel yükle
        Glide.with(holder.itemView.context)
            .load(category.imageUrl)
            .centerCrop()
            .into(holder.binding.imageViewBackground)

        // Animasyonlu tıklama
        holder.itemView.setOnClickListener {
            val scaleAnim = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.click_scale)
            holder.itemView.startAnimation(scaleAnim)

            // Animasyon sonrası yönlendirme
            holder.itemView.postDelayed({
                onItemClick(category)
            }, 100) // Animasyon süresi kadar beklet
        }
    }

    override fun getItemCount(): Int = categoryList.size

    fun updateList(newList: List<Category>) {
        categoryList = newList
        notifyDataSetChanged()
    }
}
