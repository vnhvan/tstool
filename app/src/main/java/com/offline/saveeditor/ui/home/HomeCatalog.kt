package com.offline.saveeditor.ui.home

enum class FeatureState { AVAILABLE, EXPERIMENTAL, COMING_SOON }

data class HomeFeature(
    val id: String,
    val title: String,
    val subtitle: String,
    val symbol: String,
    val state: FeatureState = FeatureState.COMING_SOON,
)

data class HomeCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val symbol: String,
    val features: List<HomeFeature>,
)

object HomeCatalog {
    val categories = listOf(
        HomeCategory("currency", "Currency", "Tiền tệ và phần thưởng", "¤", listOf(
            HomeFeature("coin", "Coin", "Thay đổi số dư Coin", "●", FeatureState.EXPERIMENTAL),
            HomeFeature("tcash", "TCash", "Quản lý TCash", "◆"),
            HomeFeature("helicopter", "Helicopter", "Phần thưởng trực thăng", "✈"),
        )),
        HomeCategory("storage", "Barn & Storage", "Kho, vật phẩm và dung lượng", "▣", listOf(
            HomeFeature("barn_capacity", "Barn Capacity", "Dung lượng kho", "▤"),
            HomeFeature("barn_items", "Barn Items", "Vật phẩm trong kho", "▥"),
            HomeFeature("warehouse", "Warehouse", "Integrity và WHUdup", "▦"),
        )),
        HomeCategory("mining", "Mining", "Mỏ, quặng và công cụ", "⛏", listOf(
            HomeFeature("mine_depth", "Mine Depth", "Độ sâu mỏ", "↧"),
            HomeFeature("ore", "Ore", "Quặng khai thác", "⬟"),
            HomeFeature("ingot", "Ingot", "Thỏi kim loại", "▰"),
            HomeFeature("mining_tools", "Mining Tools", "Pick, TNT, drill...", "✦"),
        )),
        HomeCategory("customization", "Customization", "Trang trí và diện mạo", "✿", listOf(
            HomeFeature("decor", "Decoration", "Danh mục trang trí", "❖"),
            HomeFeature("skin", "Building Skin", "Skin công trình", "▧"),
            HomeFeature("sticker", "Sticker", "Bộ sưu tập sticker", "✪"),
            HomeFeature("townsign", "Townsign", "Biển tên thành phố", "⚑"),
        )),
        HomeCategory("profile", "Profile", "Hồ sơ người chơi", "★", listOf(
            HomeFeature("badge", "Badge", "Huy hiệu", "✹"),
            HomeFeature("title", "Title", "Danh hiệu", "≡"),
            HomeFeature("frame", "Frame", "Khung đại diện", "▢"),
            HomeFeature("style", "Style", "Phong cách hồ sơ", "◈"),
        )),
        HomeCategory("zoo", "Zoo", "Thẻ và bộ bài sở thú", "♞", listOf(
            HomeFeature("zoo_cards", "Zoo Cards", "Thẻ động vật", "♣"),
            HomeFeature("zoo_deck", "Zoo Deck", "Bộ bài sở thú", "▨"),
            HomeFeature("coupon", "Coupon", "Phiếu và coupon", "◇"),
        )),
        HomeCategory("events", "Events", "Sự kiện và mùa giải", "⚡", listOf(
            HomeFeature("regatta", "Regatta", "Regatta và VIP", "⚓"),
            HomeFeature("season", "Season Pass", "Mùa giải", "☀"),
            HomeFeature("golden_ticket", "Golden Ticket", "Vé vàng", "✧"),
            HomeFeature("perk", "Perk & Booster", "Perk, booster, lab booster", "⬆"),
        )),
        HomeCategory("world", "Town & World", "Mở rộng thành phố", "⌘", listOf(
            HomeFeature("terrain", "Terrain", "Mở khóa đất", "▱"),
            HomeFeature("airport", "Airport", "Sân bay", "✈"),
            HomeFeature("community", "Community", "Cộng đồng", "◎"),
            HomeFeature("expansion", "Expansion", "Mở rộng", "＋"),
        )),
    )
    val allFeatures get() = categories.flatMap { it.features }
}
