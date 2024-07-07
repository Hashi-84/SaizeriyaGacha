package jp.hashi.menuarranger

import android.content.res.AssetManager
import android.util.Log
import android.view.Menu
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.hashi.menuarranger.model.MenuCategory
import jp.hashi.menuarranger.model.MenuItem
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class MenuViewModel(private val assetManager: AssetManager) : ViewModel() {
    private lateinit var menuItems: List<MenuItem>
    val chosenMenuItems = MutableLiveData<List<MenuItem>>(listOf())
    private val categories: List<MenuCategory> = getCategoriesFromJson()
    fun getCategories(): List<MenuCategory> = categories

    // LiveDataの定義
    private val _alcoholContains = MutableLiveData(true)
    val alcoholContains: LiveData<Boolean> = _alcoholContains
    fun setAlcoholContains(containsAlcohol: Boolean) { _alcoholContains.value = containsAlcohol }

    private val _kidsContains = MutableLiveData(true)
    val kidsContains: LiveData<Boolean> = _kidsContains
    fun setKidsContains(containsKids: Boolean) { _kidsContains.value = containsKids }

    private val _ceilingPrice = MutableLiveData(1000)
    val ceilingPrice: LiveData<Int> = _ceilingPrice
    fun setCeilingPrice(ceilingPrice: Int) { _ceilingPrice.value = ceilingPrice }

    private var _showMenuId = MutableLiveData(true)
    val showMenuId: LiveData<Boolean> = _showMenuId
    fun setShowMenuId(showMenuId: Boolean) { _showMenuId.value = showMenuId }

    private val _chosenCategories = MutableLiveData(categories)
    val chosenCategories: LiveData<List<MenuCategory>> = _chosenCategories
    fun changeCategoryChecked(category: MenuCategory) {
        chosenCategories.value?.toMutableList()?.let {
            if (it.contains(category)) {
                it.remove(category)
            } else {
                it.add(category)
            }
            _chosenCategories.value = it.toList()
        }
    }

    init {
        getMenuItemsFromJson()
        getCategoriesFromJson()
        chooseMenuItems(1000)
    }

    fun onChooseBtnClick(ceilingPrice: Int, snackbar: (String) -> Unit) {
        if (ceilingPrice > 1000000) {
            chosenMenuItems.value = listOf()
            snackbar("上限金額は100万円以下にしてください")
        } else chooseMenuItems(ceilingPrice)
    }

    private fun chooseMenuItems(ceilingPrice: Int) {
        var balance = ceilingPrice
        val lowestPrice: Int = menuItems
                                                    .minByOrNull { it.price }!!.price
        val selectedMenuItems = mutableListOf<MenuItem>()
        while (lowestPrice <= balance) {
            val selectedMenu = menuItems.filter { menuItem ->
                                                        chosenCategories.value?.let { cc ->
                                                            menuItem.category in (cc.map { it.category })
                                                        } ?: true
                                                    }.filter { it.price <= balance }
                                                    .random()
            balance -= selectedMenu.price
            selectedMenuItems.add(selectedMenu)
        }
        chosenMenuItems.value =
            selectedMenuItems
            .toList()
            .sortedBy { it.menuId }
    }

    private fun getCategoryArray(): Array<String> {
        val chosenCategories = categories.map { it.category }.toMutableList()
        if (!alcoholContains.value!!) chosenCategories.remove("alcohol")
        if (!kidsContains.value!!) chosenCategories.removeIf { it.contains("kids") }
        return chosenCategories.toTypedArray()
    }

    private fun getMenuItemsFromJson() {
        val jsonString = try {
            assetManager.open( "saizeriya_menu.json").bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        menuItems = Json.decodeFromString<List<MenuItem>>(ListSerializer(MenuItem.serializer()), jsonString)
            .filter{ it.isAvailable }
//        Log.d("TEST", menuItems.toString())
    }

    private fun getCategoriesFromJson(): List<MenuCategory> {
        val jsonString = try {
            assetManager.open( "saizeriya_categories.json").bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            return listOf()
        }

        return Json.decodeFromString<List<MenuCategory>>(ListSerializer(MenuCategory.serializer()), jsonString)
    }
}