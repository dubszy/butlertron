package com.portalsoup.mrleaguy.commands

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.lang.RuntimeException

class MagicLookup : AbstractCommand() {

    val url = "https://api.scryfall.com/cards/named?fuzzy="

    // For testing
    val sampleResponse = "{\"object\":\"card\",\"id\":\"bef16a71-5ed2-4f30-a844-c02a0754f679\",\"oracle_id\":\"09cc8709-fe10-472a-b05c-e89f3523018d\",\"multiverse_ids\":[438576],\"mtgo_id\":65899,\"mtgo_foil_id\":65900,\"tcgplayer_id\":145297,\"name\":\"Austere Command\",\"lang\":\"en\",\"released_at\":\"2017-11-17\",\"uri\":\"https://api.scryfall.com/cards/bef16a71-5ed2-4f30-a844-c02a0754f679\",\"scryfall_uri\":\"https://scryfall.com/card/ima/10/austere-command?utm_source=api\",\"layout\":\"normal\",\"highres_image\":true,\"image_uris\":{\"small\":\"https://img.scryfall.com/cards/small/front/b/e/bef16a71-5ed2-4f30-a844-c02a0754f679.jpg?1562853529\",\"normal\":\"https://img.scryfall.com/cards/normal/front/b/e/bef16a71-5ed2-4f30-a844-c02a0754f679.jpg?1562853529\",\"large\":\"https://img.scryfall.com/cards/large/front/b/e/bef16a71-5ed2-4f30-a844-c02a0754f679.jpg?1562853529\",\"png\":\"https://img.scryfall.com/cards/png/front/b/e/bef16a71-5ed2-4f30-a844-c02a0754f679.png?1562853529\",\"art_crop\":\"https://img.scryfall.com/cards/art_crop/front/b/e/bef16a71-5ed2-4f30-a844-c02a0754f679.jpg?1562853529\",\"border_crop\":\"https://img.scryfall.com/cards/border_crop/front/b/e/bef16a71-5ed2-4f30-a844-c02a0754f679.jpg?1562853529\"},\"mana_cost\":\"{4}{W}{W}\",\"cmc\":6.0,\"type_line\":\"Sorcery\",\"oracle_text\":\"Choose two —\\n• Destroy all artifacts.\\n• Destroy all enchantments.\\n• Destroy all creatures with converted mana cost 3 or less.\\n• Destroy all creatures with converted mana cost 4 or greater.\",\"colors\":[\"W\"],\"color_identity\":[\"W\"],\"legalities\":{\"standard\":\"not_legal\",\"future\":\"not_legal\",\"historic\":\"not_legal\",\"pioneer\":\"not_legal\",\"modern\":\"legal\",\"legacy\":\"legal\",\"pauper\":\"not_legal\",\"vintage\":\"legal\",\"penny\":\"not_legal\",\"commander\":\"legal\",\"brawl\":\"not_legal\",\"duel\":\"legal\",\"oldschool\":\"not_legal\"},\"games\":[\"mtgo\",\"paper\"],\"reserved\":false,\"foil\":true,\"nonfoil\":true,\"oversized\":false,\"promo\":false,\"reprint\":true,\"variation\":false,\"set\":\"ima\",\"set_name\":\"Iconic Masters\",\"set_type\":\"masters\",\"set_uri\":\"https://api.scryfall.com/sets/741bcd30-7709-4133-8919-f4b46483bed7\",\"set_search_uri\":\"https://api.scryfall.com/cards/search?order=set&q=e%3Aima&unique=prints\",\"scryfall_set_uri\":\"https://scryfall.com/sets/ima?utm_source=api\",\"rulings_uri\":\"https://api.scryfall.com/cards/bef16a71-5ed2-4f30-a844-c02a0754f679/rulings\",\"prints_search_uri\":\"https://api.scryfall.com/cards/search?order=released&q=oracleid%3A09cc8709-fe10-472a-b05c-e89f3523018d&unique=prints\",\"collector_number\":\"10\",\"digital\":false,\"rarity\":\"rare\",\"card_back_id\":\"0aeebaf5-8c7d-4636-9e82-8c27447861f7\",\"artist\":\"Anna Steinbauer\",\"artist_ids\":[\"3516496c-c279-4b56-8239-720683d03ae0\"],\"illustration_id\":\"7c6a01f8-e1f6-4fe4-b275-b2582be98783\",\"border_color\":\"black\",\"frame\":\"2015\",\"full_art\":false,\"textless\":false,\"booster\":true,\"story_spotlight\":false,\"edhrec_rank\":181,\"prices\":{\"usd\":\"7.77\",\"usd_foil\":\"11.54\",\"eur\":\"5.75\",\"tix\":\"0.46\"},\"related_uris\":{\"gatherer\":\"https://gatherer.wizards.com/Pages/Card/Details.aspx?multiverseid=438576\",\"tcgplayer_decks\":\"https://decks.tcgplayer.com/magic/deck/search?contains=Austere+Command&page=1&partner=Scryfall&utm_campaign=affiliate&utm_medium=scryfall&utm_source=scryfall\",\"edhrec\":\"https://edhrec.com/route/?cc=Austere+Command\",\"mtgtop8\":\"https://mtgtop8.com/search?MD_check=1&SB_check=1&cards=Austere+Command\"},\"purchase_uris\":{\"tcgplayer\":\"https://shop.tcgplayer.com/product/productsearch?id=145297&partner=Scryfall&utm_campaign=affiliate&utm_medium=scryfall&utm_source=scryfall\",\"cardmarket\":\"https://www.cardmarket.com/en/Magic/Products/Singles/Iconic-Masters/Austere-Command?referrer=scryfall&utm_campaign=card_prices&utm_medium=text&utm_source=scryfall\",\"cardhoarder\":\"https://www.cardhoarder.com/cards/65899?affiliate_id=scryfall&ref=card-profile&utm_campaign=affiliate&utm_medium=card&utm_source=scryfall\"}}"


    val noResultsText = "Didn't find the card.  The search could have been too broad to confidently" +
            " pick the correct match, or didn't match any cards at all."

    private val apiClient = OkHttpClient()

    override fun runPredicate(event: GuildMessageReceivedEvent): Boolean {
        return prefixPredicate(event.message.contentRaw, "mtg")
    }

    override fun run(event: GuildMessageReceivedEvent) {
        println("Raw message=${event.message.contentRaw}")
        try {
            val term = event.message.contentRaw
                .toLowerCase()
                .substring(3)
                .trim()
                .replace(" ", "+")
            println("term: ${term}")

            val cardUrl = getCardUrl(term)
            println("cardUrl: ${cardUrl}")

            event.channel.sendMessage(cardUrl).queue()
        } catch (e: NoResultsFoundException) {
            event.channel.sendMessage(noResultsText).queue()
        } catch (e2: RuntimeException) {
//            event.channel.addReactionById(event.message.id, Emote())
        }
    }

    fun getCardUrl(fuzzyName: String): String =
        JSONObject(makeCardRequest(fuzzyName))
            .getJSONObject("image_uris")
            .getString("normal").toString()

    fun makeCardRequest(fuzzyName: String): String {
        val request = Request.Builder()
            .url(url + fuzzyName)
            .build()

        var response = ""

        apiClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("API failed")
                e.printStackTrace()
            }

            override fun onResponse(call: Call, r: Response) {
                println(r.toString())
                response = r.body().toString()
            }

        })

        if (response == "null"|| response.isEmpty()) {
            println("Failed: response=${response}")
            throw NoResultsFoundException()
        }

        return response
    }

    private class NoResultsFoundException: RuntimeException()
}
