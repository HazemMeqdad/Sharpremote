package app.sharpremote;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Small helpers for building the screens in code. */
final class Ui {

    static final int BACKGROUND = Color.rgb(17, 17, 19);
    static final int KEY = Color.rgb(43, 43, 46);
    static final int KEY_BORDER = Color.rgb(64, 64, 68);
    static final int TEXT = Color.rgb(236, 236, 236);
    static final int MUTED = Color.rgb(150, 150, 155);
    static final int RED = Color.rgb(232, 90, 90);
    static final int GREEN = Color.rgb(30, 158, 106);
    static final int YELLOW = Color.rgb(229, 196, 58);
    static final int BLUE = Color.rgb(59, 125, 216);

    private Ui() {}

    static int dp(Context c, float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics()));
    }

    static StateListDrawable keyBackground(Context c, int color, float radiusDp) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed}, shape(c, lighten(color, 0.25f), radiusDp));
        s.addState(new int[]{}, shape(c, color, radiusDp));
        return s;
    }

    private static GradientDrawable shape(Context c, int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        g.setStroke(dp(c, 1), color == KEY ? KEY_BORDER : lighten(color, 0.1f));
        return g;
    }

    static int lighten(int color, float amount) {
        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        r += (int) ((255 - r) * amount);
        g += (int) ((255 - g) * amount);
        b += (int) ((255 - b) * amount);
        return Color.rgb(r, g, b);
    }

    static TextView text(Context c, String s, float sp, int color) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        return t;
    }

    static Button button(Context c, String label) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        return b;
    }

    static EditText numberField(Context c) {
        EditText e = new EditText(c);
        e.setSingleLine(true);
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        return e;
    }

    static LinearLayout row(Context c) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    static LinearLayout column(Context c, int paddingDp) {
        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        int p = dp(c, paddingDp);
        col.setPadding(p, p, p, p);
        return col;
    }

    static LinearLayout.LayoutParams weighted(float weight) {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
    }

    /** Parses "169" or "0xA9". Returns -1 if it isn't a number. */
    static int parseNumber(String s) {
        try {
            return Integer.decode(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
