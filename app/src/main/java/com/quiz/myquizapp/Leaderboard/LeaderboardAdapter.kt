package com.quiz.myquizapp.Leaderboard
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quiz.myquizapp.R
import com.quiz.myquizapp.databinding.ItemLeaderboardBinding

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    private val userList = mutableListOf<UserModel>()

    fun submitList(list: List<UserModel>) {
        userList.clear()
        userList.addAll(list)
        notifyDataSetChanged()
    }

    class LeaderboardViewHolder(val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.nicknameText.text = user.nickname
        holder.binding.levelText.text = "Level: ${user.level}"
        holder.binding.pointText.text = "Points: ${user.point}"

        val avatarResId = when (user.avatarIndex) {
            0 -> R.drawable.avatar_male
            1 -> R.drawable.avatar_female
            2 -> R.drawable.avatar_female2
            3 -> R.drawable.avatar_male2
            else -> R.drawable.default_avatar
        }
        holder.binding.avatarImageView.setImageResource(avatarResId)

        if (position == 0) {
            val animation = android.view.animation.AnimationUtils.loadAnimation(
                holder.itemView.context, R.anim.shake_in
            )
            holder.itemView.startAnimation(animation)
        } else {
            // Diğer kartlara animasyon uygulanmasın diye temizle
            holder.itemView.clearAnimation()
        }
    }

    override fun getItemCount(): Int = userList.size
}
