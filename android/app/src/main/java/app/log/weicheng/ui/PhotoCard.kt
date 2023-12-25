package app.log.weicheng.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import app.log.weicheng.R
import coil.compose.AsyncImage

// For Google Online Fonts Reference Usage
//val provider = GoogleFont.Provider(
//    providerAuthority = "com.google.android.gms.fonts",
//    providerPackage = "com.google.android.gms",
//    certificates = R.array.com_google_android_gms_fonts_certs
//)
//val fontName = GoogleFont("Lobster Two")
//val fontNameTitle = GoogleFont("M PLUS Rounded 1c")
//val fontNameContent = GoogleFont("Ubuntu")
//val fontNameNotes = GoogleFont("Caveat")
//val fontFamilyNotes = FontFamily(
//    Font(googleFont = fontNameNotes, fontProvider = provider)
//)

val fontFamily = FontFamily(Font(R.font.lobster_two))
val fontFamilyTitle = FontFamily(Font(R.font.mplus_rounded1c))
val fontFamilyContent = FontFamily(Font(R.font.ubuntu))
val fontFamilyNotes = FontFamily(Font(R.font.caveat))

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(ExperimentalFoundationApi::class)
@RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
@Composable
fun PhotoCard(elementResponse: ElementResponse) {
    @OptIn(ExperimentalFoundationApi::class)
    val pagerState = rememberPagerState(pageCount = {elementResponse.photos.size})
    // Use HorizontalPager to display photo information
    HorizontalPager(state = pagerState) { page ->
        val photo = elementResponse.photos.values.elementAtOrNull(page)
        photo?.let {
            PhotoCard(photo = it, number = page + 1, total = elementResponse.photos.size)
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class) @Composable
fun RoundedCornerPhoto(
    title: String,
    message: String,
    showcaseLink: String,
    originalQualityLink: String,
    number: Int,
    total: Int
) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var context = LocalContext.current
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }
    var photoTags by remember { mutableStateOf(emptyList<String>()) }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
//            .aspectRatio(0.6f)
            .clip(RoundedCornerShape(16.dp))
//            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
//                        .padding(bottom = 8.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontFamily = fontFamily,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(0.9f)
                )
                Text(
                    text = number.toString() + "/" + total.toString(),
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(0.1f)
                )
            }
            AsyncImage(
                model = showcaseLink,
                contentDescription = null,
                placeholder = painterResource(R.drawable.cupcake),
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .clickable {
                        // Handle click to toggle fullscreen or perform other actions
                        scale = 1f
                        rotation = 0f
                        offset = Offset.Zero
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        rotationZ = rotation,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = state)
            )

            Text(
                text = message,
                fontSize = 20.sp,
                fontFamily = fontFamilyContent,
                textAlign = TextAlign.Start,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                val webIntent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(originalQualityLink))
                try {
                    ContextCompat.startActivity(context, webIntent, null)
                } catch (e: ActivityNotFoundException) {
                    // Define what your app should do if no activity can handle the intent.
                }
            }) {
                Text(stringResource(R.string.download_original_file))
            }

            // TODO: Correct EXIF read error: InstantiationException: java.lang.Class<com.drew.metadata.exif.ExifIFD0Directory> cannot be instantiated
//            Button(onClick = {
//                // Create a new coroutine scope
//                CoroutineScope(Dispatchers.Default).launch {
//                    // Create a new coroutine scope
//                    val scope = CoroutineScope(Dispatchers.Default)
//
//                    // Launch a new coroutine in the scope
//                    scope.launch {
//                        val url = URL("")
////                    val imageData = url.readBytes()
//                        // Read image metadata directly from the URL
//                        try {
//                            var metadata = ImageMetadataReader.readMetadata(
//                                withContext(
//                                    Dispatchers.IO
//                                ) {
//                                    url.openStream()
//                                })
//
//                            val processedTags = mutableListOf<String>()
//                            for (directory in metadata.directories) {
//                                for (tag in directory.tags) {
//                                    val tagString = "${tag.tagName}: ${tag.description}"
//                                    processedTags.add(tagString)
//                                }
//                            }
//
//                            photoTags = processedTags
//                        } catch (e: MetadataException) {
//                            // Handle MetadataException
//                            e.printStackTrace()
//                        } catch (e: IOException) {
//                            // Handle IOException
//                            e.printStackTrace()
//                        } catch (e: Exception) {
//                            // Catch any other exceptions
//                            e.printStackTrace()
//                        }
//                    }
//                }
//            }) {
//                Text("Check EXIF information")
//            }
//
//
//            if(photoTags != null) {
//                Text("EXIF Information:")
//                for (tag in photoTags!!) {
//                    Text(tag)
//                }
//            }

        }
    }
}