package id.co.kamil.autochat.autoreply;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import id.co.kamil.autochat.autoreply.models.Action;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.utils.SessionManager.KEY_CHILD;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_ID;
import static id.co.kamil.autochat.utils.SharPref.AUTOREPLY_BUSINESS;
import static id.co.kamil.autochat.utils.SharPref.AUTOREPLY_PERSONAL;

public class MyNotifServiceBackup extends NotificationListenerService {
    private static final String ID_SERVICE = "AutoReply";
    private BufferedWriter bw;
    public static final String TAG = "MyNotifiService";

    //    private SimpleDateFormat sdf;
//    private MyHandler handler = new MyHandler();
//    private String nMessage;
//    private String data;
    private DBHelper dbHelper;
    private boolean statusAutoReply = true;

    //    Handler mHandler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(Message msg) {
//            String msgString = (String) msg.obj;
//            //Toast.makeText(getApplicationContext(), msgString, Toast.LENGTH_LONG).show();
//        }
//    };
    private boolean is_child;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private SharPref sharePref;
    private String user_id;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("KEVIN", "Service is started" + "-----");
//        data = intent.getStringExtra("data");
        try {
            sharePref = new SharPref(this);
            dbHelper = new DBHelper(this);
            session = new SessionManager(this);
            userDetail = session.getUserDetails();
            is_child = Boolean.parseBoolean(userDetail.get(KEY_CHILD));
        } catch (NullPointerException e) {
            Log.i(TAG, e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.i(TAG, "onNotificationRemoved");

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

//        HashMap<String,String> dataKeyword = new HashMap<>();
//        dataKeyword.put("x-ass","waalaikumsalam");
//        dataKeyword.put("x-thanks","sama-sama");
//        dataKeyword.put("x-ready gan?","ready, silahkan di order kak");
        try {

            sharePref = new SharPref(this);
            dbHelper = new DBHelper(this);
            session = new SessionManager(this);
            userDetail = session.getUserDetails();
            is_child = Boolean.parseBoolean(userDetail.get(KEY_CHILD));
            user_id = userDetail.get(KEY_CUST_ID);

        } catch (NullPointerException e) {
            Log.i(TAG, e.getMessage());
        }
        Log.i(TAG, "Here");
        if (statusAutoReply == false || is_child == true) return;
        try {
            Date c = Calendar.getInstance().getTime();
            System.out.println("Current time => " + c);

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c);
            String created = formattedDate;
            this.cancelNotification(sbn.getKey());

            Action action = NotificationUtils.getQuickReplyAction(sbn.getNotification(), getPackageName());

            if (action != null) {
                Log.i(TAG, "success");

                boolean autoreply_personal = sharePref.getSessionBool(AUTOREPLY_PERSONAL);
                boolean autoreply_business = sharePref.getSessionBool(AUTOREPLY_BUSINESS);


                if (!autoreply_personal) {
                    dbHelper.insertLog(created, ID_SERVICE, "AutoReply Personal Disabled", "warning", user_id);
                }
                if (!autoreply_business) {
                    dbHelper.insertLog(created, ID_SERVICE, "AutoReply Business Disabled", "warning", user_id);
                }
                if (autoreply_personal && sbn.getPackageName().equals("com.whatsapp")) {
                    reply(sbn, action);
                }
                if (autoreply_business && sbn.getPackageName().equals("com.whatsapp.w4b")) {
                    reply(sbn, action);
                }
            } else {
                dbHelper.insertLog(created, ID_SERVICE, "Tombol Quick Reply tidak tersedia", "warning", user_id);
                Log.i(TAG, "not success");
            }


//            try {
//                //
//                //Some notifications can't parse the TEXT content. Here is a message to judge.
//                if (sbn.getNotification().tickerText != null) {
//                    SharedPreferences sp = getSharedPreferences("msg", MODE_PRIVATE);
//                    nMessage = sbn.getNotification().tickerText.toString();
//                    //Log.e("KEVIN", "Get Message" + "-----" + nMessage);
//                    sp.edit().putString("getMsg", nMessage).apply();
//                    Message obtain = Message.obtain();
//                    obtain.obj = nMessage;
//                    mHandler.sendMessage(obtain);
//                    init();
//                    if (nMessage.contains(data)) {
//                        Message message = handler.obtainMessage();
//                        message.what = 1;
//                        handler.sendMessage(message);
//                        writeData(sdf.format(new Date(System.currentTimeMillis())) + ":" + nMessage);
//                    }
//                }
//            } catch (Exception e) {
//                //Toast.makeText(MyNotifiService.this, "Unresolvable notification", Toast.LENGTH_SHORT).show();
//            }
        } catch (NullPointerException e) {
            Log.i(TAG, e.getMessage());
        }


    }

