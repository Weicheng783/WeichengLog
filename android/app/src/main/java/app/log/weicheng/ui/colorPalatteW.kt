import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.ImageColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.gson.Gson
import com.google.gson.JsonArray
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import androidx.core.content.edit


private fun colorItemToJson(colorItem: ColorItem): String {
    val gson = Gson()
    return gson.toJson(colorItem)
}

fun saveColorItemsToSharedPreferences(context: Context, items: List<ColorItem>) {
    val gson = Gson()
    val jsonArray = JsonArray()
    items.forEach { item ->
        jsonArray.add(gson.toJsonTree(item))
    }
    val jsonString = gson.toJson(jsonArray)
    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    with(sharedPref.edit()) {
        putString("colorItems", jsonString)
        apply()
    }
}

fun loadColorItemsFromSharedPreferences(context: Context): List<ColorItem> {
    val gson = Gson()
    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val jsonString = sharedPref.getString("colorItems", null)
    return if (jsonString != null) {
        val jsonArray = gson.fromJson(jsonString, JsonArray::class.java)
        jsonArray.map { gson.fromJson(it.asJsonObject, ColorItem::class.java) }
    } else {
        emptyList()
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ElevatedCardColorP(context: Context) {
    val controller = rememberColorPickerController()
    var color: Color by remember { mutableStateOf(Color.White) }
    var hexCode: String by remember { mutableStateOf("ffffff") }
    var task by remember { mutableStateOf("") }

    var items = remember {
        mutableStateOf(loadColorItemsFromSharedPreferences(context).map {
            ColorItem(it.uuid, it.hexCode, it.color, it.task)
        }.toMutableList())
    }

    val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun addColorItem() {
        val uuid = UUID.randomUUID().toString()
        val newItem = ColorItem(uuid, hexCode, color, task)
        items.value.add(newItem)
        saveColorItemsToSharedPreferences(context, items.value)
        task = "" // Clear the task field
        items = mutableStateOf(loadColorItemsFromSharedPreferences(context).map {
            ColorItem(it.uuid, it.hexCode, it.color, it.task)
        }.toMutableList())
    }

    fun removeColorItem(uuid: String) {
        items.value.removeIf { it.uuid == uuid }
        saveColorItemsToSharedPreferences(context, items.value)
        items = mutableStateOf(loadColorItemsFromSharedPreferences(context).map {
            ColorItem(it.uuid, it.hexCode, it.color, it.task)
        }.toMutableList())
    }

    fun archiveColorItem(uuid: String) {
        val item = items.value.find { it.uuid == uuid }
        if (item != null) {
            items.value.remove(item)
            println("Archived item: $item")
            saveColorItemsToSharedPreferences(context, items.value)
        }
        items = mutableStateOf(loadColorItemsFromSharedPreferences(context).map {
            ColorItem(it.uuid, it.hexCode, it.color, it.task)
        }.toMutableList())
    }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.9f)
            .height(1000.dp)
    ) {
        items(items.value.size) { index ->
            val item = items.value[index]
            Column(modifier = Modifier.padding(8.dp)) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                            .background(Color(item.hexCode.toLong(16)))
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = item.hexCode, fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { removeColorItem(item.uuid) }, enabled = false) {
                        Text(stringResource(app.log.weicheng.R.string.trash))
                    }
//                    Button(onClick = { archiveColorItem(item.uuid) }) {
//                        Text("Archive")
//                    }
                }
                Text(text = item.task, fontSize = 14.sp, modifier = Modifier.padding(start = 40.dp))
            }
        }

        item {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(500.dp)
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope ->
                        color = colorEnvelope.color
                        hexCode = colorEnvelope.hexCode
                    }
                )
                Row(modifier = Modifier.padding(top = 16.dp)) {
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                            .background(color)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = hexCode)
                }
                TextField(
                    value = task,
                    onValueChange = { task = it },
                    label = { Text(stringResource(app.log.weicheng.R.string.task)) },
                    modifier = Modifier.padding(top = 16.dp)
                )
                Button(onClick = {
                    addColorItem()
                    items = mutableStateOf(loadColorItemsFromSharedPreferences(context).map {
                        ColorItem(it.uuid, it.hexCode, it.color, it.task)
                    }.toMutableList())
                 }, modifier = Modifier.padding(top = 16.dp)) {
                    Text(stringResource(app.log.weicheng.R.string.add_new))
                }
            }
        }
    }
}

data class ColorItem(val uuid: String, val hexCode: String, val color: Color, val task: String)

@Preview(showBackground = true)
@Composable
fun ElevatedCardColorPreview() {
    MaterialTheme {
        ElevatedCardColorP(LocalContext.current)
    }
}

@Composable
fun CardSliderColorP(context: Context) {
    var selectedIndex by remember { mutableIntStateOf(0) } // -1 表示没有选中任何按钮
    var colorItems = remember {
        mutableStateOf(loadColorItemsFromSharedPreferences(context).map {
            ColorItem(it.uuid, it.hexCode, it.color, it.task)
        }.toMutableList())
    }
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // 当 selectedIndex 改变时，保存到 SharedPreferences
    DisposableEffect(selectedIndex) {
        onDispose {
            with(context) {
                sharedPreferences.edit() {
                    putInt("selectedIndex", selectedIndex)
                }
            }
        }
    }

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(5.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(colorItems.value.size) { index ->
                val isSelected = index == selectedIndex

                Button(
                    onClick = {
                        selectedIndex = index // 更新选中的按钮
                        println("Button $index clicked")
                    },
                    modifier = Modifier
//                        .size(80.dp) // 假设按钮宽度为80dp，高度由内容决定
                        .padding(horizontal = 5.dp, vertical = 4.dp), // 调整内边距
                    shape = RoundedCornerShape(8.dp), // 圆角
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary // 高亮选中的按钮
                    )
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Box(
                            modifier = Modifier
                                .width(30.dp) // 增加宽度以更好地显示颜色块
                                .height(30.dp) // 设置高度以保持宽高比
                                .background(
                                    colorItems.value[index].color
                                )
                                .clip(RoundedCornerShape(4.dp)) // 可选：添加圆角
                        )

                        Spacer(modifier = Modifier.width(12.dp)) // 在 Box 和 Text 之间添加间距

                        Text(
                            text = colorItems.value[index].task,
                            color = if(isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondary, // 文本颜色
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 8.dp) // 右侧内边距，确保文本不贴边
                        )
                    }

                }
                // 可选：为每个按钮之间添加间距（这里已经通过按钮自身的padding和size控制了）
                // 如果需要额外的间距，可以添加 Spacer，但通常按钮之间的间距由LazyRow的orientation和子项大小决定
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardSliderColorPreview() {
    MaterialTheme {
        CardSliderColorP(LocalContext.current)
    }
}