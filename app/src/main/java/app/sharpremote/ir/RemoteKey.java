package app.sharpremote.ir;

/** Every button on the Sharp 076K0VK011 remote that the app shows. */
public enum RemoteKey {
    POWER("⏻", "Power"),
    INFO("i", "Info"),
    AUDIO("AUDIO I/II", "Audio I/II"),
    SUBTITLE("SUB", "Subtitle"),

    D1("1", "1"), D2("2", "2"), D3("3", "3"),
    D4("4", "4"), D5("5", "5"), D6("6", "6"),
    D7("7", "7"), D8("8", "8"), D9("9", "9"),
    INPUT("INPUT", "Input select"), D0("0", "0"), GUIDE("EPG", "Guide / EPG"),

    MENU("MENU", "Menu"), UP("▲", "Up"), EXIT("EXIT", "Exit"),
    LEFT("◀", "Left"), OK("ENTER", "Enter / CH list"), RIGHT("▶", "Right"),
    MUTE("🔇", "Mute"), DOWN("▼", "Down"), SIZE("SIZE", "Picture size"),

    VOL_UP("VOL +", "Volume +"), FREEZE("FREEZE", "Freeze"), CH_UP("P +", "Channel +"),
    VOL_DOWN("VOL −", "Volume −"), SWAP("P↔P", "Previous channel"), CH_DOWN("P −", "Channel −"),

    TV("TV", "TV"), MEDIA("MEDIA", "Media"), PVR("PVR", "PVR"), FAVORITE("FAV", "Favorite"),
    REW("◀◀", "Rewind"), PLAY("▶❚❚", "Play / pause"), FF("▶▶", "Fast forward"), REC("●", "Record"),
    RED("", "Red"), GREEN("", "Green"), YELLOW("", "Yellow"), BLUE("", "Blue");

    public final String label;
    public final String title;

    RemoteKey(String label, String title) {
        this.label = label;
        this.title = title;
    }

    /** Keys that keep sending while held down. */
    public boolean repeats() {
        return this == VOL_UP || this == VOL_DOWN || this == CH_UP || this == CH_DOWN
                || this == UP || this == DOWN || this == LEFT || this == RIGHT;
    }
}
