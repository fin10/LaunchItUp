package com.jeon.android.launchitup;

import android.support.annotation.NonNull;

public final class Log {

    private static final String TAG = "launch_it_up";

    public static void d(@NonNull String format, Object... args) {
        try {
            android.util.Log.d(TAG, buildMsg(String.format(format, args)));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void e(@NonNull String format, Object... args) {
        try {
            android.util.Log.e(TAG, buildMsg(String.format(format, args)));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private static String buildMsg(@NonNull String msg) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        StringBuilder buf = new StringBuilder(80);
        String fName = element.getFileName();
        if (fName == null) {
            buf.append("(Unknown Source)");
        } else {
            int lineNum = element.getLineNumber();

            buf.append('(');
            buf.append(fName);
            if (lineNum >= 0) {
                buf.append(':');
                buf.append(lineNum);
            }
            buf.append(')');
        }

        buf.append(' ');
        buf.append(msg);

        return buf.toString();
    }
}
