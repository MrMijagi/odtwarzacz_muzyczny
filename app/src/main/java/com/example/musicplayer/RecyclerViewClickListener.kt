package com.example.musicplayer

import android.view.View

interface RecyclerViewClickListener {
    fun recyclerViewListClicked(v: View, position: Int)
}