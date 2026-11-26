package earth.terrarium.cloche.target.compilation

internal sealed interface GameShards<out T> {
    data class Joined<T>(
        val value: T,
    ) : GameShards<T>

    data class Split<T>(
        val common: T,
        val client: T,
    ) : GameShards<T>
}

internal enum class GamePart(
    val key: String,
) {
    JOINED("joined"),
    COMMON("common"),
    CLIENT("client"),
}

internal fun <T> selectGame(
    games: GameShards<T>,
    part: GamePart,
): T =
    when (games) {
        is GameShards.Joined -> {
            require(part == GamePart.JOINED) { "Joined Minecraft cannot select $part" }
            games.value
        }

        is GameShards.Split -> {
            when (part) {
                GamePart.COMMON -> games.common
                GamePart.CLIENT -> games.client
                GamePart.JOINED -> throw IllegalArgumentException("Split Minecraft cannot select JOINED")
            }
        }
    }
