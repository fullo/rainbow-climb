package com.darumahq.rainbowclimb.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.darumahq.rainbowclimb.RainbowClimbGame
import com.darumahq.rainbowclimb.audio.MusicEngine
import com.darumahq.rainbowclimb.audio.SfxManager
import com.darumahq.rainbowclimb.input.TouchInputHandler
import com.darumahq.rainbowclimb.render.GameRenderer
import com.darumahq.rainbowclimb.world.World

class GameScreen(private val game: RainbowClimbGame) : ScreenAdapter() {
    private val world = World()
    private val renderer = GameRenderer(game.batch, game.sprites, game.selectedCharacter)
    private val inputHandler = TouchInputHandler()
    private val musicEngine = MusicEngine()
    private val sfx = SfxManager()

    private var lastBiomeIndex = -1

    override fun show() {
        world.init()
        Gdx.input.inputProcessor = inputHandler
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        musicEngine.updateForBiome(world.currentBiome)
        musicEngine.start()
    }

    override fun render(delta: Float) {
        val dt = delta.coerceAtMost(0.033f) // cap at ~30fps minimum

        // Handle input
        handleInput()

        // Update world
        world.update(dt)

        // Check biome change
        if (world.currentLevel != lastBiomeIndex) {
            lastBiomeIndex = world.currentLevel
            musicEngine.updateForBiome(world.currentBiome)
        }

        // Render
        renderer.render(world)

        // Check game over
        if (!world.player.isAlive) {
            sfx.playDeath()
            musicEngine.stop()
            game.addGems(world.gemsCollected)
            game.setScreen(GameOverScreen(game, world.score, world.maxHeight.toInt(), world.currentLevel, world.currentSeed))
        }
    }

    private fun handleInput() {
        // Keyboard input (for desktop testing)
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            world.player.moveLeft()
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            world.player.moveRight()
        } else if (inputHandler.moveDirection != 0) {
            if (inputHandler.moveDirection < 0) world.player.moveLeft()
            else world.player.moveRight()
        } else {
            world.player.stopMoving()
        }

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.UP) ||
            Gdx.input.isKeyJustPressed(Input.Keys.W) ||
            inputHandler.consumeJump()
        ) {
            world.player.jump()
            sfx.playJump()
        }

        // Rainbow
        val rainbowDir = when {
            Gdx.input.isKeyJustPressed(Input.Keys.Z) -> -1
            Gdx.input.isKeyJustPressed(Input.Keys.X) -> 0
            Gdx.input.isKeyJustPressed(Input.Keys.C) -> 1
            else -> inputHandler.consumeRainbow()
        }
        if (rainbowDir != -2) {
            world.shootRainbow(rainbowDir)
            sfx.playRainbow()
        }

        // Android back button → go to menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
        ) {
            musicEngine.stop()
            game.setScreen(MenuScreen(game))
        }
    }

    override fun resize(width: Int, height: Int) {
        renderer.resize(width, height)
    }

    override fun dispose() {
        renderer.dispose()
        musicEngine.dispose()
    }
}
