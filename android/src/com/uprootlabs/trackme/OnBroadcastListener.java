package com.uprootlabs.trackme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnBroadcastListener extends BroadcastReceiver {
  private final String ON_BOOT_TAG = "onBoot";
  private final String onBoot = "android.intent.action.BOOT_COMPLETED";
  private final String onNetworkChange = "android.net.conn.CONNECTIVITY_CHANGE";

  private void startUploadService(final Context context, final String action) {

    final Intent broadcastIntent = new Intent(context, UploadService.class);
    broadcastIntent.setAction(action);
    context.startService(broadcastIntent);

  }

  @Override
  public void onReceive(final Context context, final Intent intent) {
    final String broadCastAction = intent.getAction();

    if (broadCastAction.equals(onBoot)) {

      startUploadService(context, UploadService.ACTION_ON_BOOT);

    } else if (broadCastAction.equals(onNetworkChange)) {

      if (UploadService.isNetworkAvailable(context)) {

        Log.d(ON_BOOT_TAG, "Net available");

        startUploadService(context, UploadService.ACTION_NETWORK_AVAILABLE);

      } else {

        Log.d(ON_BOOT_TAG, "No internet connection");

        startUploadService(context, UploadService.ACTION_NETWORK_UNAVAILABLE);

      }

    }

  }

}
