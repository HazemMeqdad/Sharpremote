package app.sharpremote.ir;

import java.util.EnumMap;
import java.util.Map;

/**
 * Built-in Sharp code sets. Sharp has used several IR code families over the
 * years, so the app ships three and lets you pick the one your TV answers to.
 *
 * Sources: Flipper-IRDB (TVs/Sharp: Sharp_LC-RC1-16, Sharp_TV2, Aquos) and
 * probonopd/irdb (Sharp TV, Sharp protocol device 1). Any button missing here
 * can be filled in from the app with "Edit" + "Find code".
 */
public final class CodeSets {

    private CodeSets() {}

    public static final class CodeSet {
        public final String id;
        public final String name;
        public final Map<RemoteKey, IrCode> codes;
        /** Protocol/address the code finder starts from for this set. */
        public final IrCode finderBase;

        CodeSet(String id, String name, Map<RemoteKey, IrCode> codes, IrCode finderBase) {
            this.id = id;
            this.name = name;
            this.codes = codes;
            this.finderBase = finderBase;
        }
    }

    public static final CodeSet[] ALL = {classicSharp(), dvbSharp(), necSharp()};

    public static CodeSet byId(String id) {
        for (CodeSet s : ALL) if (s.id.equals(id)) return s;
        return ALL[0];
    }

    /** Set 1: classic Sharp LCD/LED TVs (Sharp protocol, address 1 and 17). */
    private static CodeSet classicSharp() {
        Map<RemoteKey, IrCode> m = new EnumMap<>(RemoteKey.class);
        m.put(RemoteKey.POWER, IrCode.sharp(1, 22));
        m.put(RemoteKey.INFO, IrCode.sharp(1, 27));
        m.put(RemoteKey.AUDIO, IrCode.sharp(1, 24));
        digits(m);
        m.put(RemoteKey.INPUT, IrCode.sharp(1, 19));
        m.put(RemoteKey.MENU, IrCode.sharp(1, 32));
        m.put(RemoteKey.EXIT, IrCode.sharp(1, 243));
        m.put(RemoteKey.UP, IrCode.sharp(1, 87));
        m.put(RemoteKey.DOWN, IrCode.sharp(1, 88));
        m.put(RemoteKey.LEFT, IrCode.sharp(1, 245));
        m.put(RemoteKey.RIGHT, IrCode.sharp(1, 246));
        m.put(RemoteKey.OK, IrCode.sharp(1, 247));
        m.put(RemoteKey.MUTE, IrCode.sharp(1, 23));
        m.put(RemoteKey.SIZE, IrCode.sharp(1, 160));
        m.put(RemoteKey.VOL_UP, IrCode.sharp(1, 20));
        m.put(RemoteKey.VOL_DOWN, IrCode.sharp(1, 21));
        m.put(RemoteKey.CH_UP, IrCode.sharp(1, 17));
        m.put(RemoteKey.CH_DOWN, IrCode.sharp(1, 18));
        m.put(RemoteKey.SWAP, IrCode.sharp(1, 47));
        m.put(RemoteKey.FAVORITE, IrCode.sharp(17, 169));
        m.put(RemoteKey.REW, IrCode.sharp(17, 196));
        m.put(RemoteKey.PLAY, IrCode.sharp(17, 194));
        m.put(RemoteKey.FF, IrCode.sharp(17, 197));
        return new CodeSet("sharp_classic", "Set 1 – Sharp classic", m, IrCode.sharp(1, 0));
    }

