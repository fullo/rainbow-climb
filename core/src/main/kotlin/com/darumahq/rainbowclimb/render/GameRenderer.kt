package com.darumahq.rainbowclimb.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.darumahq.rainbowclimb.entity.EnemyType
import com.darumahq.rainbowclimb.util.Constants
import com.darumahq.rainbowclimb.world.World
import com.darumahq.rainbowclimb.world.World.EventType

class GameRenderer(private val batch: SpriteBatch, private val sprites: SpriteManager) {
    val camera = OrthographicCamera()
    val viewport = FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera)
    private val shapeRenderer = ShapeRenderer()
    private val parallax = ParallaxBackground(sprites)
    private val particles = ParticleSystem()
    private val hudCamera = OrthographicCamera()

    private val rainbowColors = listOf(
        Color.RED, Color.ORANGE, Color.YELLOW,
        Color.GREEN, Color.CYAN, Color.BLUE, Color.VIOLET
    )

    init {
        camera.position.set(Constants.VIRTUAL_WIDTH / 2f, Constants.VIRTUAL_HEIGHT / 2f, 0f)
        hudCamera.setToOrtho(false, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT)
    }

    fun render(world: World) {
        // Update camera to follow world scroll
        camera.position.y = world.cameraY + Constants.VIRTUAL_HEIGHT / 2f
        camera.update()

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Update particles
        val delta = Gdx.graphics.deltaTime.coerceAtMost(0.033f)
        particles.update(delta)

        // ── Background (uses HUD camera for full-screen fill) ──
        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        parallax.render(batch, world.cameraY, world.currentBiome)
        batch.end()

        // ── Sprite entities (game camera) ──
        batch.projectionMatrix = camera.combined
        batch.begin()
        Gdx.gl.glEnable(GL20.GL_BLEND)

        renderPlatforms(world)
        renderCollectibles(world)
        renderEnemies(world)
        renderPlayer(world)

        batch.end()

        // ── Rainbows + Projectiles (ShapeRenderer, game camera) ──
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        renderRainbows(world)
        renderProjectiles(world)

        // Process game events into particles
        processEvents(world)
        particles.render(shapeRenderer)

        shapeRenderer.end()

        // ── HUD (screen-space) ──
        hudCamera.update()
        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        renderHUD(world)
        batch.end()
    }

    // ── Platforms ─────────────────────────────────────────────────

    private fun renderPlatforms(world: World) {
        for (platform in world.platforms) {
            if (!platform.active) continue

            val alpha = if (platform.isCrumbling) {
                1f - (platform.crumbleTimer / platform.crumbleDuration)
            } else 1f

            batch.setColor(1f, 1f, 1f, alpha)

            val tex = sprites.platformBrown
            // Tile the platform texture across the platform width
            var drawX = platform.position.x
            val remaining = platform.width
            val segW = tex.regionWidth.toFloat()  // 32
            val segH = tex.regionHeight.toFloat()  // 8
            var drawn = 0f
            while (drawn < remaining) {
                val w = minOf(segW, remaining - drawn)
                if (w < segW) {
                    // Partial segment at the end
                    val partial = TextureRegion(tex.texture,
                        tex.regionX, tex.regionY, w.toInt(), tex.regionHeight)
                    batch.draw(partial, drawX, platform.position.y, w, segH)
                } else {
                    batch.draw(tex, drawX, platform.position.y, segW, segH)
                }
                drawX += w
                drawn += w
            }
        }
        batch.setColor(Color.WHITE)
    }

    // ── Collectibles ─────────────────────────────────────────────

    private fun renderCollectibles(world: World) {
        for (collectible in world.collectibles) {
            if (!collectible.active) continue

            val anim = if (collectible.isPowerUp()) {
                sprites.getRandomFruit(collectible.position.x.toInt())
            } else {
                sprites.getGemAnimForBiome(world.currentBiome.type)
            }
            val frame = anim.getKeyFrame(collectible.animTimer, true)
            val bob = kotlin.math.sin((collectible.animTimer * 4f).toDouble()).toFloat() * 2f
            batch.draw(frame, collectible.position.x, collectible.position.y + bob, 8f, 8f)
        }
    }

    // ── Enemies ──────────────────────────────────────────────────

    private fun renderEnemies(world: World) {
        for (enemy in world.enemies) {
            if (!enemy.active) continue

            val anim = sprites.getEnemyAnim(enemy.type, enemy.animState())
            val frame = anim.getKeyFrame(enemy.stateTime, true)

            val spriteW = frame.regionWidth.toFloat()
            val spriteH = frame.regionHeight.toFloat()

            // Collision box width for centering
            val collW = if (enemy.type == EnemyType.BOMBER) Constants.BOMBER_WIDTH else Constants.ENEMY_SIZE

            // Align sprite: centered X, bottom-aligned Y (feet on platform)
            val drawX = enemy.position.x + (collW - spriteW) / 2f
            val drawY = enemy.position.y  // bottom-aligned

            if (enemy.facingRight) {
                batch.draw(frame, drawX, drawY, spriteW, spriteH)
            } else {
                // Flip horizontally: draw at x+width with negative width
                batch.draw(frame, drawX + spriteW, drawY, -spriteW, spriteH)
            }
        }
    }

    // ── Player ───────────────────────────────────────────────────

    private fun renderPlayer(world: World) {
        val player = world.player
        if (!player.isAlive && player.stateTime > 1.5f) return // hide after death anim

        val anim = sprites.getPlayerAnim(player.animState())
        val looping = player.animState() == "idle" || player.animState() == "run"
        val frame = anim.getKeyFrame(player.stateTime, looping)

        val spriteW = frame.regionWidth.toFloat()
        val spriteH = frame.regionHeight.toFloat()

        // Align sprite to bottom of collision box (feet on platform)
        val drawX = player.position.x + (Constants.PLAYER_WIDTH - spriteW) / 2f
        val drawY = player.position.y  // bottom-aligned, not centered

        // Death fade-out effect
        if (!player.isAlive) {
            val fadeAlpha = (1f - (player.stateTime / 1.5f)).coerceIn(0f, 1f)
            batch.setColor(1f, 0.3f, 0.3f, fadeAlpha) // red tint + fade
        }
        // Shield tint
        else if (player.shieldActive) {
            batch.setColor(0.5f, 1f, 1f, 1f)
        }

        if (player.facing >= 0) {
            batch.draw(frame, drawX, drawY, spriteW, spriteH)
        } else {
            batch.draw(frame, drawX + spriteW, drawY, -spriteW, spriteH)
        }

        batch.setColor(Color.WHITE)
    }

    // ── Rainbows (ShapeRenderer) ─────────────────────────────────

    private fun renderRainbows(world: World) {
        for (rainbow in world.rainbows) {
            if (!rainbow.active) continue
            val alpha = (rainbow.timer / Constants.RAINBOW_DURATION).coerceIn(0f, 1f)
            val colorIndex = ((rainbow.timer * 5f).toInt()) % rainbowColors.size
            val c = rainbowColors[colorIndex]
            shapeRenderer.setColor(c.r, c.g, c.b, alpha)
            shapeRenderer.rect(
                rainbow.bounds.x, rainbow.bounds.y,
                rainbow.bounds.width, rainbow.bounds.height
            )
        }
    }

    // ── Projectiles (ShapeRenderer) ──────────────────────────────

    private fun renderProjectiles(world: World) {
        for (proj in world.projectiles) {
            if (!proj.active) continue
            shapeRenderer.color = Color.ORANGE
            shapeRenderer.rect(proj.position.x, proj.position.y,
                Constants.PROJECTILE_SIZE, Constants.PROJECTILE_SIZE)
        }
    }

    // ── HUD ──────────────────────────────────────────────────────

    private fun renderHUD(world: World) {
        val font = sprites.pixelFont
        font.color = Color.WHITE

        // Score + Gems
        font.draw(batch, "Score:${world.score}", 4f, Constants.VIRTUAL_HEIGHT - 4f)
        font.color = Color.CYAN
        font.draw(batch, "G:${world.gemsCollected}", 4f, Constants.VIRTUAL_HEIGHT - 16f)

        // Lives
        font.color = Color.RED
        val livesText = "L:" + "♥".repeat(world.player.lives.coerceAtLeast(0))
        font.draw(batch, livesText, 4f, Constants.VIRTUAL_HEIGHT - 28f)

        // Level
        font.color = Color.WHITE
        font.draw(batch, "Lv ${world.currentLevel + 1}",
            Constants.VIRTUAL_WIDTH - 50f, Constants.VIRTUAL_HEIGHT - 4f)

        // Rainbow ammo
        val ammoText = "R:" + "o".repeat(world.player.rainbowAmmo) +
            ".".repeat(world.player.maxRainbowAmmo - world.player.rainbowAmmo)
        font.draw(batch, ammoText, Constants.VIRTUAL_WIDTH - 60f, Constants.VIRTUAL_HEIGHT - 16f)

        // Biome name
        font.draw(batch, world.currentBiome.name,
            Constants.VIRTUAL_WIDTH / 2f - 30f, Constants.VIRTUAL_HEIGHT - 4f)
    }

    private fun processEvents(world: World) {
        for (event in world.events) {
            when (event.type) {
                EventType.COLLECT -> particles.sparkle(event.x, event.y)
                EventType.ENEMY_DEATH -> particles.enemyPoof(event.x, event.y)
                EventType.PLAYER_DEATH -> particles.deathExplosion(event.x, event.y)
                EventType.RAINBOW_SHOOT -> particles.rainbowBurst(event.x, event.y, 6)
            }
        }
    }

    fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        camera.position.set(Constants.VIRTUAL_WIDTH / 2f, Constants.VIRTUAL_HEIGHT / 2f, 0f)
    }

    fun dispose() {
        shapeRenderer.dispose()
    }
}
