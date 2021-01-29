package com.example.musicplayer

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.recyclerview_item.view.*


class MainActivity : AppCompatActivity(), RecyclerViewClickListener, RecyclerViewDeleteListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

//        // get saved tracks
//        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
//        val gson = Gson()
//        val json = sharedPref.getString(getString(R.string.saved_tracks), null)
//        if (json != null) {
//            val itemType = object : TypeToken<List<Track>>() {}.type
//            val tracks: List<Track> = gson.fromJson(json, itemType)
//            viewModel.setTracks(tracks)
//        }

        val tracksRV = binding.trackListRV
        val adapter = RecyclerViewAdapter(viewModel.liveTracks.value!!, this, this)

        tracksRV.adapter = adapter
        tracksRV.layoutManager = LinearLayoutManager(this)
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter))
        itemTouchHelper.attachToRecyclerView(tracksRV)

        val trackListUpdateObserver = Observer<MutableList<Track>> { tracks ->
            adapter.setData(tracks)
        }
        viewModel.liveTracks.observe(this, trackListUpdateObserver)

        binding.playIB.setOnClickListener {
            if (!viewModel.paused) {
                if (viewModel.mediaPlayer.isPlaying) {
                    pauseSong()
                } else {
                    startSong()
                }
            }
        }
        binding.stopIB.setOnClickListener {
            stopSong()
        }
        binding.forwardIB.setOnClickListener {
            forwardSong()
        }
        binding.reverseIB.setOnClickListener {
            reverseSong()
        }
        binding.nextIB.setOnClickListener {
            nextSong()
        }
        binding.previousIB.setOnClickListener {
            previousSong()
        }
        handler = Handler()

        setContentView(binding.root)
    }

    override fun recyclerViewListClicked(v: View, position: Int) {
        if (viewModel.currTrack != position || viewModel.paused) {

            // change looks

            binding.trackListRV.layoutManager?.findViewByPosition(viewModel.currTrack)?.layoutCL?.setBackgroundColor(Color.parseColor("#ffffff"))

            viewModel.currTrack = position
            if (!viewModel.paused) {
                stopSong()
            }

            prepareSong(position)
        } else {
            if (viewModel.mediaPlayer.isPlaying) {
                pauseSong()
            } else {
                startSong()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.dialog -> openFileDialog()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun deleteTrack(position: Int) {
        viewModel.tracks.remove(viewModel.liveTracks.value?.get(position))
        viewModel.updateList()
    }

    private fun openFileDialog(): Boolean {
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123)

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val selectedfile: Uri? = data?.data

            val metaDataRetriever = MediaMetadataRetriever()
            metaDataRetriever.setDataSource(this, selectedfile)

            viewModel.addTrack(Track(metaDataRetriever.extractMetadata(METADATA_KEY_TITLE) ?: "",
                metaDataRetriever.extractMetadata(METADATA_KEY_ARTIST)?: "",
                selectedfile.toString(),
                metaDataRetriever.extractMetadata(METADATA_KEY_DURATION)?: ""))
        }
    }

    private fun prepareSong(position: Int) {
        val uri: Uri = Uri.parse(viewModel.liveTracks.value?.get(position)?.path)
        viewModel.mediaPlayer = MediaPlayer()
        viewModel.mediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(applicationContext, uri)
            prepareAsync()
        }
        viewModel.mediaPlayer.setOnPreparedListener {
            binding.seekBar.max = viewModel.mediaPlayer.duration
            startSong()
            updateSeekBar()
        }
        viewModel.mediaPlayer.setOnCompletionListener {
            nextSong()
        }

        binding.trackListRV.layoutManager?.findViewByPosition(viewModel.currTrack)?.layoutCL?.setBackgroundColor(Color.parseColor("#eebbff"))
    }

    private fun startSong() {
        viewModel.mediaPlayer.start()
        binding.playIB.setImageResource(android.R.drawable.ic_media_pause)
        viewModel.paused = false
    }

    private fun pauseSong() {
        viewModel.mediaPlayer.pause()
        binding.playIB.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun stopSong() {
        viewModel.mediaPlayer.stop()
        viewModel.mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
        viewModel.paused = true
        binding.trackListRV.layoutManager?.findViewByPosition(viewModel.currTrack)?.layoutCL?.setBackgroundColor(Color.parseColor("#ffffff"))
    }

    private fun forwardSong() {
        val currentPosition = viewModel.mediaPlayer.currentPosition
        if (currentPosition + 10000 <= viewModel.mediaPlayer.duration) {
            viewModel.mediaPlayer.seekTo(currentPosition + 10000)
        } else {
            viewModel.mediaPlayer.seekTo(viewModel.mediaPlayer.duration)
        }
    }

    private fun reverseSong() {
        val currentPosition = viewModel.mediaPlayer.currentPosition
        if (currentPosition - 10000 > 0) {
            viewModel.mediaPlayer.seekTo(currentPosition - 10000)
        } else {
            viewModel.mediaPlayer.seekTo(0)
        }
    }

    private fun nextSong() {
        stopSong()
        if (viewModel.currTrack + 1 == viewModel.liveTracks.value?.size) {
            viewModel.currTrack = 0
        } else {
            viewModel.currTrack++
        }
        prepareSong(viewModel.currTrack)
    }

    private fun previousSong() {
        stopSong()
        if (viewModel.currTrack - 1 == -1) {
            viewModel.currTrack = viewModel.liveTracks.value?.size?.minus(1)!!
        } else {
            viewModel.currTrack--
        }
        prepareSong(viewModel.currTrack)
    }

    private val runnable = Runnable {
        updateSeekBar()
    }

    private fun updateSeekBar() {
        binding.seekBar.progress = viewModel.mediaPlayer.currentPosition
        handler.postDelayed(runnable, 50)
    }
}