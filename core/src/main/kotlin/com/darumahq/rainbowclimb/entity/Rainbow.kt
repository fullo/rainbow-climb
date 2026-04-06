package com.darumahq.rainbowclimb.entity

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.darumahq.rainbowclimb.util.Constants

class Rainbow {
    val position = Vector2()
    val bounds = Rectangle()
    var active = false
    var timer = 0f
    var direction = 0 // -1 left, 0 up, 1 right

    fun activate(startX: Float, startY: Float, dir: Int) {
        active = true
        timer = Constants.RAINBOW_DURATION
        direction = dir

        // Position the rainbow arc relative to the player
        when (dir) {
            -1 -> position.set(startX - Constants.RAINBOW_LENGTH, startY + Constants.PLAYER_HEIGHT * 0.5f)
            0 -> position.set(startX - Constants.RAINBOW_WIDTH / 2f, startY + Constants.PLAYER_HEIGHT)
            1 -> position.set(startX + Constants.PLAYER_WIDTH, startY + Constants.PLAYER_HEIGHT * 0.5f)
        }

        updateBounds()
    }

    fun update(delta: Float) {
        if (!active) return
        timer -= delta
        if (timer <= 0f) {
            active = false
        }
    }

    private fun updateBounds() {
        if (direction == 0) {
            // Vertical rainbow
            bounds.set(position.x, position.y, Constants.RAINBOW_WIDTH, Constants.RAINBOW_LENGTH)
        } else {
            // Horizontal rainbow
            bounds.set(position.x, position.y, Constants.RAINBOW_LENGTH, Constants.RAINBOW_WIDTH)
        }
    }

    fun reset() {
        active = false
        timer = 0f
    }
}
