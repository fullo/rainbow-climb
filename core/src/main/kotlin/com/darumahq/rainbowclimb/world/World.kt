package com.darumahq.rainbowclimb.world

import com.darumahq.rainbowclimb.entity.*
import com.darumahq.rainbowclimb.util.Constants
import com.darumahq.rainbowclimb.util.SeededRandom

class World(seed: Long = System.currentTimeMillis()) {
    val random = SeededRandom(seed)
    val player = Player()
    val generator = ChunkGenerator(random)

    val platforms = mutableListOf<Platform>()
    val enemies = mutableListOf<Enemy>()
    val collectibles = mutableListOf<Collectible>()
    val rainbows = mutableListOf<Rainbow>()

    var cameraY = 0f
    var scrollSpeed = Constants.BASE_SCROLL_SPEED
    var score = 0
    var maxHeight = 0f
    var currentLevel = 0
    var platformsClimbed = 0

    // Biome management
    private var biomeOrder = Biome.ALL.shuffled(java.util.Random(seed))
    private var biomeIndex = 0
    var currentBiome = biomeOrder[0]
        private set

    // Chunk tracking
    private var highestGeneratedY = 0f

    fun init() {
        // Generate initial platforms — a safe starting area + first chunks
        generateStartingArea()
        generateChunksUpTo(cameraY + Constants.VIRTUAL_HEIGHT * 2)
    }

    private fun generateStartingArea() {
        // Ground platform
        val ground = Platform()
        ground.activate(0f, 0f, Constants.VIRTUAL_WIDTH, PlatformType.STATIC)
        platforms.add(ground)

        // A few easy platforms to get started
        val easyPlatforms = listOf(
            PlatformDef(80f, 60f, 64f, PlatformType.STATIC),
            PlatformDef(30f, 120f, 56f, PlatformType.STATIC),
            PlatformDef(140f, 170f, 48f, PlatformType.STATIC),
            PlatformDef(60f, 230f, 56f, PlatformType.STATIC),
        )
        for (def in easyPlatforms) {
            val p = Platform()
            p.activate(def.x, def.y, def.width, def.type)
            platforms.add(p)
        }
        highestGeneratedY = 280f
    }

    private fun generateChunksUpTo(targetY: Float) {
        while (highestGeneratedY < targetY) {
            val chunk = generator.generate(highestGeneratedY)
            for (def in chunk.platforms) {
                val p = Platform()
                p.activate(def.x, def.y, def.width, def.type)
                if (def.type == PlatformType.MOVING) {
                    p.moveMinX = 0f
                    p.moveMaxX = Constants.VIRTUAL_WIDTH
                }
                platforms.add(p)
            }
            for (def in chunk.enemies) {
                val e = Enemy()
                val speed = 40f * Math.pow(1.0 + Constants.DIFFICULTY_INCREASE, currentLevel.toDouble()).toFloat()
                e.activate(def.x, def.y, def.type, speed)
                e.patrolMinX = (def.x - 40f).coerceAtLeast(0f)
                e.patrolMaxX = (def.x + 40f).coerceAtMost(Constants.VIRTUAL_WIDTH)
                enemies.add(e)
            }
            for (def in chunk.collectibles) {
                val c = Collectible()
                c.activate(def.x, def.y, def.type)
                collectibles.add(c)
            }
            highestGeneratedY += Constants.CHUNK_HEIGHT_PX
        }
    }

    fun update(delta: Float) {
        if (!player.isAlive) return

        // Update scroll speed based on level
        val speedMult = if (player.slowTimeTimer > 0) 0.5f else 1f
        scrollSpeed = Constants.BASE_SCROLL_SPEED *
            Math.pow(1.0 + Constants.DIFFICULTY_INCREASE, currentLevel.toDouble()).toFloat() *
            speedMult

        // Auto-scroll camera
        cameraY += scrollSpeed * delta

        // Keep camera at least at player height
        if (player.position.y - cameraY > Constants.VIRTUAL_HEIGHT * 0.6f) {
            cameraY = player.position.y - Constants.VIRTUAL_HEIGHT * 0.6f
        }

        // Update max height and level
        if (player.position.y > maxHeight) {
            maxHeight = player.position.y
        }
        val newLevel = (maxHeight / (Constants.PLATFORMS_PER_LEVEL * 40f)).toInt()
        if (newLevel > currentLevel) {
            currentLevel = newLevel
            generator.setLevel(currentLevel)
            updateBiome()
        }

        // Generate more chunks ahead
        generateChunksUpTo(cameraY + Constants.VIRTUAL_HEIGHT * 3)

        // Update entities
        player.update(delta)
        platforms.forEach { it.update(delta) }
        enemies.forEach { it.update(delta) }
        collectibles.forEach { it.update(delta) }
        rainbows.forEach { it.update(delta) }

        // Collision detection
        handlePlatformCollisions()
        handleEnemyCollisions()
        handleCollectiblePickups()
        handleRainbowEnemyCollisions()

        // Check death: fell below camera
        if (player.position.y < cameraY - Constants.PLAYER_HEIGHT) {
            player.isAlive = false
        }

        // Clean up off-screen entities
        cleanUp()
    }

