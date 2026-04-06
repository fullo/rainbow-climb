package com.darumahq.rainbowclimb.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.darumahq.rainbowclimb.RainbowClimbGame
import com.darumahq.rainbowclimb.util.Constants

class MenuScreen(private val game: RainbowClimbGame) : ScreenAdapter() {
    private val camera = OrthographicCamera()
    private val font get() = game.sprites.pixelFont
    private val shapeRenderer = ShapeRenderer()
    private var animTimer = 0f

    private val rainbowColors = listOf(
        Color.RED, Color.ORANGE, Color.YELLOW,
        Color.GREEN, Color.CYAN, Color.BLUE, Color.VIOLET
    )

    init {
        camera.setToOrtho(false, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT)
    }

    override fun render(delta: Float) {
        animTimer += delta

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()

        // Animated rainbow bar
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (i in rainbowColors.indices) {
            val y = 240f + i * 4f
            shapeRenderer.color = rainbowColors[i]
            shapeRenderer.rect(0f, y, Constants.VIRTUAL_WIDTH, 3f)
        }
        shapeRenderer.end()

        // Text
        game.batch.projectionMatrix = camera.combined
        game.batch.begin()

        font.color = Color.WHITE
        drawCentered("RAINBOW CLIMB", Constants.VIRTUAL_HEIGHT - 100f)

        // Blink "tap to play"
        if ((animTimer * 2f).toInt() % 2 == 0) {
            font.color = Color.YELLOW
            drawCentered("TAP TO PLAY", Constants.VIRTUAL_HEIGHT / 2f)
        }

        font.color = Color.GRAY
        drawCentered("Arrow keys / Touch to move", 100f)
        drawCentered("Space / 2nd finger to jump", 84f)
        drawCentered("Z/X/C or Swipe for rainbow", 68f)

        font.color = Color.SKY
        drawCentered("[S] Settings", 40f)

        game.batch.end()

        // Start game on tap or key
        if (Gdx.input.justTouched() ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
        ) {
            game.setScreen(GameScreen(game))
        }

        // Settings
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            game.setScreen(SettingsScreen(game))
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
        shapeRenderer.dispose()
    }
}
