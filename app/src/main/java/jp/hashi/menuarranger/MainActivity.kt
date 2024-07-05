@file:OptIn(ExperimentalMaterial3Api::class)

package jp.hashi.menuarranger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction2

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
                        onChooseBtnClick = (viewModel::onChooseBtnClick),
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    viewModel: MenuViewModel,
    onChooseBtnClick: KFunction2<Int, (String) -> Unit, Unit>
) {
    val menuList = viewModel.chosenMenuItems.observeAsState()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val hostState = remember { SnackbarHostState() }

    val alcoholContains by viewModel.alcoholContains.observeAsState(true)
    val kidsContains by viewModel.kidsContains.observeAsState(true)
    val ceilingPrice by viewModel.ceilingPrice.observeAsState(1000)
    val showMenuId by viewModel.showMenuId.observeAsState(true)

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = "条件を変更") },
                icon = {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Change conditions to choose menus."
                    )
                },
                onClick = { showBottomSheet = true }
            )
        },
        snackbarHost = { SnackbarHost(hostState) }
    ) { innerPadding ->
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(paddingValues = innerPadding)
            ) {
                Row(Modifier.padding(8.dp)) {
                    TextField(
                        value = ceilingPrice.toString(),
                        onValueChange = {
                            viewModel.setCeilingPrice(
                                try {
                                    it.toInt()
                                } catch (e: NumberFormatException) {
                                    0
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(2f),
                        label = { Text(text = "上限金額") }
                    )
                    Text(
                        "円",
                        Modifier
                            .align(Alignment.Bottom)
                            .padding(8.dp)
                    )
                    Button(
                        onClick = {
                            onChooseBtnClick(ceilingPrice) {
                                scope.launch {
                                    hostState.showSnackbar(it)
                                }
                            }
                        },
                        modifier = Modifier
                            .wrapContentWidth()
                            .align(Alignment.CenterVertically)
                            .padding(start = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Shuffle and choose menus.",
                                Modifier.padding(end = 4.dp)
                            )
                            Text(
                                stringResource(R.string.gatcha),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "合計: ${menuList.value?.sumOf { it.price }.toString()}円",
                        fontSize = MaterialTheme.typography.displaySmall.fontSize,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        "(${menuList.value?.size}品)",
                        Modifier.padding(bottom = 5.dp, start = 4.dp)
                    )
                }
            }
            HorizontalDivider(Modifier.padding(top = 8.dp), thickness = 1.dp)
            LazyColumn(
                verticalArrangement = Arrangement.Top, modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp)
            ) {
                menuList.value?.size?.let {
                    items(it) { item ->
                        (if (showMenuId) 0.dp else 8.dp).let { padding ->
                            Column(Modifier.padding(vertical = padding)) {
                                MenuListItem(menuList.value!![item], showMenuId)
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(top = padding)
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(74.dp)) }
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .padding(bottom = 36.dp)
                ) {

                    Row {
                        Switch(
                            onCheckedChange = viewModel::setAlcoholContains,
                            checked = alcoholContains
                        )
                        Text(
                            text = stringResource(R.string.contains_alcohol),
                            Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 4.dp, end = 8.dp)
                        )
                    }
                    Row {
                        Switch(
                            onCheckedChange =
                            viewModel::setKidsContains, checked = kidsContains
                        )
                        Text(
                            text = stringResource(R.string.contains_kids),
                            Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 4.dp, end = 8.dp)
                        )
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
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showMenuId) FilledTonalButton(onClick = {}, Modifier.padding(end = 8.dp)) {
            Text(
                text = item.menuId.toString(),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        Text(
            item.name, Modifier.weight(1f),
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
        Text(
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
            ).forEach { MenuListItem(it, true) }
        }
    }
}