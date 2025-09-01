package com.fit4j.testcontainers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.yaml.snakeyaml.Yaml

class RedisDataProvider(resource: Resource) {
    private var redisData: RedisData

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        if(resource.exists()) {
            logger.debug("Loading Redis data from ${resource.filename}")
            val yaml = Yaml()
            val entries = yaml.load(resource.inputStream) as List<Map<String,Any>>
            val stringEntries = mutableListOf<RedisString>()
            val hashEntries = mutableListOf<RedisHash>()
            val listEntries = mutableListOf<RedisList>()
            val setEntries = mutableListOf<RedisSet>()
            val sortedSetEntries = mutableListOf<RedisSortedSet>()
            val bitmapEntries = mutableListOf<RedisBitmap>()
            val hyperLogLogEntries = mutableListOf<RedisHyperLogLog>()
            val geoEntries = mutableListOf<RedisGeo>()
            val streamEntries = mutableListOf<RedisStream>()
            entries.forEach {
                val entry = it.get("entry") as Map<String,Any>
                val type = entry.get("type")
                val data = entry.get("data") as Map<String,Any>
                when (type) {
                    "string" -> {
                        val entry = buildStringEntry(data)
                        stringEntries.add(entry)
                    }
                    "hash" -> {
                        val entry = buildHashEntry(data)
                        hashEntries.add(entry)
                    }
                    "list" -> {
                        val entry = buildListEntry(data)
                        listEntries.add(entry)
                    }
                    "set" -> {
                        val entry = buildSetEntry(data)
                        setEntries.add(entry)
                    }
                    "sortedset" -> {
                        val entry = buildSortedSetEntry(data)
                        sortedSetEntries.add(entry)
                    }
                    "bitmap" -> {
                        val entry = buildBitmapEntry(data)
                        bitmapEntries.add(entry)
                    }
                    "hyperloglog" -> {
                        val entry = buildHyperLogLogEntry(data)
                        hyperLogLogEntries.add(entry)
                    }
                    "geospatial" -> {
                        val entry = buildGeospatialEntry(data)
                        geoEntries.add(entry)
                    }
                    "stream" -> {
                        val entry = buildStreamEntry(data)
                        streamEntries.add(entry)
                    }
                    else -> throw IllegalArgumentException("Unknown type: $type")
                }
            }

            redisData = RedisData(strings = stringEntries,
                                hashes = hashEntries,
                                lists = listEntries,
                                sets = setEntries,
                                sortedsets = sortedSetEntries,
                                bitmaps = bitmapEntries,
                                hyperloglogs = hyperLogLogEntries,
                                geospatials = geoEntries,
                                streams = streamEntries)
        } else {
            logger.debug("No Redis data found at ${resource.filename}, check your filename or make sure it is located in the classpath")
            redisData = RedisData()
        }
    }

    private fun buildStringEntry(data: Map<String,Any>): RedisString {
        return RedisString(data.get("key") as String, data.get("value") as String)
    }

    private fun buildHashEntry(data: Map<String,Any>): RedisHash {
        return RedisHash(data.get("key") as String, data.get("fields") as Map<String, String>)
    }

    private fun buildListEntry(data: Map<String,Any>): RedisList {
        return RedisList(data.get("key") as String, data.get("values") as List<String>)
    }

    private fun buildSetEntry(data: Map<String,Any>): RedisSet {
        return RedisSet(data.get("key") as String, data.get("members") as List<String>)
    }

    private fun buildSortedSetEntry(data: Map<String,Any>): RedisSortedSet {
        val members = (data.get("members") as List<Map<String,Any>>).map {
            SortedSetMember(it.get("value") as String, it.get("score") as Double)
        }
        return RedisSortedSet(data.get("key") as String, members)
    }

    private fun buildBitmapEntry(data: Map<String,Any>): RedisBitmap {
        val bits = (data.get("bits") as List<Map<String,Any>>).map {
            BitmapBit(it.get("offset") as Int, it.get("value") as Boolean)
        }
        return RedisBitmap(data.get("key") as String, bits)
    }

    private fun buildHyperLogLogEntry(data: Map<String,Any>): RedisHyperLogLog {
        return RedisHyperLogLog(data.get("key") as String, data.get("values") as List<String>)
    }

    private fun buildGeospatialEntry(data: Map<String,Any>): RedisGeo {
        val locations = (data.get("locations") as List<Map<String,Any>>).map {
            GeoLocation(it.get("name") as String, it.get("longitude") as Double, it.get("latitude") as Double)
        }
        return RedisGeo(data.get("key") as String, locations)
    }

    private fun buildStreamEntry(data: Map<String,Any>): RedisStream {
        val entries = (data.get("entries") as List<Map<String,String>>).map {
            StreamEntry(it["fields"] as Map<String, String>)
        }
        return RedisStream(data.get("key") as String, entries)
    }

    fun getRedisData(): RedisData {
        return redisData
    }
}

data class RedisData(
    val strings: List<RedisString> = emptyList(),
    val hashes: List<RedisHash> = emptyList(),
    val lists: List<RedisList> = emptyList(),
    val sets: List<RedisSet> = emptyList(),
    val sortedsets: List<RedisSortedSet> = emptyList(),
    val bitmaps: List<RedisBitmap> = emptyList(),
    val hyperloglogs: List<RedisHyperLogLog> = emptyList(),
    val geospatials: List<RedisGeo> = emptyList(),
    val streams: List<RedisStream> = emptyList()
)

data class RedisString(val key: String, val value: String)
data class RedisHash(val key: String, val fields: Map<String, String>)
data class RedisList(val key: String, val values: List<String>)
data class RedisSet(val key: String, val members: List<String>)
data class RedisSortedSet(val key: String, val members: List<SortedSetMember>)
data class SortedSetMember(val value: String, val score: Double)
data class RedisBitmap(val key: String, val bits: List<BitmapBit>)
data class BitmapBit(val offset: Int, val value: Boolean)
data class RedisHyperLogLog(val key: String, val values: List<String>)
data class RedisGeo(val key: String, val locations: List<GeoLocation>)
data class GeoLocation(val name: String, val longitude: Double, val latitude: Double)
data class RedisStream(val key: String, val entries: List<StreamEntry>)
data class StreamEntry(val fields: Map<String, String>)