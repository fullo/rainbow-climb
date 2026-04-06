package com.darumahq.rainbowclimb

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.darumahq.rainbowclimb.screen.MenuScreen

class RainbowClimbGame : Game() {
    lateinit var batch: SpriteBatch
        private set

    override fun create() {
        batch = SpriteBatch()
        setScreen(MenuScreen(this))
    }

    override fun dispose() {
        batch.dispose()
        screen?.dispose()
    }
}
