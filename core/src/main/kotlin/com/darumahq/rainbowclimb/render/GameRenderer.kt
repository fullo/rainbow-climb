package com.darumahq.rainbowclimb.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.darumahq.rainbowclimb.entity.EnemyType
import com.darumahq.rainbowclimb.util.Constants
import com.darumahq.rainbowclimb.world.World

class GameRenderer(private val batch: SpriteBatch) {
    val camera = OrthographicCamera()
    val viewport = FitViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera)
    private val shapeRenderer = ShapeRenderer()
    private val parallax = ParallaxBackground()
    private val font = BitmapFont() // default font, replace with pixel font later

    private val rainbowColors = listOf(
        Color.RED, Color.ORANGE, Color.YELLOW,
        Color.GREEN, Color.CYAN, Color.BLUE, Color.VIOLET
    )

    init {
        camera.position.set(Constants.VIRTUAL_WIDTH / 2f, Constants.VIRTUAL_HEIGHT / 2f, 0f)
        font.data.setScale(0.5f)
    }

    fun render(world: World) {
        // Update camera to follow world scroll
        camera.position.y = world.cameraY + Constants.VIRTUAL_HEIGHT / 2f
        camera.update()

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Background (uses its own projection)
        parallax.render(world.cameraY, world.currentBiome)

        // Game entities
        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Platforms
        for (platform in world.platforms) {
            if (!platform.active) continue
            val alpha = if (platform.isCrumbling) {
                1f - (platform.crumbleTimer / platform.crumbleDuration)
            } else 1f
            val color = world.currentBiome.platformColor
            shapeRenderer.setColor(color.r, color.g, color.b, alpha)
            shapeRenderer.rect(platform.position.x, platform.position.y, platform.width, 8f)
        }

        // Rainbows
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

        // Enemies (color-coded by type for playtesting)
        for (enemy in world.enemies) {
            if (!enemy.active) continue
            shapeRenderer.color = when (enemy.type) {
                EnemyType.SHOOTER -> Color.DARK_GRAY
                EnemyType.BOMBER -> Color(0.5f, 0f, 0.5f, 1f)
                EnemyType.CHASER -> Color.SCARLET
                else -> Color.RED
            }
            val w = if (enemy.type == EnemyType.BOMBER) Constants.BOMBER_WIDTH else Constants.ENEMY_SIZE
            val h = if (enemy.type == EnemyType.BOMBER) Constants.BOMBER_HEIGHT else Constants.ENEMY_SIZE
            shapeRenderer.rect(enemy.position.x, enemy.position.y, w, h)
        }

        // Projectiles
        for (proj in world.projectiles) {
            if (!proj.active) continue
            shapeRenderer.color = Color.ORANGE
            shapeRenderer.rect(proj.position.x, proj.position.y,
                Constants.PROJECTILE_SIZE, Constants.PROJECTILE_SIZE)
        }

        // Collectibles
        for (collectible in world.collectibles) {
            if (!collectible.active) continue
            shapeRenderer.color = if (collectible.isPowerUp()) Color.MAGENTA else Color.YELLOW
            val size = 8f
            val bob = kotlin.math.sin((collectible.animTimer * 4f).toDouble()).toFloat() * 2f
            shapeRenderer.rect(collectible.position.x, collectible.position.y + bob, size, size)
        }

        // Player
        if (world.player.isAlive) {
            shapeRenderer.color = if (world.player.shieldActive) Color.CYAN else Color.WHITE
            shapeRenderer.rect(
                world.player.position.x, world.player.position.y,
                Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT
            )
        }

        shapeRenderer.end()

        // HUD (fixed to screen, not world)
        val hudCamera = OrthographicCamera()
        hudCamera.setToOrtho(false, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT)
        hudCamera.update()

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.color = Color.WHITE

        // Score
        font.draw(batch, "Score: ${world.score}", 4f, Constants.VIRTUAL_HEIGHT - 4f)

        // Height / Level
        font.draw(batch, "Lv ${world.currentLevel + 1}", Constants.VIRTUAL_WIDTH - 50f, Constants.VIRTUAL_HEIGHT - 4f)

        // Rainbow ammo
        val ammoText = "R:" + "o".repeat(world.player.rainbowAmmo) +
            ".".repeat(world.player.maxRainbowAmmo - world.player.rainbowAmmo)
        font.draw(batch, ammoText, 4f, Constants.VIRTUAL_HEIGHT - 16f)

        // Biome name
        font.draw(batch, world.currentBiome.name, Constants.VIRTUAL_WIDTH / 2f - 30f, Constants.VIRTUAL_HEIGHT - 4f)

        batch.end()
    }

    fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        camera.position.set(Constants.VIRTUAL_WIDTH / 2f, Constants.VIRTUAL_HEIGHT / 2f, 0f)
    }

    fun dispose() {
        shapeRenderer.dispose()
        parallax.dispose()
        font.dispose()
    }
}
