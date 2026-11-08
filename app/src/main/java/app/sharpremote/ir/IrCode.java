package app.sharpremote.ir;

/**
 * One infrared command: a protocol plus an address and a command number.
 * Pure Java (no Android classes) so it can be tested anywhere.
 */
public final class IrCode {

    public enum Protocol {
        /** Sharp / Denon protocol: 5-bit address, 8-bit command, sent twice (second time inverted). */
        SHARP,
        /** Standard NEC: 8-bit address + inverse, 8-bit command + inverse. */
        NEC,
        /** Extended NEC: 16-bit address, 8-bit command + inverse. */
        NEC_EXT
    }

    public final Protocol protocol;
    public final int address;
    public final int command;

    public IrCode(Protocol protocol, int address, int command) {
        this.protocol = protocol;
        this.address = address;
        this.command = command;
    }

    public static IrCode sharp(int address, int command) {
        return new IrCode(Protocol.SHARP, address, command);
    }

    public static IrCode nec(int address, int command) {
        return new IrCode(Protocol.NEC, address, command);
    }

    public static IrCode necExt(int address, int command) {
        return new IrCode(Protocol.NEC_EXT, address, command);
    }

    public IrCode withCommand(int newCommand) {
        return new IrCode(protocol, address, newCommand);
    }

    /** Carrier frequency in Hz. */
    public int frequency() {
        return 38000;
    }

    /** On/off durations in microseconds, starting with "on". */
    public int[] pattern() {
        switch (protocol) {
            case SHARP:
                return IrEncoder.sharp(address, command);
            case NEC:
                return IrEncoder.nec(address, command);
            case NEC_EXT:
            default:
                return IrEncoder.necExt(address, command);
        }
    }

    /** Serialised form used for saving, e.g. "SHARP,1,169". */
    public String serialize() {
        return protocol.name() + "," + address + "," + command;
    }

    public static IrCode parse(String s) {
        if (s == null) return null;
        String[] p = s.split(",");
        if (p.length != 3) return null;
        try {
            return new IrCode(Protocol.valueOf(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Human-readable form, e.g. "Sharp 1 / 169" or "NEC-ext 0x7F00 / 0x0A". */
    public String describe() {
        switch (protocol) {
            case SHARP:
                return "Sharp " + address + " / " + command;
            case NEC:
                return String.format("NEC 0x%02X / 0x%02X", address, command);
            case NEC_EXT:
            default:
                return String.format("NEC-ext 0x%04X / 0x%02X", address, command);
        }
    }

    /** Largest valid command / address values for each protocol. */
    public static int maxAddress(Protocol p) {
        switch (p) {
            case SHARP: return 31;
            case NEC: return 255;
            default: return 65535;
        }
    }

    public static int maxCommand(Protocol p) {
        return 255;
    }
}
