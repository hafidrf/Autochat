package id.co.kamil.autochat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

public class PermissionManagement {
  private static final PermissionManagement INSTANCE = new PermissionManagement();
  public static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

  private PermissionManagement(){}

  public static PermissionManagement getInstance() {
    return INSTANCE;
  }

  public boolean needAccessToOverDrawApps(final Activity activity) {
    //Check if the application has draw over other apps permission or not?
    //This permission is by default available for API<23. But for API > 23
    //you have to ask for the permission in runtime.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
      //If the draw over permission is not available open the settings screen
      //to grant the permission.
      new AlertDialog.Builder(activity)
          .setTitle("Akses Floating Widget")
          .setMessage("WABOT memerlukan akses over draw untuk floating widget")
          .setNegativeButton(android.R.string.cancel, null)
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                  Uri.parse("package:" + activity.getPackageName()));
              activity.startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
            }
          }).show();
      return true;
    }

    return false;
  }
}
