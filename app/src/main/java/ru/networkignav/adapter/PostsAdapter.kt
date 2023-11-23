package ru.networkignav.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.networkignav.R
import ru.networkignav.databinding.PostItemBinding
import ru.networkignav.dto.FeedItem
import ru.networkignav.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onDislike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onViewImage(post: Post) {}
    fun onShare(post: Post) {}
    fun onRefresh() {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    private val typeAd = 0
    private val typePost = 1
    private val typeDate = 2
    override fun getItemViewType(position: Int): Int = R.layout.post_item


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.post_item -> {
                val binding =
                    PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }

            else -> error("unknown item type: $viewType")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Post -> (holder as? PostViewHolder)?.bind(item)
            else -> error("unknown item type")
        }
    }

    class PostViewHolder(
        private val binding: PostItemBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                author.text = post.userId
                content.text = post.text
                createdAt.text = post.createdAt

                val userId = post.userId
                val url = "https://netomedia.ru/users/$userId/avatar"
                Glide.with(postUserAvatar)
                    .load(url)
                    .circleCrop()
                    .timeout(10_000)
                    .into(postUserAvatar)
            }
        }
    }




    class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return oldItem == newItem
        }


    }
}