    private fun handlePlatformCollisions() {
        player.isOnGround = false

        // Check regular platforms
        for (platform in platforms) {
            if (!platform.active) continue
            if (checkPlatformLanding(platform)) break
        }

        // Check rainbow platforms
        for (rainbow in rainbows) {
            if (!rainbow.active) continue
            if (player.velocity.y <= 0 &&
                player.bounds.overlaps(rainbow.bounds) &&
                player.position.y >= rainbow.position.y + rainbow.bounds.height * 0.5f
            ) {
                player.position.y = rainbow.position.y + rainbow.bounds.height
                player.velocity.y = 0f
                player.isOnGround = true
                break
            }
        }
    }

    private fun checkPlatformLanding(platform: Platform): Boolean {
        if (player.velocity.y > 0) return false // moving up, skip

        val playerBottom = player.position.y
        val platformTop = platform.position.y + 8f

        if (player.bounds.x + player.bounds.width > platform.bounds.x &&
            player.bounds.x < platform.bounds.x + platform.bounds.width &&
            playerBottom >= platform.position.y - 2f &&
            playerBottom <= platformTop + 4f &&
            player.velocity.y <= 0
        ) {
            player.position.y = platformTop
            player.velocity.y = 0f
            player.isOnGround = true
            platform.startCrumble()
            return true
        }
        return false
    }

    private fun handleEnemyCollisions() {
        for (enemy in enemies) {
            if (!enemy.active) continue
            if (player.bounds.overlaps(enemy.bounds)) {
                if (player.shieldActive) {
                    player.shieldActive = false
                    enemy.active = false
                    score += 25
                } else {
                    player.isAlive = false
                }
                return
            }
        }
    }

    private fun handleCollectiblePickups() {
        val magnetRange = if (player.magnetTimer > 0) Constants.TILE_SIZE * 3f else 0f

        for (collectible in collectibles) {
            if (!collectible.active) continue

            // Magnet attraction
            if (magnetRange > 0 && !collectible.isPowerUp()) {
                val dx = player.position.x - collectible.position.x
                val dy = player.position.y - collectible.position.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (dist < magnetRange && dist > 0) {
                    collectible.position.x += dx / dist * 200f * 0.016f
                    collectible.position.y += dy / dist * 200f * 0.016f
                }
            }

            if (player.bounds.overlaps(collectible.bounds)) {
                collectible.active = false
                score += collectible.scoreValue
                applyCollectible(collectible.type)
            }
        }
    }

    private fun applyCollectible(type: CollectibleType) {
        when (type) {
            CollectibleType.RAINBOW_BOOST -> {
                player.maxRainbowAmmo = 6
                player.rainbowAmmo = 6
                player.rainbowBoostTimer = 15f
            }
            CollectibleType.DOUBLE_JUMP -> {
                player.hasDoubleJump = true
                player.doubleJumpTimer = 20f
            }
            CollectibleType.SHIELD -> {
                player.shieldActive = true
            }
            CollectibleType.SLOW_TIME -> {
                player.slowTimeTimer = 10f
            }
            CollectibleType.MAGNET -> {
                player.magnetTimer = 15f
            }
            else -> {} // gems and stars are just score
        }
    }

    private fun handleRainbowEnemyCollisions() {
        for (rainbow in rainbows) {
            if (!rainbow.active) continue
            for (enemy in enemies) {
                if (!enemy.active) continue
                if (rainbow.bounds.overlaps(enemy.bounds)) {
                    enemy.active = false
                    score += 25
                }
            }
        }
    }

    fun shootRainbow(direction: Int) {
        if (!player.shootRainbow()) return
        val rainbow = Rainbow()
        rainbow.activate(player.position.x, player.position.y, direction)
        rainbows.add(rainbow)
    }

    private fun updateBiome() {
        biomeIndex = (biomeIndex + 1) % biomeOrder.size
        if (biomeIndex == 0) {
            biomeOrder = Biome.ALL.shuffled(java.util.Random(random.seed + currentLevel))
        }
        currentBiome = biomeOrder[biomeIndex]
    }

    private fun cleanUp() {
        val bottomThreshold = cameraY - Constants.CHUNK_HEIGHT_PX
        platforms.removeAll { !it.active || it.position.y < bottomThreshold }
        enemies.removeAll { !it.active || it.position.y < bottomThreshold }
        collectibles.removeAll { !it.active || it.position.y < bottomThreshold }
        rainbows.removeAll { !it.active }
    }

    fun reset(newSeed: Long = System.currentTimeMillis()) {
        platforms.clear()
        enemies.clear()
        collectibles.clear()
        rainbows.clear()
        player.reset()
        cameraY = 0f
        scrollSpeed = Constants.BASE_SCROLL_SPEED
        score = 0
        maxHeight = 0f
        currentLevel = 0
        highestGeneratedY = 0f
        biomeIndex = 0
        biomeOrder = Biome.ALL.shuffled(java.util.Random(newSeed))
        currentBiome = biomeOrder[0]
        generator.setLevel(0)
        init()
    }
}