    private void reply(StatusBarNotification sbn, Action action) {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c);
        String created = formattedDate;

        String msg_client = sbn.getNotification().extras.get("android.text").toString().toLowerCase().trim();
        String cari = dbHelper.cari_keyword(msg_client);
        String sender_ori = sbn.getNotification().extras.getString("android.title");
        String sender = sender_ori;
        sender = sender.replace("+62", "0")
                .replace("-", "").replace(" ", "");
        List<String[]> personalisasi = dbHelper.cari_phone(sender);
        List<String> contactExist = searchContact(this, sender_ori);
        Log.i(TAG, "contactExist:" + contactExist.size());
        if (personalisasi.size() <= 0 && contactExist.size() > 0) {
            for (int x = 0; x < contactExist.size(); x++) {
                personalisasi = dbHelper.cari_phone(contactExist.get(x));
                if (personalisasi.size() > 0) {
                    break;
                }
            }
        }
        if (!(cari.equals("") || cari.isEmpty())) {
            //Log.i(TAG,"OK Brooooh");
            //cari = cari.replace("[sender_name]",sbn.getNotification().extras.getString("android.title").replace("+","").replace("-","").replace(" ",""));
            if (personalisasi.size() > 0) {
                cari = cari.replace("[sapaan]", personalisasi.get(0)[2])
                        .replace("[nama]", personalisasi.get(0)[3] + " " + personalisasi.get(0)[4]);
                dbHelper.insertLog(created, ID_SERVICE, "Nomor Pengirim berhasil ditemukan di DB Wabot", "normal", user_id);
                dbHelper.insertLog(created, ID_SERVICE, "Balas : " + cari + " ke " + sender + "(" + personalisasi.get(0)[3] + ")", "normal", user_id);
            } else {
                cari = cari.replace("[sapaan]", "")
                        .replace("[nama]", sender_ori);
                dbHelper.insertLog(created, ID_SERVICE, "Nomor Pengirim tidak ditemukan di DB Wabot", "warning", user_id);
                dbHelper.insertLog(created, ID_SERVICE, "Balas : " + cari + " ke " + sender_ori, "normal", user_id);
            }
            try {
                action.sendReply(getApplicationContext(), cari);
                dbHelper.insertLog(created, ID_SERVICE, "Berhasil dibalas", "success", user_id);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
                dbHelper.insertLog(created, ID_SERVICE, "Gagal dibalas. Error : " + e.getStackTrace().toString(), "danger", user_id);
            }
        } else {
            Log.i(TAG, "Msg: " + msg_client);
        }
//                    Log.e("Salman", "package name " + sbn.getPackageName());
        Log.e("Salman", "phone number " + sbn.getKey());  // extract from it
//                    Log.e("Salman", "sender " + sbn.getNotification().extras.getString("android.title"));
//                    Log.e("Salman", "text " + sbn.getNotification().extras.get("android.text"));
//                    Log.e("Salman", "extras " + sbn.getNotification().extras.getString("android.title"));
//                    Log.e("Salman", "Salman " + sbn.getNotification().tickerText);
    }

    public List<String> searchContact(Context context, String name) {
/// number is the phone number
        String selection = String.format("%s > 0", ContactsContract.Contacts.HAS_PHONE_NUMBER);
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(name));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, selection, null, null);
        try {
            if (cur.moveToFirst()) {
                List<String> list = new ArrayList<>();
                while (cur.moveToNext()) {
                    String number = cur.getString(1);
                    list.add(number);
                }
                return list;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return null;
    }

//    private void writeData(String str) {
//        try {
////            bw.newLine();
////            bw.write("NOTE");
//            bw.newLine();
//            bw.write(str);
//            bw.newLine();
////            bw.newLine();
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void init() {
//        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        try {
//            FileOutputStream fos = new FileOutputStream(newFile(), true);
//            OutputStreamWriter osw = new OutputStreamWriter(fos);
//            bw = new BufferedWriter(osw);
//        } catch (IOException e) {
//            Log.d("KEVIN", "BufferedWriter Initialization error");
//        }
//        Log.d("KEVIN", "Initialization Successful");
//    }
//
//    private File newFile() {
//        File fileDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "ANotification");
//        fileDir.mkdir();
//        String basePath = Environment.getExternalStorageDirectory() + File.separator + "ANotification" + File.separator + "record.txt";
//        return new File(basePath);
//
//    }
//
//
//    class MyHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//
//            switch (msg.what) {
//                case 1:
////                    Toast.makeText(MyService.this,"Bingo",Toast.LENGTH_SHORT).show();
//
//
//
//
//            }
//        }
//    }
}
