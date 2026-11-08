package app.sharpremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import app.sharpremote.ir.CodeSets;
import app.sharpremote.ir.IrCode;
import app.sharpremote.ir.RemoteKey;

/** Dialog for setting one button's code by hand, testing it, or searching for it. */
final class CodeEditor {

    interface Listener {
        /** code == null means "go back to the built-in code". */
        void onCodeChosen(IrCode code);
    }

    private static final String[] PROTOCOL_NAMES = {"Sharp", "NEC", "NEC extended"};
    private static final IrCode.Protocol[] PROTOCOLS = {
            IrCode.Protocol.SHARP, IrCode.Protocol.NEC, IrCode.Protocol.NEC_EXT};

    private final Activity activity;
    private final IrSender sender;
    private final RemoteKey key;
    private final IrCode current;
    private final IrCode builtIn;
    private final boolean isCustom;
    private final IrCode finderBase;
    private final Listener listener;

    private Spinner protocol;
    private EditText address;
    private EditText command;

    CodeEditor(Activity activity, IrSender sender, RemoteKey key, IrCode current, IrCode builtIn,
               boolean isCustom, IrCode finderBase, Listener listener) {
        this.activity = activity;
        this.sender = sender;
        this.key = key;
        this.current = current;
        this.builtIn = builtIn;
        this.isCustom = isCustom;
        this.finderBase = finderBase;
        this.listener = listener;
    }

    void show() {
        LinearLayout body = Ui.column(activity, 20);

        String now = current == null ? "none"
                : current.describe() + (isCustom ? " (yours)" : " (built-in)");
        body.addView(Ui.text(activity, "Current code: " + now, 14, Ui.MUTED));

        body.addView(label("Protocol"));
        protocol = new Spinner(activity);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, PROTOCOL_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocol.setAdapter(adapter);
        body.addView(protocol);

        body.addView(label("Address"));
        address = Ui.numberField(activity);
        body.addView(address);

        body.addView(label("Command (0–255)"));
        command = Ui.numberField(activity);
        body.addView(command);

        fill(current != null ? current : finderBase);

        protocol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                IrCode.Protocol p = PROTOCOLS[position];
                int a = Ui.parseNumber(address.getText().toString());
                if (a < 0 || a > IrCode.maxAddress(p)) {
                    address.setText(format(p, defaultAddress(p), true));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        IrCode[] likely = CodeSets.suggestions(key);
        if (likely.length > 0) {
            body.addView(label("Likely codes: tap one, watch the TV, then Save"));
            LinearLayout line = null;
            for (int i = 0; i < likely.length; i++) {
                if (i % 2 == 0) {
                    line = Ui.row(activity);
                    body.addView(line);
                }
                IrCode c = likely[i];
                Button b = Ui.button(activity, "Try " + c.describe());
                b.setOnClickListener(v -> {
                    fill(c);
                    if (!sender.available()) toast("This phone has no IR blaster.");
                    sender.send(c);
                });
                line.addView(b, Ui.weighted(1));
            }
        }

        LinearLayout actions = Ui.row(activity);
        actions.setPadding(0, Ui.dp(activity, 12), 0, 0);
        Button test = Ui.button(activity, "Test");
        Button find = Ui.button(activity, "Find code…");
        actions.addView(test, Ui.weighted(1));
        actions.addView(find, Ui.weighted(1));
        body.addView(actions);

        body.addView(Ui.text(activity,
                "Numbers can be decimal (169) or hex (0xA9). Sharp TVs mostly use the Sharp "
                        + "protocol with address 1 or 17.", 12, Ui.MUTED));

        ScrollView scroll = new ScrollView(activity);
        scroll.addView(body);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(key.title)
                .setView(scroll)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null);
        if (isCustom && builtIn != null) {
            builder.setNeutralButton("Use built-in", (d, w) -> listener.onCodeChosen(null));
        } else if (isCustom) {
            builder.setNeutralButton("Remove", (d, w) -> listener.onCodeChosen(null));
        }
        AlertDialog dialog = builder.create();

        test.setOnClickListener(v -> {
            IrCode code = read();
            if (code != null) {
                if (!sender.available()) toast("This phone has no IR blaster.");
                sender.send(code);
            }
        });
        find.setOnClickListener(v -> {
            IrCode code = read();
            if (code == null) return;
            dialog.dismiss();
            new CodeFinder(activity, sender, key, code, listener::onCodeChosen).show();
        });
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    IrCode code = read();
                    if (code != null) {
                        listener.onCodeChosen(code);
                        dialog.dismiss();
                    }
                }));
        dialog.show();
    }

    private void fill(IrCode c) {
        protocol.setSelection(indexOf(c.protocol));
        address.setText(format(c.protocol, c.address, true));
        command.setText(format(c.protocol, c.command, false));
    }

    /** Reads the fields; shows a message and returns null if something is out of range. */
    private IrCode read() {
        IrCode.Protocol p = PROTOCOLS[protocol.getSelectedItemPosition()];
        int a = Ui.parseNumber(address.getText().toString());
        int c = Ui.parseNumber(command.getText().toString());
        if (a < 0 || a > IrCode.maxAddress(p)) {
            toast("Address must be 0–" + IrCode.maxAddress(p) + " for this protocol.");
            return null;
        }
        if (c < 0 || c > IrCode.maxCommand(p)) {
            toast("Command must be 0–255.");
            return null;
        }
        return new IrCode(p, a, c);
    }

    private TextView label(String s) {
        TextView t = Ui.text(activity, s, 13, Ui.MUTED);
        t.setPadding(0, Ui.dp(activity, 12), 0, 0);
        return t;
    }

    private void toast(String s) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
    }

    private static int indexOf(IrCode.Protocol p) {
        for (int i = 0; i < PROTOCOLS.length; i++) if (PROTOCOLS[i] == p) return i;
        return 0;
    }

    private static int defaultAddress(IrCode.Protocol p) {
        switch (p) {
            case SHARP: return 1;
            case NEC: return 0;
            default: return 0x7F00;
        }
    }

    static String format(IrCode.Protocol p, int value, boolean isAddress) {
        if (p == IrCode.Protocol.SHARP) return String.valueOf(value);
        if (p == IrCode.Protocol.NEC_EXT && isAddress) return String.format("0x%04X", value);
        return String.format("0x%02X", value);
    }
}
