package id.co.kamil.autochat.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import id.co.kamil.autochat.R;

public class Utils {
    private static String bulanId(String bln) {
        switch (bln) {
            case "01":
            case "1":
                return "Januari";
            case "02":
            case "2":
                return "Februari";
            case "03":
            case "3":
                return "Maret";
            case "04":
            case "4":
                return "April";
            case "05":
            case "5":
                return "Mei";
            case "06":
            case "6":
                return "Juni";
            case "07":
            case "7":
                return "Juli";
            case "08":
            case "8":
                return "Agustus";
            case "09":
            case "9":
                return "September";
            case "10":
                return "Oktober";
            case "11":
                return "November";
            case "12":
                return "Desember";
            default:
                return "";

        }
    }

    public static void errorResponse(Context context, VolleyError error) {
        try {
            if (error instanceof NoConnectionError) {
                Toast.makeText(context, context.getResources().getString(R.string.toast_tidak_ada_internet), Toast.LENGTH_SHORT).show();
            } else if (error instanceof NetworkError) {
                Toast.makeText(context, context.getResources().getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
            } else if (error instanceof ServerError) {
                Toast.makeText(context, context.getResources().getString(R.string.toast_server_error), Toast.LENGTH_SHORT).show();
            } else if (error instanceof TimeoutError) {
                Toast.makeText(context, context.getResources().getString(R.string.toast_timeout), Toast.LENGTH_SHORT).show();
            } else if (error instanceof AuthFailureError) {
                Toast.makeText(context, context.getResources().getString(R.string.toast_server_error), Toast.LENGTH_SHORT).show();
            } else if (error instanceof ParseError) {
                Toast.makeText(context, context.getResources().getString(R.string.toast_server_error), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(context, context.getResources().getString(R.string.toast_server_error), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static int errorResponse(VolleyError error) {
        if (error instanceof NoConnectionError) {
            return R.string.toast_tidak_ada_internet;
        } else if (error instanceof NetworkError) {
            return R.string.toast_network_error;
        } else if (error instanceof ServerError) {
            return R.string.toast_server_error;
        } else if (error instanceof TimeoutError) {
            return R.string.toast_timeout;
        } else if (error instanceof AuthFailureError) {
            return R.string.toast_server_error;
        } else if (error instanceof ParseError) {
            return R.string.toast_server_error;
        } else {
            return R.string.toast_server_error;
        }
    }

    public static String errorResponseString(VolleyError error) {
        String json = null;

        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null) {
            switch (response.statusCode) {
                case 403:
                case 500:
                    json = new String(response.data);
                    //json = trimMessage(json, "message");
                    break;
            }
            //Additional cases
        }
        if (json == null) {
            return "false";
        } else {
            return json;
        }
    }

    public static String formateDateFromstring(String inputFormat, String outputFormat, String inputDate) {

        Date parsed = null;
        String outputDate = "";

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);

        } catch (ParseException e) {

        }

        return outputDate;

    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static float convertPixelsToDp(float px, Context context) {
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    public static String formatIdDateFromString(String inputDate) {
        if (inputDate.length() >= 15) {
            String tglBaru = null;
            try {
                String[] tgl = inputDate.split(" ")[0].split("-");
                String[] jam = inputDate.split(" ")[1].split(":");

                tglBaru = tgl[2] + " " + bulanId(tgl[1]) + " " + tgl[0];
                tglBaru = tglBaru + " " + jam[0] + ":" + jam[1];

            } catch (Exception e) {
                e.printStackTrace();
            }
            return tglBaru;
        } else {
            return inputDate;
        }

    }

    public static String formatCurrencyId(double harga) {
        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();

        formatRp.setCurrencySymbol("Rp. ");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');

        kursIndonesia.setDecimalFormatSymbols(formatRp);
        return kursIndonesia.format(harga);
    }

    public static void openAppPlaystore(Context context) {
        // you can also use BuildConfig.APPLICATION_ID
        String appId = context.getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp : otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appId));
            context.startActivity(webIntent);
        }
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
    //method to convert the selected image to base64 encoded string

    public static String ConvertBitmapToString(Bitmap bitmap) {
        String encodedImage = "";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        encodedImage = "data:image/png;base64," + Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);
//        try {
//            encodedImage= URLEncoder.encode("data:image/png;base64," + Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT), "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        return encodedImage;
    }

    public static boolean fileExist(Context context, String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public static String getDirWabot(String dir) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/wabot/" + dir + "/");
        return myDir.getPath();
    }

    public static void SaveImage(Bitmap finalBitmap, String dir, String filename) {

        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/wabot/" + dir + "/");
        myDir.mkdirs();

        String fname = filename;
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
