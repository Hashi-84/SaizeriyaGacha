package jp.hashi.menuarranger

import android.content.res.AssetManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.hashi.menuarranger.model.MenuItem
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.math.floor
import kotlin.math.roundToInt

class MenuViewModel(private val assetManager: AssetManager) : ViewModel() {
    private lateinit var menuItems: List<MenuItem>
    val chosenMenuItems = MutableLiveData<List<MenuItem>>(listOf())
    private val alcoholContains = MutableLiveData(true)
    private lateinit var categories: Array<String>
    private val kidsContains = MutableLiveData(true)

    init {
        getMenuItemsFromJson()
        chooseMenuItems(1000)
    }

    fun chooseMenuItems(ceilingPrice: Int) {
        categories = getCategoryArray()
        var balance = ceilingPrice
        val lowestPrice: Int = menuItems
                                                    .minByOrNull { it.price }!!.price
        val selectedMenuItems = mutableListOf<MenuItem>()
        while (lowestPrice <= balance) {
            val selectedMenu = menuItems.filter { it.category in (categories) }
                                                    .filter { it.price <= balance }
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
        val categories = menuItems.map { it.category }
            .distinct().toMutableList()
        if (!alcoholContains.value!!) categories.remove("alcohol")
        if (!kidsContains.value!!) categories.removeIf { it.contains("kids") }
        return categories.toTypedArray()
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
        Log.d("TEST", menuItems.toString())
    }

    fun setAlcohol(containsAlcohol: Boolean) { alcoholContains.value = containsAlcohol }
    fun setKids(containsKids: Boolean) { kidsContains.value = containsKids }
}