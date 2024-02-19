package ru.networkignav.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.networkignav.R
import ru.networkignav.databinding.JobItemBinding
import ru.networkignav.dto.FeedItem
import ru.networkignav.dto.Job
import ru.networkignav.util.Formatter.formatJobDate


class JobsAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val isProfileMine: Boolean
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(JobDiffCallback()) {

    override fun getItemViewType(position: Int): Int = R.layout.job_item

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.job_item -> {
                val binding =
                    JobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                JobViewHolder(binding, onInteractionListener, isProfileMine)
            }

            else -> {
                error("unknown item type: $viewType")
            }
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Job -> (holder as? JobViewHolder)?.bind(item)
            else -> {
                error("unknown item type")}
        }
    }

    class JobViewHolder(
        private val binding: JobItemBinding,
        private val onInteractionListener: OnInteractionListener,
        private val isProfileMine: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Job) {
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

