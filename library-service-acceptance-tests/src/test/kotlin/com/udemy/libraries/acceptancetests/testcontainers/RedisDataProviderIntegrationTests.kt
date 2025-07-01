package com.udemy.libraries.acceptancetests.testcontainers

import com.udemy.libraries.acceptancetests.AcceptanceTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader

@AcceptanceTest
class RedisDataProviderIntegrationTests {
    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Test
    fun `it should load redis data from yaml file`() {
        val redisDataProvider = RedisDataProvider(resourceLoader.getResource("classpath:scripts/redis_initial_data.yml"))
        val redisData = redisDataProvider.getRedisData()

        MatcherAssert.assertThat(redisData.strings,Matchers.contains(
            RedisString("stringKey","stringValue")))

        MatcherAssert.assertThat(redisData.hashes,Matchers.contains(
            RedisHash("hashKey", mapOf("field1" to "value1","field2" to "value2"))))

        MatcherAssert.assertThat(redisData.lists,Matchers.contains(
            RedisList("listKey",listOf("value1","value2","value3"))))

        MatcherAssert.assertThat(redisData.sets,Matchers.contains(
            RedisSet("setKey",listOf("value1","value2","value3"))))

        MatcherAssert.assertThat(redisData.sortedsets,Matchers.contains(
            RedisSortedSet("sortedSetKey",listOf(
                SortedSetMember("value1",1.0),SortedSetMember("value2",2.0)
            ))))

        MatcherAssert.assertThat(redisData.bitmaps,Matchers.contains(
            RedisBitmap("bitmapKey",listOf(
                BitmapBit(7,true),BitmapBit(8,false)))))

        MatcherAssert.assertThat(redisData.hyperloglogs,Matchers.contains(
            RedisHyperLogLog("hllKey",listOf("value1","value2"))))

        MatcherAssert.assertThat(redisData.geospatials,Matchers.contains(
            RedisGeo("geoKey",listOf(
                GeoLocation("Palermo",13.361389,38.115556),
                GeoLocation("Catania",15.087269,37.502669)
            ))))

        MatcherAssert.assertThat(redisData.streams,Matchers.contains(
            RedisStream("streamKey",listOf(
                StreamEntry(mapOf("field1" to "value1", "field2" to "value2")),
                StreamEntry(mapOf("field3" to "value3", "field4" to "value4"))
            ))))
    }

}