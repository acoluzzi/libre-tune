package com.coluzziandrea.libretune_extractor.response.tab.section.content

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

object SectionContentSerializer :
    JsonContentPolymorphicSerializer<SectionContent>(SectionContent::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<SectionContent> =
        when {
            "musicShelfRenderer" in element.jsonObject -> SectionContent.MusicShelfContent.serializer()

            "musicCarouselShelfRenderer" in element.jsonObject -> SectionContent.MusicCarouselContent.serializer()


            "musicResponsiveListItemRenderer" in element.jsonObject -> SectionContent.MusicResponsiveListItemContent.serializer()

            // Default case
            else -> SectionContent.EmptyContent.serializer()
        }

}