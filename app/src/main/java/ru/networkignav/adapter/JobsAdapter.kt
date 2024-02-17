package ru.networkignav.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.networkignav.R
import ru.networkignav.auth.AppAuth
import ru.networkignav.databinding.JobItemBinding
import ru.networkignav.dto.FeedItem
import ru.networkignav.dto.Job
import ru.networkignav.util.Formatter.formatJobDate


class JobsAdapter(
    private val context: Context,
    private val onInteractionListener: OnInteractionListener,
    private val isProfileMine: Boolean
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(JobDiffCallback()) {

    private val typeAd = 0
    private val typePost = 1
    private val typeDate = 2
    override fun getItemViewType(position: Int): Int = R.layout.job_item


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.job_item -> {
                Log.d("MYLOG", "onCreateViewHolder")
                val binding =
                    JobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                JobViewHolder(context, binding, onInteractionListener, isProfileMine)
            }

            else -> {
                Log.d("MYLOG", "onCreateViewHolder error")
                error("unknown item type: $viewType")
            }
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Job -> (holder as? JobViewHolder)?.bind(item)
            else -> {
                Log.d("MYLOG", "onBindViewHolder error")
                error("unknown item type")}
        }
    }

    class JobViewHolder(
        private val context: Context,
        private val binding: JobItemBinding,
        private val onInteractionListener: OnInteractionListener,
        private val isProfileMine: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Job) {
            Log.d("MYLOG", "bind job")
            binding.apply {
                name.text = job.name
                position.text = job.position
                start.text = formatJobDate(job.start)
                end.text = formatJobDate(job.finish)

                if (job.link != null) {
                    link.visibility = View.VISIBLE
                    link.text = job.link
                } else {
                    link.visibility = View.GONE
                }
                menu.isVisible = isProfileMine

                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_menu)
                        menu.removeItem(R.id.edit)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    onInteractionListener.onRemoveJob(job)
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


    class JobDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
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

