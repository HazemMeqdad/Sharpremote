package app.sharpremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.EnumMap;
import java.util.Map;

import app.sharpremote.ir.CodeSets;
import app.sharpremote.ir.IrCode;
import app.sharpremote.ir.RemoteKey;

/** The remote control screen, laid out like the Sharp 076K0VK011 remote. */
public class MainActivity extends Activity {

    private static final long REPEAT_DELAY_MS = 400;
    private static final long REPEAT_EVERY_MS = 160;

    private IrSender sender;
    private CodeStore store;
    private CodeSets.CodeSet set;
    private boolean editMode;

    private final Map<RemoteKey, Button> keys = new EnumMap<>(RemoteKey.class);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable repeater;

    private Spinner setPicker;
    private Button editToggle;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sender = new IrSender(this);
        store = new CodeStore(this);
        set = CodeSets.byId(store.getSetId());
        setContentView(buildScreen());
        refresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRepeat();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sender.shutdown();
    }

    // ------------------------------------------------------------------ layout

    private View buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Ui.BACKGROUND);
        scroll.setFillViewport(true);

        LinearLayout root = Ui.column(this, 14);
        scroll.addView(root);

        TextView brand = Ui.text(this, "SHARP", 24, Ui.TEXT);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setLetterSpacing(0.25f);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand);

        // Code set picker + "which set?" + edit toggle
        LinearLayout top = Ui.row(this);
        top.setPadding(0, Ui.dp(this, 8), 0, 0);
        setPicker = new Spinner(this);
        String[] names = new String[CodeSets.ALL.length];
        int selected = 0;
        for (int i = 0; i < names.length; i++) {
            names[i] = CodeSets.ALL[i].name;
            if (CodeSets.ALL[i] == set) selected = i;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setPicker.setAdapter(adapter);
        setPicker.setSelection(selected);
        setPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chooseSet(CodeSets.ALL[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        top.addView(setPicker, Ui.weighted(1));

        Button which = Ui.button(this, "Which set?");
        which.setOnClickListener(v -> testSet(0));
        top.addView(which);

        editToggle = Ui.button(this, "Edit");
        editToggle.setOnClickListener(v -> {
            editMode = !editMode;
            refresh();
        });
        top.addView(editToggle);
        root.addView(top);

        status = Ui.text(this, "", 13, Ui.MUTED);
        status.setPadding(Ui.dp(this, 4), Ui.dp(this, 6), Ui.dp(this, 4), Ui.dp(this, 10));
        root.addView(status);

        addKeys(root, 52, RemoteKey.POWER, RemoteKey.INFO, RemoteKey.AUDIO, RemoteKey.SUBTITLE);
        gap(root);
        addKeys(root, 54, RemoteKey.D1, RemoteKey.D2, RemoteKey.D3);
        addKeys(root, 54, RemoteKey.D4, RemoteKey.D5, RemoteKey.D6);
        addKeys(root, 54, RemoteKey.D7, RemoteKey.D8, RemoteKey.D9);
        addKeys(root, 54, RemoteKey.INPUT, RemoteKey.D0, RemoteKey.GUIDE);
        gap(root);
        addKeys(root, 58, RemoteKey.MENU, RemoteKey.UP, RemoteKey.EXIT);
        addKeys(root, 58, RemoteKey.LEFT, RemoteKey.OK, RemoteKey.RIGHT);
        addKeys(root, 58, RemoteKey.MUTE, RemoteKey.DOWN, RemoteKey.SIZE);
        gap(root);
        addKeys(root, 58, RemoteKey.VOL_UP, RemoteKey.FREEZE, RemoteKey.CH_UP);
        addKeys(root, 58, RemoteKey.VOL_DOWN, RemoteKey.SWAP, RemoteKey.CH_DOWN);
        gap(root);
        addKeys(root, 48, RemoteKey.TV, RemoteKey.MEDIA, RemoteKey.PVR, RemoteKey.FAVORITE);
        addKeys(root, 48, RemoteKey.REW, RemoteKey.PLAY, RemoteKey.FF, RemoteKey.REC);
        addKeys(root, 30, RemoteKey.RED, RemoteKey.GREEN, RemoteKey.YELLOW, RemoteKey.BLUE);

        TextView help = Ui.text(this,
                "Volume, channel and arrow buttons repeat while held. Dimmed buttons have no "
                        + "code in this set yet: tap Edit, then the button, to add one.",
                12, Ui.MUTED);
        help.setPadding(Ui.dp(this, 4), Ui.dp(this, 16), Ui.dp(this, 4), 0);
        root.addView(help);
        return scroll;
    }

    private void gap(LinearLayout root) {
        View v = new View(this);
        root.addView(v, new LinearLayout.LayoutParams(1, Ui.dp(this, 12)));
    }

    private void addKeys(LinearLayout root, int heightDp, RemoteKey... row) {
        LinearLayout line = Ui.row(this);
        for (RemoteKey k : row) {
            Button b = makeKey(k);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Ui.dp(this, heightDp), 1);
            int m = Ui.dp(this, 5);
            lp.setMargins(m, m, m, m);
            line.addView(b, lp);
            keys.put(k, b);
        }
        root.addView(line);
    }

    private Button makeKey(RemoteKey k) {
        Button b = new Button(this);
        b.setText(k.label);
        b.setAllCaps(false);
        b.setTextColor(Ui.TEXT);
        b.setPadding(0, 0, 0, 0);
        b.setStateListAnimator(null);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, k.label.length() > 3 ? 12 : 18);
        b.setBackground(Ui.keyBackground(this, keyColor(k), k == RemoteKey.OK ? 29 : 14));
        b.setOnTouchListener((v, e) -> {
            onKeyTouch(k, v, e);
            return false; // let the button show its pressed state and handle clicks
        });
        b.setOnClickListener(v -> onKeyClick(k));
        return b;
    }

    private static int keyColor(RemoteKey k) {
        switch (k) {
            case POWER:
            case REC:
            case RED:
                return Ui.RED;
            case GREEN:
                return Ui.GREEN;
            case YELLOW:
                return Ui.YELLOW;
            case BLUE:
                return Ui.BLUE;
            default:
                return Ui.KEY;
        }
    }

    // ------------------------------------------------------------------ behaviour

    private void onKeyTouch(RemoteKey k, View v, MotionEvent e) {
        if (editMode) return;
        int action = e.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            IrCode code = store.codeFor(set, k);
            if (code == null) return;
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            sender.send(code);
            if (k.repeats()) startRepeat(code);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            stopRepeat();
        }
    }

    private void onKeyClick(RemoteKey k) {
        if (editMode) {
            openEditor(k);
            return;
        }
        if (store.codeFor(set, k) == null) {
            toast("No code for " + k.title + " in this set yet. Tap Edit, then this button.");
        } else if (!sender.available()) {
            toast("This phone has no IR blaster.");
        }
    }

    private void startRepeat(IrCode code) {
        stopRepeat();
        repeater = new Runnable() {
            @Override
            public void run() {
                sender.send(code);
                handler.postDelayed(this, REPEAT_EVERY_MS);
            }
        };
        handler.postDelayed(repeater, REPEAT_DELAY_MS);
    }

    private void stopRepeat() {
        if (repeater != null) handler.removeCallbacks(repeater);
        repeater = null;
    }

    private void chooseSet(CodeSets.CodeSet newSet) {
        if (newSet == set) return;
        set = newSet;
        store.setSetId(set.id);
        refresh();
    }

    private void openEditor(RemoteKey k) {
        new CodeEditor(this, sender, k,
                store.codeFor(set, k),
                set.codes.get(k),
                store.customCode(set, k) != null,
                set.finderBase,
                code -> {
                    store.saveCustom(set, k, code);
                    refresh();
                    toast(code == null ? k.title + " reset to the built-in code"
                            : k.title + " saved: " + code.describe());
                }).show();
    }

    /** Walks through the code sets, sending MENU from each, until the TV reacts. */
    private void testSet(int index) {
        CodeSets.CodeSet candidate = CodeSets.ALL[index];
        IrCode menu = candidate.codes.get(RemoteKey.MENU);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Testing " + candidate.name)
                .setMessage("Point your phone at the TV and tap SEND.\n\nDid the TV's menu open?")
                .setPositiveButton("Yes, use it", (d, w) -> {
                    setPicker.setSelection(index);
                    chooseSet(candidate);
                    toast("Using " + candidate.name + ". Press EXIT to close the TV menu.");
                })
                .setNegativeButton(index + 1 < CodeSets.ALL.length ? "No, next set" : "No", (d, w) -> {
                    if (index + 1 < CodeSets.ALL.length) {
                        testSet(index + 1);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("No match yet")
                                .setMessage("None of the built-in sets opened the menu. Check the "
                                        + "phone is pointed at the TV, then try again. You can also "
                                        + "tap Edit and use \"Find code\" on any button.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                })
                .setNeutralButton("Send", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setOnClickListener(v -> {
                    if (!sender.available()) {
                        toast("This phone has no IR blaster.");
                    }
                    sender.send(menu);
                }));
        dialog.show();
    }

    private void refresh() {
        int withCode = 0;
        for (Map.Entry<RemoteKey, Button> e : keys.entrySet()) {
            boolean has = store.codeFor(set, e.getKey()) != null;
            if (has) withCode++;
            e.getValue().setAlpha(has || editMode ? 1f : 0.4f);
        }
        editToggle.setText(editMode ? "Done" : "Edit");
        if (!sender.available()) {
            status.setTextColor(Ui.RED);
            status.setText("No IR blaster found on this phone, so it can't control the TV. "
                    + "Phones with one include many Xiaomi, Redmi, Poco and Honor models.");
        } else if (editMode) {
            status.setTextColor(Color.rgb(120, 190, 255));
            status.setText("Edit mode: tap any button to change or find its code.");
        } else {
            status.setTextColor(Ui.MUTED);
            status.setText(withCode + " of " + keys.size() + " buttons have codes in this set.");
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
