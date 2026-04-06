package com.darumahq.rainbowclimb.render

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.darumahq.rainbowclimb.util.Constants
import com.darumahq.rainbowclimb.world.Biome

class ParallaxBackground(private val sprites: SpriteManager) {

    // Scroll speeds for each layer (fraction of camera movement)
    private val scrollSpeeds = floatArrayOf(0.03f, 0.12f, 0.25f)

    fun render(batch: SpriteBatch, cameraY: Float, biome: Biome) {
        val layers = sprites.getBackgroundLayers(biome.type)

        batch.setColor(Color.WHITE)

        for (i in layers.indices) {
            val tex = layers[i]
            val region = TextureRegion(tex)
            val speed = if (i < scrollSpeeds.size) scrollSpeeds[i] else 0.3f

            // Scroll offset wrapping within one screen height
            val offset = (cameraY * speed) % Constants.VIRTUAL_HEIGHT

            // Draw two copies for seamless vertical loop
            batch.draw(region, 0f, -offset, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT)
            batch.draw(region, 0f, Constants.VIRTUAL_HEIGHT - offset, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT)
        }
    }
}
