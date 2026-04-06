package com.darumahq.rainbowclimb.util

object Constants {
    // Virtual resolution (pixel art)
    const val VIRTUAL_WIDTH = 240f
    const val VIRTUAL_HEIGHT = 400f

    // Tile size
    const val TILE_SIZE = 32f

    // Physics
    const val GRAVITY = -900f
    const val JUMP_VELOCITY = 350f
    const val MOVE_SPEED = 150f
    const val MAX_FALL_SPEED = -500f

    // Player
    const val PLAYER_WIDTH = 16f
    const val PLAYER_HEIGHT = 24f

    // Rainbow
    const val RAINBOW_MAX_AMMO = 3
    const val RAINBOW_REGEN_TIME = 1f // seconds
    const val RAINBOW_DURATION = 4f   // seconds
    const val RAINBOW_LENGTH = 96f    // pixels (~3 tiles)
    const val RAINBOW_WIDTH = 16f

    // Camera
    const val BASE_SCROLL_SPEED = 30f  // pixels/second
    const val DIFFICULTY_INCREASE = 0.002f // +0.2% per level

    // Chunks
    const val CHUNK_HEIGHT = 16  // in tiles
    const val CHUNK_HEIGHT_PX = CHUNK_HEIGHT * TILE_SIZE
    const val PLATFORMS_PER_LEVEL = 50

    // Enemies
    const val ENEMY_SIZE = 16f
    const val BOMBER_WIDTH = 16f
    const val BOMBER_HEIGHT = 24f

    // Projectiles
    const val PROJECTILE_SIZE = 6f
    const val PROJECTILE_SPEED = 120f
    const val BOMB_FALL_SPEED = -150f
    const val PROJECTILE_LIFETIME = 3f

    // Enemy AI
    const val SHOOTER_COOLDOWN = 2.5f
    const val BOMBER_COOLDOWN = 3.0f
    const val BOMBER_DROP_RANGE_X = 20f
    const val BOMBER_HEIGHT_OFFSET = 60f
    const val CHASER_DETECT_RADIUS = 96f  // 3 tiles
    const val CHASER_DURATION = 3f
    const val CHASER_SPEED_MULT = 2.5f

    // Power-ups
    const val POWERUP_SPAWN_CHANCE = 0.05f
}
