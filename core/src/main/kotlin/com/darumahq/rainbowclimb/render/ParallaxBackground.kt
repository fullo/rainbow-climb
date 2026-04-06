package com.darumahq.rainbowclimb.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.darumahq.rainbowclimb.util.Constants
import com.darumahq.rainbowclimb.world.Biome

class ParallaxBackground {
    private val shapeRenderer = ShapeRenderer()

    fun render(cameraY: Float, biome: Biome) {
        val bg = biome.bgColor
        val accent = biome.accentColor

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Background gradient
        shapeRenderer.rect(
            0f, 0f,
            Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT,
            bg, bg,
            Color(bg.r * 0.7f, bg.g * 0.7f, bg.b * 0.7f, 1f),
            Color(bg.r * 0.7f, bg.g * 0.7f, bg.b * 0.7f, 1f)
        )

        // Parallax dots/stars (slow layer)
        val slowOffset = cameraY * 0.1f
        shapeRenderer.color = Color(accent.r, accent.g, accent.b, 0.3f)
        for (i in 0 until 12) {
            val x = ((i * 47 + 13) % Constants.VIRTUAL_WIDTH.toInt()).toFloat()
            val y = ((i * 67 + slowOffset.toInt()) % Constants.VIRTUAL_HEIGHT.toInt()).toFloat()
            shapeRenderer.circle(x, y, 1.5f)
        }

        // Medium parallax layer
        val medOffset = cameraY * 0.3f
        shapeRenderer.color = Color(accent.r, accent.g, accent.b, 0.15f)
        for (i in 0 until 8) {
            val x = ((i * 37 + 7) % Constants.VIRTUAL_WIDTH.toInt()).toFloat()
            val y = ((i * 53 + medOffset.toInt()) % Constants.VIRTUAL_HEIGHT.toInt()).toFloat()
            shapeRenderer.rect(x, y, 3f, 3f)
        }

        shapeRenderer.end()
    }

    fun dispose() {
        shapeRenderer.dispose()
    }
}
