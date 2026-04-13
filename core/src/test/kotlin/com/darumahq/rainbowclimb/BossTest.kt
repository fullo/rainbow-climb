package com.darumahq.rainbowclimb

import com.darumahq.rainbowclimb.entity.Boss
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BossTest {

    @Test
    fun `activate sets correct initial state`() {
        val b = Boss()
        b.activate(100f, 500f, 0f, 1)

        assertTrue(b.active)
        assertEquals(Boss.State.IDLE, b.state)
        assertEquals(6, b.hp) // 5 + difficulty(1)
        assertEquals(6, b.maxHp)
    }

    @Test
    fun `takeDamage decrements HP and enters HIT state`() {
        val b = Boss()
        b.activate(100f, 500f, 0f, 0)
        val initialHp = b.hp

        b.takeDamage()
        assertEquals(initialHp - 1, b.hp)
        assertEquals(Boss.State.HIT, b.state)
    }

    @Test
    fun `takeDamage ignored when already in HIT state`() {
        val b = Boss()
        b.activate(100f, 500f, 0f, 0)
        b.takeDamage() // hp = 4, state = HIT
        val hpAfterFirst = b.hp

        b.takeDamage() // should be ignored
        assertEquals(hpAfterFirst, b.hp) // unchanged
    }

    @Test
    fun `takeDamage ignored when DEAD`() {
        val b = Boss()
        b.activate(100f, 500f, 0f, 0)
        b.hp = 1
        b.takeDamage() // hp = 0, state = HIT

        // Simulate transition to DEAD
        b.state = Boss.State.DEAD
        b.takeDamage() // should be ignored
        assertEquals(0, b.hp)
    }

    @Test
    fun `boss stays on screen horizontally`() {
        val b = Boss()
        b.activate(0f, 500f, 0f, 0)
        b.state = Boss.State.CHARGE

        // Simulate running off left edge
        for (i in 0 until 200) {
            b.update(0.016f, -1000f, 500f)
        }
        assertTrue(b.position.x >= 0f, "Boss should not go off left edge")
    }

    @Test
    fun `animState returns correct values`() {
        val b = Boss()
        b.activate(100f, 500f, 0f, 0)

        assertEquals("idle", b.animState())
        b.state = Boss.State.CHARGE
        assertEquals("run", b.animState())
        b.state = Boss.State.JUMP
        assertEquals("jump", b.animState())
        b.state = Boss.State.ATTACK
        assertEquals("attack", b.animState())
        b.state = Boss.State.HIT
        assertEquals("hit", b.animState())
        b.state = Boss.State.DEAD
        assertEquals("dead", b.animState())
    }

    @Test
    fun `reset clears active state`() {
        val b = Boss()
        b.activate(100f, 500f, 0f, 2)
        b.reset()
        assertFalse(b.active)
    }
}
