package com.example.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.RecyclerviewItemBinding

class RecyclerViewAdapter  (private var tracks: MutableList<Track>, val itemListener: RecyclerViewClickListener, private val deleteListener: RecyclerViewDeleteListener) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>()  {

    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView), View.OnClickListener {
        private val binding = RecyclerviewItemBinding.bind(listItemView)
        val nameTV = binding.nameTV
        val authorTV = binding.authorTV
        val timeTV = binding.timeTV

        init {
            listItemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            itemListener.recyclerViewListClicked(v, this.layoutPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val trackView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        // Return a new holder instance
        return ViewHolder(trackView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get the data model based on position
        val track: Track = tracks[position]
        // Set item views based on your views and data model
        val name = viewHolder.nameTV
        val author = viewHolder.authorTV
        val time = viewHolder.timeTV

        name.text = track.name
        author.text = track.author

        val minutes = track.time.toInt() / 1000 / 60
        val seconds = track.time.toInt() / 1000 % 60

        time.text = String.format("%02d:%02d", minutes, seconds)
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    fun deleteTrack(position: Int) {
        deleteListener.deleteTrack(position)
        //notifyItemRemoved(position)
    }

    fun setData(newCars: MutableList<Track>) {
        tracks = newCars
        notifyDataSetChanged()
    }
}