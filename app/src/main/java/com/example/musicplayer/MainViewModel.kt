package com.example.musicplayer

import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var currTrack = -1
    var paused = true
    lateinit var mediaPlayer: MediaPlayer

    val tracks = mutableListOf<Track>()
    val liveTracks: MutableLiveData<MutableList<Track>> by lazy {
        MutableLiveData<MutableList<Track>>(tracks)
    }

    fun updateList() {
        liveTracks.value = tracks
    }

    fun addTrack(track: Track) {
        liveTracks.value?.add(track)
        liveTracks.value = tracks
    }
}