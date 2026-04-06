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

        // Tint batch with biome background color for atmosphere
        val bg = biome.bgColor
        batch.setColor(bg.r, bg.g, bg.b, 1f)

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
