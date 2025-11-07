package org.fit4j.examples.dynamodb

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.sql.Timestamp
import java.util.*

@DynamoDBTable(tableName = "PlayerTable")
class PlayerItem {
    @get:DynamoDBHashKey(attributeName = "PK")
    var pK: String? = null

    @get:DynamoDBRangeKey(attributeName = "SK")
    var sK: String? = null

    @get:DynamoDBAttribute(attributeName = "id")
    var id: Long? = null

    @get:DynamoDBAttribute(attributeName = "playerName")
    var name: String? = null

    private var created: Timestamp? = null
    private var modified: Timestamp? = null

    constructor()

    constructor(
        id: Long?, name: String?
    ) {
        this.id = id
        this.name = name
        this.created = Timestamp(Date().time)
        this.modified = this.created
        this.pK = "PLAYER#$id"
        this.sK = "PLAYER#$id"
    }

    @DynamoDBAttribute(attributeName = "created")
    @DynamoDBTypeConverted(converter = TimestampConverter::class)
    fun getCreated(): Timestamp? {
        return created
    }

    fun setCreated(created: Timestamp?) {
        this.created = created
    }

    @DynamoDBAttribute(attributeName = "modified")
    @DynamoDBTypeConverted(converter = TimestampConverter::class)
    fun getModified(): Timestamp? {
        return modified
    }

    fun setModified(modified: Timestamp?) {
        this.modified = modified
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerItem

        if (pK != other.pK) return false
        if (sK != other.sK) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (created != other.created) return false
        if (modified != other.modified) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pK?.hashCode() ?: 0
        result = 31 * result + (sK?.hashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (created?.hashCode() ?: 0)
        result = 31 * result + (modified?.hashCode() ?: 0)
        return result
    }
}

class TimestampConverter : DynamoDBTypeConverter<Long, Timestamp> {
    override fun convert(timestamp: Timestamp): Long {
        return timestamp.time
    }

    override fun unconvert(value: Long): Timestamp {
        return Timestamp(value)
    }
}