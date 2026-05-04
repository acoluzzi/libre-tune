package com.coluzziandrea.libretune_extractor.client.response.section.content.menu

import com.coluzziandrea.libretune_extractor.client.response.section.content.ContentTitle
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class MenuNavigationItemRenderer(
    val text: ContentTitle,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class MenuServiceItemRenderer(
    val text: ContentTitle,
    val serviceEndpoint: NavigationEndpoint? = null
)


@Serializable(with = MenuItemSerializer::class)
sealed interface MenuItem {
    @Serializable
    class EmptyContent : MenuItem

    @Serializable
    data class MenuNavigationItemContent(val menuNavigationItemRenderer: MenuNavigationItemRenderer) :
        MenuItem

    @Serializable
    data class MenuServiceItemContent(val menuServiceItemRenderer: MenuServiceItemRenderer) :
        MenuItem
}

object MenuItemSerializer :
    JsonContentPolymorphicSerializer<MenuItem>(MenuItem::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<MenuItem> =
        when {

            "menuNavigationItemRenderer" in element.jsonObject -> MenuItem.MenuNavigationItemContent.serializer()

            "menuServiceItemRenderer" in element.jsonObject -> MenuItem.MenuServiceItemContent.serializer()
            // Default case
            else -> MenuItem.EmptyContent.serializer()
        }

}