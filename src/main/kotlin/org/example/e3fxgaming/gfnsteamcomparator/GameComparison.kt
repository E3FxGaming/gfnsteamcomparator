package org.example.e3fxgaming.gfnsteamcomparator

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URL


fun main() {
    val fetcher = Fetcher()

    val steamDbGames = fetcher.steamDbGamesDataFetcher()?.sortedBy { it.title }
    val gfnGames = fetcher.gfnSteamGamesDataFetcher()?.sortedBy { it.title }

    if (steamDbGames != null && gfnGames != null) {
        compare(steamDbGames, gfnGames)
    } else {
        throw IllegalStateException("Games fetching failed")
    }


}

fun compare(steamDbGames: List<SteamGame>, gfnGames: List<SteamGame>) {

    val unmappedSteamDbGames = mutableListOf<SteamGame>()

    steamDbGames.forEach { steamDbGame ->
        val result = gfnGames.find { it.steamUrl == steamDbGame.steamUrl }
        if (result == null) {
            unmappedSteamDbGames.add(steamDbGame)
        }
    }

    val unmappedGfnGames = mutableListOf<SteamGame>()

    gfnGames.forEach { gfnGame ->
        val result = steamDbGames.find { it.steamUrl == gfnGame.steamUrl }
        if (result == null) {
            unmappedGfnGames.add(gfnGame)
        }
    }


    println("Opted in according to SteamDB, but not explicitly available on GFN:\n")
    unmappedSteamDbGames.forEach { println(it) }
    println("Total ${unmappedSteamDbGames.size} out of ${steamDbGames.size}")

    println("\nAvailable on GFN (no opt-in according to SteamDB):\n")
    unmappedGfnGames.forEach { println(it) }
    println("Total ${unmappedGfnGames.size} out of ${gfnGames.size}")


}

class Fetcher {
    private val gfnURL = URL("https://static.nvidiagrid.net/supported-public-game-list/locales/gfnpc-en-US.json")
    private val steamdbURL = URL("https://steamdb.info/search/?a=app&q=&type=1&category=46")

    private val client = OkHttpClient.Builder().retryOnConnectionFailure(false).build()


    fun gfnSteamGamesDataFetcher(): List<SteamGame>? {

        val moshi = Moshi.Builder().build()
        val gfnGameListType = Types.newParameterizedType(List::class.java, SteamGame::class.java)
        val gfnGameListAdapter: JsonAdapter<List<SteamGame>> = moshi.adapter(gfnGameListType)

        val request = Request.Builder().get().url(gfnURL).build()

        val response = client.newCall(request).execute()

        return response.use {
            val responseBody = response.body

            if (responseBody != null) {
                gfnGameListAdapter.fromJson(responseBody.source())?.filter { it.steamUrl.isNotEmpty() }
            } else {
                null
            }
        }
    }

    fun steamDbGamesDataFetcher(): List<SteamGame>? {
        val steamURLPrefix = "https://store.steampowered.com/app/"

        val headers = Headers.Builder().add(
            "User-Agent",
            "Mozilla/5.0 (X11; Linux x86_64; rv:81.0) Gecko/20100101 Firefox/81.0"
        ).build()

        val request = Request.Builder().get().url(steamdbURL).headers(headers).build()

        val response = client.newCall(request).execute()

        return response.use {
            val responseBody = response.body

            if (responseBody != null) {
                val document = Jsoup.parse(responseBody.string())

                document.select("table#table-sortable > tbody > tr.app").map {
                    val tds = it.children()
                    SteamGame(steamURLPrefix + tds.first().text(), tds[2].ownText())
                }


            } else {
                null
            }
        }
    }


}

@JsonClass(generateAdapter = true)
data class SteamGame(val steamUrl: String, val title: String) {
    override fun toString(): String {
        return title
    }
}
