package ru.networkignav.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.networkignav.R
import ru.networkignav.databinding.EventItemBinding
import ru.networkignav.dto.AttachmentType
import ru.networkignav.dto.Event
import ru.networkignav.dto.FeedItem
import ru.networkignav.util.Formatter


class EventsAdapter(
    private val context: Context,
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(EventDiffCallback()) {

    override fun getItemViewType(position: Int): Int = R.layout.event_item

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.event_item -> {
                val binding =
                    EventItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventViewHolder(context, binding, onInteractionListener)
            }

            else -> {
                error("unknown item type: $viewType")
            }
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Event -> (holder as? EventViewHolder)?.bind(item)
            else -> {
                error("unknown item type")}
        }
    }

    class EventViewHolder(
        context: Context,
        private val binding: EventItemBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val player = ExoPlayer.Builder(context).build()

        init {
            binding.eventAudio.player = player
            binding.eventAudio.player = player
        }

        fun bind(event: Event) {
            binding.apply {
                author.text = event.author
                eventInfo.text = event.content
                createdAt.text = Formatter.formatPostDate(event.published)

                val url = event.authorAvatar
                    ?: "https://ob-kassa.ru/content/front/buhoskol_tmp1/images/reviews-icon.jpg"
                Glide.with(postUserAvatar)
                    .load(url)
                    .circleCrop()
                    .timeout(10_000)
                    .into(postUserAvatar)
                if (event.link != null) {
                    link.visibility = View.VISIBLE
                    link.text = event.link
                } else {
                    link.visibility = View.GONE
                }
                eventDateTime.text = Formatter.formatEventDate(event.datetime)
                if (event.attachment != null) {
                    when (event.attachment.type) {
                        AttachmentType.IMAGE -> {
                            val imageUrl = event.attachment.url
                            eventImage.visibility = View.VISIBLE
                            Glide.with(eventImage)
                                .load(imageUrl)
                                .timeout(10_000)
                                .into(eventImage)

                            eventVideo.visibility = View.GONE
                            eventAudio.visibility = View.GONE
                        }

                        AttachmentType.VIDEO -> {
                            val videoUrl = event.attachment.url
                            eventVideo.visibility = View.VISIBLE

                            val mediaItem = MediaItem.fromUri(videoUrl)
                            eventAudio.visibility = View.VISIBLE
                            player.setMediaItem(mediaItem)
                            player.prepare()

                            eventVideo.requestFocus()

                            eventImage.visibility = View.GONE
                            eventAudio.visibility = View.GONE
                        }

                        AttachmentType.AUDIO -> {

                            val audioUrl = event.attachment.url
                            val mediaItem = MediaItem.fromUri(audioUrl)
                            eventAudio.visibility = View.VISIBLE
                            player.setMediaItem(mediaItem)
                            player.prepare()



                            eventImage.visibility = View.GONE
                            eventVideo.visibility = View.GONE
                        }
                    }
                } else {
                    eventImage.visibility = View.GONE
                    eventVideo.visibility = View.GONE
                    eventAudio.visibility = View.GONE
                }
                menu.isVisible = event.ownedByMe

                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_menu)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    onInteractionListener.onRemoveEvent(event)
                                    true
                                }

                                R.id.edit -> {
                                    onInteractionListener.onEditEvent(event)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
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

