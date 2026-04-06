package com.darumahq.rainbowclimb.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.darumahq.rainbowclimb.RainbowClimbGame
import com.darumahq.rainbowclimb.util.Constants

class GameOverScreen(
    private val game: RainbowClimbGame,
    private val score: Int,
    private val height: Int,
    private val level: Int
) : ScreenAdapter() {
    private val camera = OrthographicCamera()
    private val font = BitmapFont()
    private var animTimer = 0f
    private val highScore: Int
    private val isNewBest: Boolean

    init {
        camera.setToOrtho(false, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT)
        font.data.setScale(0.5f)

        // Load and update high score
        val prefs: Preferences = Gdx.app.getPreferences("rainbow-climb")
        val previousBest = prefs.getInteger("highScore", 0)
        isNewBest = score > previousBest
        highScore = maxOf(score, previousBest)
        if (isNewBest) {
            prefs.putInteger("highScore", score)
            prefs.flush()
        }
    }

    override fun render(delta: Float) {
        animTimer += delta

        Gdx.gl.glClearColor(0.1f, 0.02f, 0.02f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        game.batch.projectionMatrix = camera.combined
        game.batch.begin()

        font.color = Color.RED
        drawCentered("GAME OVER", Constants.VIRTUAL_HEIGHT - 80f)

        font.color = Color.WHITE
        drawCentered("Score: $score", Constants.VIRTUAL_HEIGHT - 130f)
        drawCentered("Height: $height", Constants.VIRTUAL_HEIGHT - 150f)
        drawCentered("Level: ${level + 1}", Constants.VIRTUAL_HEIGHT - 170f)

        font.color = if (isNewBest) Color.GOLD else Color.GRAY
        drawCentered(
            if (isNewBest) "NEW BEST: $highScore!" else "Best: $highScore",
            Constants.VIRTUAL_HEIGHT - 200f
        )

        // Blink restart prompt
        if ((animTimer * 2f).toInt() % 2 == 0) {
            font.color = Color.YELLOW
            drawCentered("TAP TO RETRY", Constants.VIRTUAL_HEIGHT / 2f - 40f)
        }

        font.color = Color.SKY
        drawCentered("Buy Me a Coffee :)", 50f)

        game.batch.end()

        // Restart or back to menu
        if (Gdx.input.justTouched() ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
        ) {
            game.setScreen(GameScreen(game))
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(MenuScreen(game))
        }
    }

    private fun drawCentered(text: String, y: Float) {
        val layout = GlyphLayout(font, text)
        font.draw(game.batch, text, (Constants.VIRTUAL_WIDTH - layout.width) / 2f, y)
    }

    override fun resize(width: Int, height: Int) {
        camera.setToOrtho(false, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT)
    }

    override fun dispose() {
        font.dispose()
    }
}
