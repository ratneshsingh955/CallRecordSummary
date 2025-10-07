@file:OptIn(InternalResourceApi::class)

package callrecordsummary.composeapp.generated.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.ResourceItem

private const val MD: String = "composeResources/callrecordsummary.composeapp.generated.resources/"

internal val Res.drawable.apple_icon: DrawableResource by lazy {
      DrawableResource("drawable:apple_icon", setOf(
        ResourceItem(setOf(), "${MD}drawable/apple_icon.png", -1, -1),
      ))
    }

internal val Res.drawable.compose_multiplatform: DrawableResource by lazy {
      DrawableResource("drawable:compose_multiplatform", setOf(
        ResourceItem(setOf(), "${MD}drawable/compose-multiplatform.xml", -1, -1),
      ))
    }

internal val Res.drawable.google_icon: DrawableResource by lazy {
      DrawableResource("drawable:google_icon", setOf(
        ResourceItem(setOf(), "${MD}drawable/google_icon.png", -1, -1),
      ))
    }

internal val Res.drawable.whatsApp_icon: DrawableResource by lazy {
      DrawableResource("drawable:whatsApp_icon", setOf(
        ResourceItem(setOf(), "${MD}drawable/whatsApp_icon.png", -1, -1),
      ))
    }

@InternalResourceApi
internal fun _collectCommonMainDrawable0Resources(map: MutableMap<String, DrawableResource>) {
  map.put("apple_icon", Res.drawable.apple_icon)
  map.put("compose_multiplatform", Res.drawable.compose_multiplatform)
  map.put("google_icon", Res.drawable.google_icon)
  map.put("whatsApp_icon", Res.drawable.whatsApp_icon)
}
