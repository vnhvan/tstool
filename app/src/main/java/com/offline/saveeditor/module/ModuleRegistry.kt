package com.offline.saveeditor.module

enum class ModuleStatus { VERIFIED, READ_ONLY, EXPERIMENTAL, BLOCKED }

data class EditorModule(
    val id: String,
    val title: String,
    val status: ModuleStatus,
    val description: String,
)

object ModuleRegistry {
    val catalog: ModuleCatalog = ModuleCatalog.Builder()
        .register(EditorModule("sound", "Sound Volume", ModuleStatus.VERIFIED, "Đã kiểm chứng bằng save thực và Township chấp nhận."))
        .register(EditorModule("coin", "Coin", ModuleStatus.EXPERIMENTAL, "Đọc và ghi biến money bằng worker tách riêng."))
        .register(EditorModule("game_start_date", "Ngày tạo tài khoản", ModuleStatus.EXPERIMENTAL, "Đọc và ghi gameStartDate theo Unix timestamp bằng worker tách riêng."))
        .register(EditorModule("tcash", "TCash", ModuleStatus.READ_ONLY, "Đọc được biến moneyCash; chưa bật ghi cho tới khi có mẫu riêng."))
        .register(EditorModule("cow_factory_slots", "Factory Slot", ModuleStatus.EXPERIMENTAL, "Đã thấy cowfactory.slotsCount; cần mẫu độc lập để tổng quát hóa."))
        .register(EditorModule("barn", "Barn / Warehouse", ModuleStatus.BLOCKED, "Cần xác minh integrity và các trường đồng bộ."))
        .build()

    val modules: List<EditorModule> get() = catalog.modules
}
