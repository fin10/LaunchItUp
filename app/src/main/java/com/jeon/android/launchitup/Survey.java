package com.jeon.android.launchitup;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class Survey {

    public static synchronized void init(Context context) {
    }

    public static void send(@NonNull String name, long value) {
    }

    public static void send(@NonNull Action action, @NonNull String label) {
    }

    public enum Action {
        CLICK {
            @Override
            public String toString() {
                return "click";
            }
        },

        LAUNCH {
            @Override
            public String toString() {
                return "launch";
            }
        },

        ENTER {
            @Override
            public String toString() {
                return "enter";
            }
        }
    }
}
