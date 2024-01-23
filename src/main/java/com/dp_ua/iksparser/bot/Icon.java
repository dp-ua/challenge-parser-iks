package com.dp_ua.iksparser.bot;

public enum Icon {
    BIRTHDAY("🎂"),
    ATHLETE("🏃"),
    MARK("🏷️"),
    FIND("🔍"),
    LOOK("👀"),
    EVENT("📅"),
    HEAT("🔥"),
    COMPETITION("🏆"),
    CHAMPIONSHIP("🏆"),
    CHALLENGE("🎯"),
    SPORT("🏀"),
    TEAM("👥"),
    PERSON("👤"),
    PLACE_OF_EVENT("📍"),
    PLACE_OF_TRAINING("🏟️"),
    PLACE_OF_LIVING("🏠"),
    PLACE_OF_WORK("🏢"),
    PLACE_OF_STUDY("🏫"),
    PLACE_OF_REST("🏖️"),
    PAGE("📄"),
    PAGE_WITH_CURL("📃"),
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
    ONE("1️⃣"),
    TWO("2️⃣"),
    THREE("3️⃣"),
    FOUR("4️⃣"),
    FIVE("5️⃣"),
    SIX("6️⃣"),
    SEVEN("7️⃣"),
    EIGHT("8️⃣"),
    NUMBER("🔢"),
    NINE("9️⃣"),
    TEN("🔟"),
    ZERO("0️⃣"),
    ;

    public static Icon getIconForNumber(int number) {
        if (number < 0 || number > 9) throw new IllegalArgumentException("Number must be between 0 and 9");
        switch (number) {
            case 0:
                return ZERO;
            case 1:
                return ONE;
            case 2:
                return TWO;
            case 3:
                return THREE;
            case 4:
                return FOUR;
            case 5:
                return FIVE;
            case 6:
                return SIX;
            case 7:
                return SEVEN;
            case 8:
                return EIGHT;
            case 9:
                return NINE;
            default:
                return NUMBER;
        }
    }

    private final String icon;

    Icon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return icon;
    }
}
