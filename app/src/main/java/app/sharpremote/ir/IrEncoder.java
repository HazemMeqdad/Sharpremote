package app.sharpremote.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns protocol + address + command into a pulse pattern (microseconds)
 * that Android's ConsumerIrManager can transmit.
 */
public final class IrEncoder {

    private IrEncoder() {}

    // ---- Sharp protocol (timings match captures of real Sharp remotes) ----
    private static final int SHARP_MARK = 320;
    private static final int SHARP_ZERO = 680;   // space for a 0 bit (1 ms period)
    private static final int SHARP_ONE = 1680;   // space for a 1 bit (2 ms period)
    private static final int SHARP_GAP = 46000;  // pause between the two frames

    /**
     * Sharp frame: 5 address bits, 8 command bits, expansion bit, check bit (LSB first).
     * The message is sent twice: normal (exp=1, chk=0) then with the command and
     * the two flag bits inverted (exp=0, chk=1).
     */
    public static int[] sharp(int address, int command) {
        List<Integer> out = new ArrayList<>();
        sharpFrame(out, address, command & 0xFF, 1, 0);
        out.add(SHARP_GAP);
        sharpFrame(out, address, (~command) & 0xFF, 0, 1);
        out.add(SHARP_GAP);
        return toArray(out);
    }

    private static void sharpFrame(List<Integer> out, int address, int command, int exp, int chk) {
        for (int i = 0; i < 5; i++) sharpBit(out, (address >> i) & 1);
        for (int i = 0; i < 8; i++) sharpBit(out, (command >> i) & 1);
        sharpBit(out, exp);
        sharpBit(out, chk);
        out.add(SHARP_MARK); // trailing mark ends the last bit
    }

    private static void sharpBit(List<Integer> out, int bit) {
        out.add(SHARP_MARK);
        out.add(bit == 1 ? SHARP_ONE : SHARP_ZERO);
    }

    // ---- NEC protocol ----
    private static final int NEC_LEAD_MARK = 9000;
    private static final int NEC_LEAD_SPACE = 4500;
    private static final int NEC_MARK = 562;
    private static final int NEC_ZERO = 562;
    private static final int NEC_ONE = 1687;
    private static final int NEC_FRAME = 108000; // full frame length incl. trailing gap

    public static int[] nec(int address, int command) {
        int a = address & 0xFF;
        return necBytes(a, (~a) & 0xFF, command & 0xFF, (~command) & 0xFF);
    }

    /** Extended NEC: 16-bit address sent low byte first. */
    public static int[] necExt(int address, int command) {
        return necBytes(address & 0xFF, (address >> 8) & 0xFF, command & 0xFF, (~command) & 0xFF);
    }

    private static int[] necBytes(int b0, int b1, int b2, int b3) {
        List<Integer> out = new ArrayList<>();
        out.add(NEC_LEAD_MARK);
        out.add(NEC_LEAD_SPACE);
        int[] bytes = {b0, b1, b2, b3};
        for (int b : bytes) {
            for (int i = 0; i < 8; i++) {
                out.add(NEC_MARK);
                out.add(((b >> i) & 1) == 1 ? NEC_ONE : NEC_ZERO);
            }
        }
        out.add(NEC_MARK);
        int used = 0;
        for (int v : out) used += v;
        out.add(Math.max(NEC_FRAME - used, 20000));
        return toArray(out);
    }

    private static int[] toArray(List<Integer> list) {
        int[] r = new int[list.size()];
        for (int i = 0; i < r.length; i++) r[i] = list.get(i);
        return r;
    }
}
