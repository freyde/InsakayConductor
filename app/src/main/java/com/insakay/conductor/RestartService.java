package com.insakay.conductor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Restarting service. .  .");
        context.startService(new Intent(context, locationService.class));
    }
}
