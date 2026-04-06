package com.darumahq.rainbowclimb.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.darumahq.rainbowclimb.util.Constants
import com.darumahq.rainbowclimb.world.Biome

class ParallaxBackground(private val sprites: SpriteManager) {

    fun render(batch: SpriteBatch, cameraY: Float, biome: Biome) {
        val bgTex = sprites.getBackgroundForBiome(biome.type)
        val region = TextureRegion(bgTex)

        // Draw full-screen background with slow parallax scroll
        // The background is 240x400, same as virtual resolution
        // Offset Y by a fraction of cameraY for parallax effect
        val parallaxOffset = (cameraY * 0.05f) % Constants.VIRTUAL_HEIGHT

        // Draw two copies for seamless vertical scrolling
        batch.setColor(Color.WHITE)
        batch.draw(region, 0f, -parallaxOffset, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT)
        batch.draw(region, 0f, Constants.VIRTUAL_HEIGHT - parallaxOffset, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT)
    }
}
