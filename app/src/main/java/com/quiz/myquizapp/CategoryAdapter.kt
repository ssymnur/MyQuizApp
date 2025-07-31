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

    inner class CategoryViewHolder(val binding: RecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecyclerRowBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        val context = holder.itemView.context

        // Kategori adını ata
        holder.binding.textViewCategoryName.text = category.name

        // Görseli Glide ile arka plana yükle
        Glide.with(context)
            .load(category.imageUrl)
            .centerCrop()
            .into(holder.binding.imageViewBackground)

        // Tıklanabilirlik ve animasyon
        holder.itemView.setOnClickListener {
            val scaleAnim = AnimationUtils.loadAnimation(context, R.anim.click_scale)
            holder.itemView.startAnimation(scaleAnim)

            // Animasyon bittikten sonra callback çağır
            holder.itemView.postDelayed({
                onItemClick(category)
            }, 100)
        }
    }

    override fun getItemCount(): Int = categoryList.size

    fun updateList(newList: List<Category>) {
        categoryList = newList
        notifyDataSetChanged()
    }
}
