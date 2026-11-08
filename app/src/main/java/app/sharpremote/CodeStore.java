package app.sharpremote;

import android.content.Context;
import android.content.SharedPreferences;

import app.sharpremote.ir.CodeSets;
import app.sharpremote.ir.IrCode;
import app.sharpremote.ir.RemoteKey;

/** Remembers the chosen code set and any codes you set yourself. */
public final class CodeStore {

    private static final String KEY_SET = "code_set";
    private final SharedPreferences prefs;

    public CodeStore(Context context) {
        prefs = context.getSharedPreferences("sharp_remote", Context.MODE_PRIVATE);
    }

    public String getSetId() {
        return prefs.getString(KEY_SET, CodeSets.ALL[0].id);
    }

    public void setSetId(String id) {
        prefs.edit().putString(KEY_SET, id).apply();
    }

    /** Your saved code for this key, or the code set's built-in one, or null. */
    public IrCode codeFor(CodeSets.CodeSet set, RemoteKey key) {
        IrCode custom = customCode(set, key);
        return custom != null ? custom : set.codes.get(key);
    }

    public IrCode customCode(CodeSets.CodeSet set, RemoteKey key) {
        return IrCode.parse(prefs.getString(prefKey(set, key), null));
    }

    public void saveCustom(CodeSets.CodeSet set, RemoteKey key, IrCode code) {
        if (code == null) {
            prefs.edit().remove(prefKey(set, key)).apply();
        } else {
            prefs.edit().putString(prefKey(set, key), code.serialize()).apply();
        }
    }

    private static String prefKey(CodeSets.CodeSet set, RemoteKey key) {
        return "custom_" + set.id + "_" + key.name();
    }
}
