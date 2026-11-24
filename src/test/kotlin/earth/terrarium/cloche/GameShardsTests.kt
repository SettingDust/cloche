package earth.terrarium.cloche

import earth.terrarium.cloche.target.compilation.GamePart
import earth.terrarium.cloche.target.compilation.GameShards
import earth.terrarium.cloche.target.compilation.selectGame
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GameShardsTests {
    @Test
    fun typedShapesKeepValuesAndRejectIncompatiblePrimary() {
        val common = Any()
        val client = Any()
        val split = GameShards.Split(common, client)
        assertSame(common, selectGame(split, GamePart.COMMON))
        assertSame(client, selectGame(split, GamePart.CLIENT))
        val joined = GameShards.Joined(common)
        assertSame(common, selectGame(joined, GamePart.JOINED))
        assertThrows(IllegalArgumentException::class.java) { selectGame(split, GamePart.JOINED) }
        for (part in listOf(GamePart.COMMON, GamePart.CLIENT)) {
            assertThrows(IllegalArgumentException::class.java) { selectGame(joined, part) }
        }
        assertEquals(GameShards.Split(common, client), split)
        assertEquals(listOf("joined", "common", "client"), GamePart.entries.map { it.key })
    }
}
