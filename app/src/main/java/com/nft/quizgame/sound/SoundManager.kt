package com.nft.quizgame.sound

import android.media.AudioAttributes
import android.media.AudioAttributes.USAGE_GAME
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.SparseArray
import androidx.core.util.contains
import androidx.core.util.forEach
import com.nft.quizgame.common.QuizAppState
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SoundManager {

    private val resourceArray = intArrayOf(/*R.raw.test_pop_up*/)
    private val resMap = SparseArray<SoundState>()

    private val soundPool: SoundPool
    private val loadCompleteListener = SoundPool.OnLoadCompleteListener { _, soundId, status ->
        resMap.forEach foreach@{ _, soundState ->
            if (soundState.soundId == soundId) {
                soundState.state = status
                return@foreach
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    init {
        val attrs = AudioAttributes.Builder().setUsage(USAGE_GAME).build()
        soundPool = SoundPool.Builder().setMaxStreams(10).setAudioAttributes(attrs).build()
    }

    suspend fun preloadSounds() = suspendCoroutine<Unit?> { cont ->
        if (resourceArray.isEmpty()) {
            cont.resume(null)
            return@suspendCoroutine
        }
//        if (mediaPlayer == null) {
//            try {
//                mediaPlayer = MediaPlayer.create(QuizAppState.getContext(), R.raw.test_ring)
//                mediaPlayer!!.isLooping = true
//            } catch (e: Exception) {
//                cont.resumeWithException(e)
//                return@suspendCoroutine
//            }
//        }
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            resMap.forEach foreach@{ _, soundState ->
                if (soundState.soundId == soundId) {
                    soundState.state = status
                    return@foreach
                }
            }
            var isReady = true
            resMap.forEach foreach@{ _, soundState ->
                if (soundState.state != 0) {
                    isReady = false
                    return@foreach
                }
            }
            if (isReady) {
                soundPool.setOnLoadCompleteListener(loadCompleteListener)
                cont.resume(null)
            }
        }

        resourceArray.forEach { resId ->
            if (!resMap.contains(resId)) {
                resMap.put(resId, SoundState())
            }
        }
        resMap.forEach { resId, soundState ->
            if (soundState.state != 0) {
                val soundId = soundPool.load(QuizAppState.getContext(), resId, 1)
                if (soundId > 0) {
                    soundState.soundId = soundId
                } else {
                    cont.resumeWithException(RuntimeException())
                    return@suspendCoroutine
                }
            }
        }
    }

    fun playMusic(resId: Int) {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun playSound(resId: Int, loop: Boolean = false) {
        var soundState: SoundState? = null
        if (resMap.contains(resId)) {
            soundState = resMap.get(resId)
        } else {
            val soundId = soundPool.load(QuizAppState.getContext(), resId, 1)
            if (soundId > 0) {
                soundState = SoundState().apply {
                    this.soundId = soundId
                }
                resMap.put(resId, soundState)
            }
        }
        if (soundState?.soundId!! > 0 && soundState.state == 0) {
            soundPool.play(soundState.soundId, 1f, 1f, 1, if (loop) -1 else 0, 1f)
        }
    }

    fun onPause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
        soundPool.autoPause()
    }

    fun onResume() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
        soundPool.autoResume()
    }

    class SoundState {
        var soundId = 0
        var state = -1
    }
}