package com.darumahq.rainbowclimb.entity

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.darumahq.rainbowclimb.util.Constants

class Rainbow {
    val position = Vector2()
    val bounds = Rectangle()
    var active = false
    var timer = 0f
    var direction = 1 // -1 left, 1 right (no more vertical)

    fun activate(startX: Float, startY: Float, dir: Int) {
        active = true
        timer = Constants.RAINBOW_DURATION
        direction = if (dir >= 0) 1 else -1

        // Position the rainbow platform relative to the player
        // Always horizontal: creates a bridge in the direction the player faces
        val offsetY = Constants.PLAYER_HEIGHT * 0.4f  // slightly below player center
        if (direction == -1) {
            position.set(startX - Constants.RAINBOW_LENGTH, startY + offsetY)
        } else {
            position.set(startX + Constants.PLAYER_WIDTH, startY + offsetY)
        }

        // Horizontal platform bounds
        bounds.set(position.x, position.y, Constants.RAINBOW_LENGTH, Constants.RAINBOW_WIDTH)
    }

    fun update(delta: Float) {
        if (!active) return
        timer -= delta
        if (timer <= 0f) {
            active = false
        }
    }

    fun reset() {
        active = false
        timer = 0f
    }
}
