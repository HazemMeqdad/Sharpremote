package app.sharpremote;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import app.sharpremote.ir.IrCode;
import app.sharpremote.ir.RemoteKey;

/**
 * Steps through all 256 commands on one protocol/address so you can find the
 * code for a button the built-in sets don't know (for example FAVORITE).
 */
final class CodeFinder {

    interface Listener {
        void onFound(IrCode code);
    }

    private static final long AUTO_STEP_MS = 2000;

    private final Activity activity;
    private final IrSender sender;
    private final RemoteKey key;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private IrCode code;
    private boolean auto;
    private TextView codeLabel;
    private TextView counter;
    private Button autoButton;

    private final Runnable autoStep = new Runnable() {
        @Override
        public void run() {
            step(1);
            handler.postDelayed(this, AUTO_STEP_MS);
        }
    };

    CodeFinder(Activity activity, IrSender sender, RemoteKey key, IrCode start, Listener listener) {
        this.activity = activity;
        this.sender = sender;
        this.key = key;
        this.code = start;
        this.listener = listener;
    }

    void show() {
        LinearLayout body = Ui.column(activity, 20);
        body.addView(Ui.text(activity,
                "Point the phone at the TV. Tap Next to try each code. When the TV does what "
                        + key.title + " should do, tap \"It worked\".\n\nSome codes may open other "
                        + "menus or change inputs; press EXIT if that happens. To search a different "
                        + "address, change it in the previous screen first.",
                14, Ui.MUTED));

        codeLabel = Ui.text(activity, "", 22, Ui.TEXT);
        codeLabel.setTypeface(Typeface.DEFAULT_BOLD);
        codeLabel.setGravity(Gravity.CENTER);
        codeLabel.setPadding(0, Ui.dp(activity, 18), 0, Ui.dp(activity, 2));
        body.addView(codeLabel);

        counter = Ui.text(activity, "", 13, Ui.MUTED);
        counter.setGravity(Gravity.CENTER);
        body.addView(counter);

        LinearLayout nav = Ui.row(activity);
        nav.setPadding(0, Ui.dp(activity, 14), 0, 0);
        Button prev = Ui.button(activity, "◀ Prev");
        Button again = Ui.button(activity, "Send");
        Button next = Ui.button(activity, "Next ▶");
        nav.addView(prev, Ui.weighted(1));
        nav.addView(again, Ui.weighted(1));
        nav.addView(next, Ui.weighted(1));
        body.addView(nav);

        autoButton = Ui.button(activity, "");
        body.addView(autoButton);

        prev.setOnClickListener(v -> { stopAuto(); step(-1); });
        next.setOnClickListener(v -> { stopAuto(); step(1); });
        again.setOnClickListener(v -> send());
        autoButton.setOnClickListener(v -> {
            if (auto) {
                stopAuto();
            } else {
                auto = true;
                updateLabels();
                handler.post(autoStep);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Find code: " + key.title)
                .setView(body)
                .setPositiveButton("It worked – save", (d, w) -> listener.onFound(code))
                .setNegativeButton("Close", null)
                .create();
        dialog.setOnDismissListener(d -> stopAuto());
        updateLabels();
        dialog.show();
    }

    private void step(int delta) {
        int c = (code.command + delta + 256) % 256;
        code = code.withCommand(c);
        updateLabels();
        send();
    }

    private void send() {
        if (!sender.available()) {
            Toast.makeText(activity, "This phone has no IR blaster.", Toast.LENGTH_SHORT).show();
            return;
        }
        sender.send(code);
    }

    private void stopAuto() {
        auto = false;
        handler.removeCallbacks(autoStep);
        if (autoButton != null) updateLabels();
    }

    private void updateLabels() {
        codeLabel.setText(code.describe());
        counter.setText("Code " + (code.command + 1) + " of 256");
        autoButton.setText(auto ? "Stop auto-try" : "Auto-try (a new code every 2 s)");
    }
}
