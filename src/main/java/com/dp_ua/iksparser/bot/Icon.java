package com.dp_ua.iksparser.bot;

public enum Icon {
    START("🏁"),
    BACK("🔙"),
    NEXT("➡️"),
    PREVIOUS("⬅️"),
    CANCEL("❌"),
    CHECK("✅"),
    INFO("ℹ️"),
    WARNING("⚠️"),
    ERROR("❗"),
    QUESTION("❓"),
    URL("🌐"),
    CHAIN("🔗"),
    PLACE("📍"),
    AREA("🗺️"),
    CALENDAR("📅"),
    CLOCK("🕒"),

    ;

    private final String icon;

    Icon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return icon;
    }
}
