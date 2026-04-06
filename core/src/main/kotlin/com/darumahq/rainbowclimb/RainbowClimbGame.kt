package com.darumahq.rainbowclimb

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.darumahq.rainbowclimb.render.SpriteManager
import com.darumahq.rainbowclimb.screen.MenuScreen

class RainbowClimbGame : Game() {
    lateinit var batch: SpriteBatch
        private set
    lateinit var sprites: SpriteManager
        private set

    override fun create() {
        batch = SpriteBatch()
        sprites = SpriteManager()
        sprites.load()
        setScreen(MenuScreen(this))
    }

    override fun dispose() {
        batch.dispose()
        sprites.dispose()
        screen?.dispose()
    }
}
