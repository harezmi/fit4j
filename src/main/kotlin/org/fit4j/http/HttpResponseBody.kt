package org.fit4j.http

import java.nio.charset.Charset

sealed class HttpResponseBody {

    open fun asText(charset: Charset = Charsets.UTF_8): String? = null

    open fun asBytes(charset: Charset = Charsets.UTF_8): ByteArray = byteArrayOf()

    object Empty : HttpResponseBody() {
        override fun asText(charset: Charset): String? = null

        override fun asBytes(charset: Charset): ByteArray = byteArrayOf()
    }

    data class Text(val content: String) : HttpResponseBody() {
        override fun asText(charset: Charset): String? = content

        override fun asBytes(charset: Charset): ByteArray = content.toByteArray(charset)
    }

    class Bytes(val content: ByteArray) : HttpResponseBody() {
        override fun asText(charset: Charset): String? = String(content, charset)

        override fun asBytes(charset: Charset): ByteArray = content

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Bytes) return false
            return content.contentEquals(other.content)
        }

        override fun hashCode(): Int = content.contentHashCode()

        override fun toString(): String = "Bytes(${content.size})"
    }

    companion object {
        @JvmStatic
        fun empty(): HttpResponseBody = Empty

        @JvmStatic
        fun text(content: String): HttpResponseBody = Text(content)

        @JvmStatic
        fun bytes(content: ByteArray): HttpResponseBody = Bytes(content)
    }
}
