package com.darumahq.rainbowclimb.entity

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.darumahq.rainbowclimb.util.Constants

/**
 * King Pig boss — appears every 5 biomes.
 * Larger than normal enemies (64x48), takes 5 rainbow hits to defeat.
 * Attack pattern: charges toward player, jumps, throws projectiles.
 */
class Boss {
    val position = Vector2()
    val velocity = Vector2()
    val bounds = Rectangle()
    var active = false
    var hp = 5
    var maxHp = 5
    var stateTime = 0f
    var facingRight = false

    // State machine
    enum class State { IDLE, CHARGE, JUMP, ATTACK, HIT, DEAD }
    var state = State.IDLE
    private var stateTimer = 0f
    private var attackCooldown = 0f

    companion object {
        const val WIDTH = 64f
        const val HEIGHT = 48f
        const val SPEED = 200f
        const val CHARGE_SPEED = 350f
        const val JUMP_VELOCITY = 600f
    }

    fun activate(x: Float, y: Float, camY: Float, difficulty: Int) {
        position.set(x, y)
        cameraY = camY
        baseY = y - camY  // store as offset from camera
        active = true
        hp = 5 + difficulty  // harder bosses have more HP
        maxHp = hp
        stateTime = 0f
        state = State.IDLE
        stateTimer = 2f  // idle for 2 seconds before first attack
        attackCooldown = 0f
        velocity.set(0f, 0f)
        bounds.set(x, y, WIDTH, HEIGHT)
    }

    // Camera Y reference — boss stays on-screen relative to camera
    var cameraY = 0f
    private var baseY = 0f  // anchor Y relative to camera

    fun update(delta: Float, playerX: Float, playerY: Float) {
        if (!active) return

        stateTime += delta
        stateTimer -= delta
        if (attackCooldown > 0) attackCooldown -= delta

        facingRight = playerX > position.x

        when (state) {
            State.IDLE -> {
                velocity.x = 0f
                velocity.y = 0f
                // Float toward baseY
                position.y += (baseY + cameraY - position.y) * 3f * delta
                if (stateTimer <= 0f && attackCooldown <= 0f) {
                    state = State.CHARGE
                    stateTimer = 1.5f
                    stateTime = 0f
                }
            }
            State.CHARGE -> {
                // Run toward player horizontally, stay at baseY
                val dir = if (facingRight) 1f else -1f
                velocity.x = dir * CHARGE_SPEED
                velocity.y = 0f
                position.y += (baseY + cameraY - position.y) * 3f * delta

                if (stateTimer <= 0f) {
                    state = State.JUMP
                    velocity.y = JUMP_VELOCITY * 0.5f
                    stateTimer = 0.8f
                    stateTime = 0f
                }
            }
            State.JUMP -> {
                // Controlled jump arc — returns to baseY
                velocity.y -= JUMP_VELOCITY * delta * 2f  // decelerate upward
                val dir = if (facingRight) 1f else -1f
                velocity.x = dir * SPEED

                if (stateTimer <= 0f) {
                    state = State.ATTACK
                    stateTimer = 0.3f
                    attackCooldown = 2f
                    stateTime = 0f
                    velocity.y = 0f
                }
            }
            State.ATTACK -> {
                velocity.x *= 0.9f
                velocity.y = 0f
                // Snap back toward baseY
                position.y += (baseY + cameraY - position.y) * 5f * delta
                if (stateTimer <= 0f) {
                    state = State.IDLE
                    stateTimer = 1f
                    stateTime = 0f
                }
            }
            State.HIT -> {
                velocity.x *= 0.8f
                velocity.y = 0f
                if (stateTimer <= 0f) {
                    if (hp <= 0) {
                        state = State.DEAD
                        stateTimer = 1f
                        stateTime = 0f
                    } else {
                        state = State.IDLE
                        stateTimer = 0.5f
                        stateTime = 0f
                    }
                }
            }
            State.DEAD -> {
                velocity.x = 0f
                // Fall off screen
                velocity.y -= 500f * delta
                if (stateTimer <= 0f) {
                    active = false
                }
            }
        }

        position.x += velocity.x * delta
        position.y += velocity.y * delta

        // Clamp X to screen
        position.x = position.x.coerceIn(0f, Constants.VIRTUAL_WIDTH - WIDTH)

        bounds.set(position.x, position.y, WIDTH, HEIGHT)
    }

    fun takeDamage() {
        if (state == State.DEAD || state == State.HIT) return
        hp--
        state = State.HIT
        stateTimer = 0.3f
        stateTime = 0f
        // Knockback
        velocity.x = if (facingRight) -200f else 200f
        velocity.y = 200f
    }

    fun animState(): String = when (state) {
        State.IDLE -> "idle"
        State.CHARGE -> "run"
        State.JUMP -> "jump"
        State.ATTACK -> "attack"
        State.HIT -> "hit"
        State.DEAD -> "dead"
    }

    fun reset() {
        active = false
        hp = 0
    }
}
