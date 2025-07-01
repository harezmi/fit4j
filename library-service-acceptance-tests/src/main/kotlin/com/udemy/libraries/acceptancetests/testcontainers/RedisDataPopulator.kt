package com.udemy.libraries.acceptancetests.testcontainers

import org.springframework.core.io.Resource
import redis.clients.jedis.Jedis
import redis.clients.jedis.params.XAddParams

class RedisDataPopulator(redisConnectionProperties: RedisConnectionProperties) : TestContainerDataPopulator {

    private var jedis: Jedis = Jedis(redisConnectionProperties.host, redisConnectionProperties.port)

    override fun populateData(resource: Resource) {
        val dataProvider = RedisDataProvider(resource)
        val redisData = dataProvider.getRedisData()
        redisData.strings.forEach {
            jedis.set(it.key, it.value)
        }
        redisData.hashes.forEach {
            jedis.hmset(it.key, it.fields)
        }
        redisData.lists.forEach {
            jedis.lpush(it.key, *it.values.toTypedArray())
        }
        redisData.sets.forEach {
            jedis.sadd(it.key, *it.members.toTypedArray())
        }
        redisData.sortedsets.forEach {
            val membersMap = mutableMapOf<String,Double>()
            it.members.forEach {
                membersMap[it.value] = it.score
            }
            jedis.zadd(it.key, membersMap)
        }
        redisData.bitmaps.forEach {
            it.bits.forEach { bit ->
                jedis.setbit(it.key, bit.offset.toLong(), bit.value)
            }
        }
        redisData.hyperloglogs.forEach {
            jedis.pfadd(it.key, *it.values.toTypedArray())
        }
        redisData.geospatials.forEach {
            it.locations.forEach { location ->
                jedis.geoadd(it.key, location.longitude, location.latitude, location.name)
            }
        }
        redisData.streams.forEach { stream ->
            stream.entries.forEach { entry ->
                jedis.xadd(stream.key, entry.fields, XAddParams())
            }
        }
    }

    fun getJedis(): Jedis {
        return jedis
    }
}

data class RedisConnectionProperties(
    val host: String,
    val port: Int
)