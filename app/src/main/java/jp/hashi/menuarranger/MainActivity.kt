package jp.hashi.menuarranger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import jp.hashi.menuarranger.model.MenuItem
import jp.hashi.menuarranger.ui.theme.MenuArrangerTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this, MenuViewModelFactory(application)
        )[MenuViewModel::class.java]
        setContent {
            MenuArrangerTheme {
                Surface {
                    Greeting(
                        viewModel,
                        onChooseBtnClick = { viewModel.chooseMenuItems(it) },
                        onAlcoholSwitchClick = { viewModel.setAlcohol(it) },
                        onKidsSwitchClick = { viewModel.setKids(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    viewModel: MenuViewModel,
    onChooseBtnClick: (Int) -> Unit,
    onAlcoholSwitchClick: (Boolean) -> Unit,
    onKidsSwitchClick: (Boolean) -> Unit
) {
    val menuList = viewModel.chosenMenuItems.observeAsState()
    var ceilingPrice by remember { mutableIntStateOf(1000) }
    var alcoholContains by remember { mutableStateOf(true) }
    var kidsContains by remember { mutableStateOf(true) }
    var showMenuId by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.Bottom, modifier = Modifier
            .wrapContentHeight()
            .padding(bottom = 8.dp)) {
            Row(Modifier.padding(8.dp)) {
                TextField(
                    value = ceilingPrice.toString(),
                    onValueChange = { ceilingPrice = try {it.toInt()} catch (e: NumberFormatException) {0}},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(2f),
                    label = { Text(text = "上限金額") }
                )
                Text("円",
                    Modifier
                        .align(Alignment.Bottom)
                        .padding(8.dp))
                Button(
                    onClick = { onChooseBtnClick(ceilingPrice) },
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp)
                ) {
                    Text(text = "ガチャ！")
                }
            }
            Column(Modifier.padding(start = 16.dp)) {
                Row {
                    Switch(onCheckedChange = {
                        alcoholContains = it
                        onAlcoholSwitchClick(it)
                    }, checked = alcoholContains)
                    Text(text = stringResource(R.string.contains_alcohol),
                        Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 4.dp, end = 8.dp))
                }
                Row {
                    Switch(onCheckedChange = {
                        kidsContains = it
                        onKidsSwitchClick(it)
                    }, checked = kidsContains)
                    Text(text = stringResource(R.string.contains_kids),
                        Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 4.dp, end = 8.dp))
                }
                Row {
                    Switch(onCheckedChange = {
                        showMenuId = it
                    }, checked = showMenuId)
                    Text(text = stringResource(R.string.show_menu_number),
                        Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 4.dp, end = 8.dp))
                }
            }
            Text("合計: " + menuList.value?.sumOf { it.price }.toString() + "円",
                fontSize = androidx.compose.material3.MaterialTheme.typography.displaySmall.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        LazyColumn(verticalArrangement = Arrangement.Top, modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp)) {
            menuList.value?.size?.let {
                items(it) { item ->
                    Column {
                        MenuListItem(menuList.value!![item], showMenuId)
                    }
                }
            }
        }

    }
}

@Composable
fun MenuListItem(item: MenuItem, showMenuId: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            item.name, Modifier.weight(1f),
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
        if (showMenuId) Text(
            text = "ID: " + item.menuId.toString(),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        else Text(
            text = item.price.toString() + "円",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MenuArrangerTheme {
        Column {
            listOf(
                MenuItem(
                    1,
                    1305,
                    "田舎風ミネストローネ",
                    273,
                    arrayOf("east", "west", "saitama"),
                    "soup",
                    true
                ),
                MenuItem(
                    2,
                    1307,
                    "たまねぎのズッパ",
                    273,
                    arrayOf("east", "west", "saitama"),
                    "soup",
                    true
                ),
                MenuItem(
                    3,
                    1401,
                    "辛味チキン",
                    273,
                    arrayOf("east", "west", "saitama"),
                    "appetizer",
                    true
                ),
                MenuItem(
                    4,
                    1402,
                    "アロスティチーニ",
                    364,
                    arrayOf("east", "west", "saitama"),
                    "appetizer",
                    true
                ),
            ).forEach { MenuListItem(it, false) }
        }
    }
}