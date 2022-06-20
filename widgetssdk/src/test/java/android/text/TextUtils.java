package android.text;

/**
 * Mock implementation of isEmpty() in android.text.TextUtils.
 * It's needed for unit tests.
 */
public class TextUtils {
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}
