import org.example.e3fxgaming.gfnsteamcomparator.Fetcher
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GameComparisonTest {
    private val fetcher = Fetcher()

    @Test
    fun gfnSteamGamesDataFetcherTest() {
        val result = fetcher.gfnSteamGamesDataFetcher()
        Assertions.assertTrue(
            result != null && result.isNotEmpty(),
            "Steam games were not fetched from the GFN website"
        )
    }

    @Test
    fun steamDbGamesDataFetcherTest() {
        val result = fetcher.steamDbGamesDataFetcher()
        Assertions.assertTrue(
            result != null && result.isNotEmpty(),
            "Steam games were not fetched from the SteamDB webesite"
        )
    }
}
