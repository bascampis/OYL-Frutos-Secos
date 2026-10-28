package cl.oyl.frutossecos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage

@Composable
fun ProductImage(
    productId: String,
    imageUrl: String?,
    description: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fallback = painterResource(id = productImage(productId))
    if (imageUrl.isNullOrBlank()) {
        Image(painter = fallback, contentDescription = description, modifier = modifier, contentScale = contentScale)
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = description,
            modifier = modifier,
            contentScale = contentScale,
            placeholder = fallback,
            error = fallback
        )
    }
}