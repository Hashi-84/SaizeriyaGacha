package jp.hashi.menuarranger.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
* メニュー情報を格納するデータクラス。
 * @param id ユニークな数字
 * @param menuId 注文時に指定する番号(重複する場合あり)
 * @param name 商品名
 * @param price 商品の税抜価格
 * @param area 販売地域(全国: national / 北海道: hokkaido / 埼玉: saitama)
 * @param category 商品のカテゴリ
 * @param isAvailable 販売中のメニューであるか
 **/
@Serializable
data class MenuItem(
    val id: Int,
    val menuId: Int,
    val name: String,
    val price: Int,
    val area: Array<String>,
    val category: String,
    val isAvailable: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MenuItem

        if (id != other.id) return false
        if (menuId != other.menuId) return false
        if (name != other.name) return false
        if (price != other.price) return false
        if (!area.contentEquals(other.area)) return false
        if (category != other.category) return false
        if (isAvailable != other.isAvailable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + menuId
        result = 31 * result + name.hashCode()
        result = 31 * result + price
        result = 31 * result + area.contentHashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + isAvailable.hashCode()
        return result
    }
}