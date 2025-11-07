package org.fit4j.mock.declarative

class DeclarativeTestFixtureDrivenServiceResponseProvider(
    private val jsonToMockResponseConverterList: List<JsonToMockResponseConverter>,
    private val declarativeTestFixtureProvider: DeclarativeTestFixtureProvider) {

    fun getResponseFor(request: Any?): Any? {
        val testFixturesGroup = declarativeTestFixtureProvider.getTestFixturesForCurrentTest()
        if(testFixturesGroup != null) {
            val rawJsonContent = testFixturesGroup.build(request)
            if(rawJsonContent != null) {
                return jsonToMockResponseConverterList.first {
                    it.isApplicableFor(request)
                }.convert(rawJsonContent,request!!)
            }
        }
        return null
    }
}