package ru.networkignav.adapter

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.networkignav.R
import ru.networkignav.databinding.PostItemBinding
import ru.networkignav.dto.AttachmentType
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
    private val context: Context,
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
                PostViewHolder(context, binding, onInteractionListener)
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
        private val context: Context,
        private val binding: PostItemBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val mediaController = MediaController(context)
        val player = ExoPlayer.Builder(context).build()

        init {
            binding.postAudio.player = player
            binding.postVideo.player = player
        }

        fun bind(post: Post) {
            binding.apply {
                author.text = post.author
                content.text = post.content
                createdAt.text = post.published

                val url = post.authorAvatar
                    ?: "https://ob-kassa.ru/content/front/buhoskol_tmp1/images/reviews-icon.jpg"
                Glide.with(postUserAvatar)
                    .load(url)
                    .circleCrop()
                    .timeout(10_000)
                    .into(postUserAvatar)
                if (post.attachment != null) {
                    when (post.attachment.type) {
                        AttachmentType.IMAGE -> {
                            val imageUrl = post.attachment.url
                            postImage.visibility = View.VISIBLE
                            Glide.with(postImage)
                                .load(imageUrl)
                                .timeout(10_000)
                                .into(postImage)

                            postVideo.visibility = View.GONE
                            postAudio.visibility = View.GONE
                        }

                        AttachmentType.VIDEO -> {
                            val videoUrl = post.attachment.url
                            postVideo.visibility = View.VISIBLE

                            val mediaItem = MediaItem.fromUri(videoUrl)
                            postAudio.visibility = View.VISIBLE
                            player.setMediaItem(mediaItem)
                            player.prepare()

                            postVideo.requestFocus()

                            postImage.visibility = View.GONE
                            postAudio.visibility = View.GONE
                        }

                        AttachmentType.AUDIO -> {

                            val audioUrl = post.attachment.url
                            val mediaItem = MediaItem.fromUri(audioUrl)
                            postAudio.visibility = View.VISIBLE
                            player.setMediaItem(mediaItem)
                            player.prepare()



                            postImage.visibility = View.GONE
                            postVideo.visibility = View.GONE
                        }
                    }
                } else {
                    postImage.visibility = View.GONE
                    postVideo.visibility = View.GONE
                    postAudio.visibility = View.GONE
                }
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

