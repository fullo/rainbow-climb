package com.darumahq.rainbowclimb.entity

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.darumahq.rainbowclimb.util.Constants

enum class EnemyType {
    WALKER,
    FLYER,
    HOPPER,
    SHOOTER,
    BOMBER,
    CHASER
}

class Enemy {
    val position = Vector2()
    val velocity = Vector2()
    val bounds = Rectangle()
    var type = EnemyType.WALKER
    var active = false
    var baseSpeed = 40f

    // Patrol bounds
    var patrolMinX = 0f
    var patrolMaxX = 0f

    // Sine wave movement (for flyers)
    var sineTimer = 0f
    var sineAmplitude = 30f
    var baseY = 0f

    fun activate(x: Float, y: Float, enemyType: EnemyType, speed: Float = 40f) {
        position.set(x, y)
        type = enemyType
        active = true
        baseSpeed = speed
        baseY = y
        sineTimer = 0f

        when (type) {
            EnemyType.WALKER -> {
                velocity.set(baseSpeed, 0f)
            }
            EnemyType.FLYER -> {
                velocity.set(baseSpeed * 0.5f, 0f)
                sineAmplitude = 30f
            }
            EnemyType.HOPPER -> {
                velocity.set(baseSpeed * 0.8f, 0f)
            }
            else -> {
                velocity.set(0f, 0f)
            }
        }
    }

    fun update(delta: Float) {
        if (!active) return

        when (type) {
            EnemyType.WALKER -> updateWalker(delta)
            EnemyType.FLYER -> updateFlyer(delta)
            EnemyType.HOPPER -> updateHopper(delta)
            EnemyType.SHOOTER -> {} // stationary, shoot logic in World
            EnemyType.BOMBER -> updateFlyer(delta) // similar to flyer
            EnemyType.CHASER -> {} // chase logic in World
        }

        bounds.set(position.x, position.y, Constants.ENEMY_SIZE, Constants.ENEMY_SIZE)
    }

    private fun updateWalker(delta: Float) {
        position.x += velocity.x * delta
        if (position.x <= patrolMinX || position.x >= patrolMaxX) {
            velocity.x = -velocity.x
        }
    }

    private fun updateFlyer(delta: Float) {
        position.x += velocity.x * delta
        sineTimer += delta * 3f
        position.y = baseY + kotlin.math.sin(sineTimer.toDouble()).toFloat() * sineAmplitude

        if (position.x <= patrolMinX || position.x >= patrolMaxX) {
            velocity.x = -velocity.x
        }
    }

    private fun updateHopper(delta: Float) {
        position.x += velocity.x * delta
        position.y += velocity.y * delta
        velocity.y += Constants.GRAVITY * delta * 0.3f // lighter gravity for hoppers

        if (position.x <= patrolMinX || position.x >= patrolMaxX) {
            velocity.x = -velocity.x
        }
    }

    fun reset() {
        active = false
        position.set(0f, 0f)
        velocity.set(0f, 0f)
    }
}