    /**
     * Set 2: Sharp DVB TVs (Asia / Middle East / Europe models such as LC-40LE820X,
     * remotes GA857WJSA and GA538WJSA). Codes from Flipper-IRDB _Converted_/IR_Plus/S/SHARP.
     */
    private static CodeSet dvbSharp() {
        Map<RemoteKey, IrCode> m = new EnumMap<>(RemoteKey.class);
        m.put(RemoteKey.POWER, IrCode.sharp(1, 22));
        m.put(RemoteKey.INFO, IrCode.sharp(1, 27));
        m.put(RemoteKey.AUDIO, IrCode.sharp(1, 24));
        m.put(RemoteKey.SUBTITLE, IrCode.sharp(30, 159));
        digits(m);
        m.put(RemoteKey.INPUT, IrCode.sharp(1, 19));
        m.put(RemoteKey.GUIDE, IrCode.sharp(17, 163));
        m.put(RemoteKey.MENU, IrCode.sharp(17, 198));
        m.put(RemoteKey.OK, IrCode.sharp(1, 82));
        m.put(RemoteKey.UP, IrCode.sharp(1, 87));
        m.put(RemoteKey.DOWN, IrCode.sharp(1, 32));
        m.put(RemoteKey.LEFT, IrCode.sharp(1, 215));
        m.put(RemoteKey.RIGHT, IrCode.sharp(1, 216));
        m.put(RemoteKey.EXIT, IrCode.sharp(1, 228));
        m.put(RemoteKey.MUTE, IrCode.sharp(1, 23));
        m.put(RemoteKey.SIZE, IrCode.sharp(17, 200));
        m.put(RemoteKey.VOL_UP, IrCode.sharp(1, 20));
        m.put(RemoteKey.VOL_DOWN, IrCode.sharp(1, 21));
        m.put(RemoteKey.CH_UP, IrCode.sharp(1, 17));
        m.put(RemoteKey.CH_DOWN, IrCode.sharp(1, 18));
        m.put(RemoteKey.FREEZE, IrCode.sharp(17, 211));
        m.put(RemoteKey.SWAP, IrCode.sharp(1, 39));
        m.put(RemoteKey.TV, IrCode.sharp(17, 160));
        m.put(RemoteKey.REW, IrCode.sharp(17, 3));
        m.put(RemoteKey.PLAY, IrCode.sharp(17, 1));
        m.put(RemoteKey.FF, IrCode.sharp(17, 4));
        m.put(RemoteKey.REC, IrCode.sharp(17, 5));
        m.put(RemoteKey.RED, IrCode.sharp(1, 72));
        m.put(RemoteKey.GREEN, IrCode.sharp(1, 73));
        m.put(RemoteKey.YELLOW, IrCode.sharp(1, 74));
        m.put(RemoteKey.BLUE, IrCode.sharp(1, 75));
        return new CodeSet("sharp_dvb", "Set 2 – Sharp DVB", m, IrCode.sharp(17, 160));
    }

    /**
     * Likely codes for buttons no database lists for your exact remote. Shown as
     * one-tap "Try" buttons in the editor.
     * FAVORITE: 17/169 (Sharp LC-RC1-16), 17/143 (LIRC file for Sharp remote
     * 845-039-40B0), 1/13 (irdb Sharp TV), 17/98 (Aquos FAV CH), and 17/165, 17/166,
     * the unused slots in the DTV button block (17/160–168) this TV family uses.
     */
    public static IrCode[] suggestions(RemoteKey key) {
        switch (key) {
            case FAVORITE:
                return new IrCode[]{IrCode.sharp(17, 169), IrCode.sharp(17, 143),
                        IrCode.sharp(1, 13), IrCode.sharp(17, 98),
                        IrCode.sharp(17, 165), IrCode.sharp(17, 166)};
            case MEDIA:
            case PVR:
                return new IrCode[]{IrCode.sharp(17, 165), IrCode.sharp(17, 166),
                        IrCode.sharp(17, 169), IrCode.sharp(17, 7)};
            default:
                return new IrCode[0];
        }
    }

    /** Set 3: newer Sharp Aquos made on NEC-protocol chassis. */
    private static CodeSet necSharp() {
        Map<RemoteKey, IrCode> m = new EnumMap<>(RemoteKey.class);
        int a = 0x7F00;
        m.put(RemoteKey.POWER, IrCode.necExt(a, 0x0A));
        m.put(RemoteKey.INFO, IrCode.necExt(a, 0x0B));
        m.put(RemoteKey.INPUT, IrCode.necExt(a, 0x53));
        m.put(RemoteKey.MENU, IrCode.necExt(a, 0x57));
        m.put(RemoteKey.EXIT, IrCode.necExt(a, 0x4E));
        m.put(RemoteKey.UP, IrCode.necExt(a, 0x5E));
        m.put(RemoteKey.DOWN, IrCode.necExt(a, 0x56));
        m.put(RemoteKey.LEFT, IrCode.necExt(a, 0x5B));
        m.put(RemoteKey.RIGHT, IrCode.necExt(a, 0x58));
        m.put(RemoteKey.OK, IrCode.necExt(a, 0x5A));
        m.put(RemoteKey.MUTE, IrCode.necExt(a, 0x50));
        m.put(RemoteKey.VOL_UP, IrCode.necExt(a, 0x1E));
        m.put(RemoteKey.VOL_DOWN, IrCode.necExt(a, 0x5F));
        m.put(RemoteKey.CH_UP, IrCode.necExt(a, 0x1F));
        m.put(RemoteKey.CH_DOWN, IrCode.necExt(a, 0x5C));
        return new CodeSet("sharp_nec", "Set 3 – Sharp Aquos (NEC)", m, IrCode.necExt(a, 0));
    }

    private static void digits(Map<RemoteKey, IrCode> m) {
        RemoteKey[] d = {RemoteKey.D1, RemoteKey.D2, RemoteKey.D3, RemoteKey.D4, RemoteKey.D5,
                RemoteKey.D6, RemoteKey.D7, RemoteKey.D8, RemoteKey.D9};
        for (int i = 0; i < d.length; i++) m.put(d[i], IrCode.sharp(1, i + 1));
        m.put(RemoteKey.D0, IrCode.sharp(1, 10));
    }
}
