package com.darumahq.rainbowclimb.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.darumahq.rainbowclimb.util.Constants
import com.darumahq.rainbowclimb.world.Biome

class ParallaxBackground(private val sprites: SpriteManager) {

    fun render(batch: SpriteBatch, cameraY: Float, biome: Biome) {
        val bgTex = sprites.getBackgroundForBiome(biome.type)
        val texW = bgTex.width.toFloat()
        val texH = bgTex.height.toFloat()

        // Lighten biome color so background is never too dark
        // Mix 60% biome color + 40% white
        val bg = biome.bgColor
        val tintR = (bg.r * 0.6f + 0.4f).coerceAtMost(1f)
        val tintG = (bg.g * 0.6f + 0.4f).coerceAtMost(1f)
        val tintB = (bg.b * 0.6f + 0.4f).coerceAtMost(1f)
        batch.setColor(tintR, tintG, tintB, 1f)

        // Slow parallax layer: tile the background across the viewport
        val scrollOffset = (cameraY * 0.1f) % texH
        val tilesX = (Constants.VIRTUAL_WIDTH / texW).toInt() + 2
        val tilesY = (Constants.VIRTUAL_HEIGHT / texH).toInt() + 3

        val region = TextureRegion(bgTex)

        for (tx in 0 until tilesX) {
            for (ty in -1 until tilesY) {
                val drawX = tx * texW
                val drawY = ty * texH - scrollOffset
                batch.draw(region, drawX, drawY, texW, texH)
            }
        }

        // Reset tint
        batch.setColor(Color.WHITE)
    }
}
