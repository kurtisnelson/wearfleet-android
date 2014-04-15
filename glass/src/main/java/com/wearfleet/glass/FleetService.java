package com.wearfleet.glass;

import android.content.Context;
import android.content.Intent;

public class FleetService extends com.wearfleet.core.FleetService {
    public static void start(Context context) {
        Intent i = new Intent(context, FleetService.class);
        context.startService(i);
    }

    public static void stop(Context context) {
        Intent i = new Intent(context, FleetService.class);
        context.stopService(i);
    }

    public static boolean isRunning(Context c) {
        return com.wearfleet.core.FleetService.isRunning(c, FleetService.class);
    }
}
