package com.udemy.libraries.acceptancetests.legacy_api.eventtracking

class ChatResponseGeneratedBuilder {
    var chatId: String = "chat-id"
    var userId: Long = 1
    var organizationId: Long = 1
    private var userMessageId : String = "user-message-id"

    fun setChatId(chatId:String) : ChatResponseGeneratedBuilder{
        this.chatId = chatId
        return this
    }

    fun setUserId(userId:Long) : ChatResponseGeneratedBuilder {
        this.userId = userId
        return this
    }

    fun setOrganizationId(orgId:Long) : ChatResponseGeneratedBuilder {
        this.organizationId = orgId
        return this
    }

    fun setUserMessageId(msgId: String) : ChatResponseGeneratedBuilder {
        this.userMessageId = msgId
        return this
    }

    fun build() : ChatResponseGenerated {
        return ChatResponseGenerated()
    }
}