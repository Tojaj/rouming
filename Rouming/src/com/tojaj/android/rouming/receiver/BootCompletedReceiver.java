package com.tojaj.android.rouming.receiver;

import com.tojaj.android.rouming.service.RoumingSyncService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent syn_intent = new Intent(context, RoumingSyncService.class);
        syn_intent.putExtra(RoumingSyncService.EXTRA_FORCE_SYNC, true);
        context.startService(syn_intent);
    }

}
