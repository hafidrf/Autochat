package id.co.kamil.autochat.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "autochat.db";
    private static final int DATABASE_VERSION = 15;
    private static final String TAG = "DBHelper";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table kontak (col_id text,col_phone text, col_sapaan text, col_firstname text, col_lastname text);");
        db.execSQL("create table autoreply (col_keyword text, col_balasan text);");
        db.execSQL("create table autotext (col_shorcut text, col_template text);");
        db.execSQL("create table setting (col_key text, col_val text);");
        db.execSQL("create table template_promosi (col_id INTEGER, col_picture text);");
        db.execSQL("create table template_dictionary (col_id INTEGER, col_keyword text,col_nilai text);");
        db.execSQL("create table log (id INTEGER PRIMARY KEY   AUTOINCREMENT, col_created text, col_service text, col_log text, col_status,id_user);");
        db.execSQL("create table outbox (col_id INTEGER , col_destnumber text, col_message text, col_sentdate text DEFAULT NULL, col_status_sent INTEGER DEFAULT 0, col_upload INTEGER DEFAULT 0, col_image_hash text DEFAULT NULL,col_image_url text DEFAULT NULL,col_lock INTEGER DEFAULT 0,col_index_order INTEGER DEFAULT 0,col_created text,col_name text);");
        db.execSQL("create table received (id INTEGER PRIMARY KEY AUTOINCREMENT, col_sender text, col_message, col_upload INTEGER DEFAULT 0,id_user text,timestamp text,created text);");
        //sql = "INSERT INTO biodata (no, nama, tgl, jk, alamat) VALUES ('1', 'Darsiwan', '1996-07-12', 'Laki-laki','Indramayu');";
        //db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        db.execSQL("DROP TABLE IF EXISTS kontak");
        db.execSQL("DROP TABLE IF EXISTS autoreply");
        db.execSQL("DROP TABLE IF EXISTS autotext");
        db.execSQL("DROP TABLE IF EXISTS setting");
        db.execSQL("DROP TABLE IF EXISTS template_promosi");
        db.execSQL("DROP TABLE IF EXISTS template_dictionary");
        db.execSQL("DROP TABLE IF EXISTS outbox");
        db.execSQL("DROP TABLE IF EXISTS log");
        db.execSQL("DROP TABLE IF EXISTS received");
        onCreate(db);
        // TODO Auto-generated method stub
    }

    public static int getDatabaseVersion() {
        return DATABASE_VERSION;
    }

    public List<String> cari_autotext(String keyword) {

        List<String> result = new ArrayList<>();
        try {
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM autotext where col_shorcut LIKE '%" + keyword + "%'", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        String shorcut = cursor.getString(0);
                        String template = cursor.getString(1);
                        result.add(template);
                    } while (cursor.moveToNext());
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<String[]> cari_phone(String keyword) {
        List<String[]> result = new ArrayList<>();
        try {
            SQLiteDatabase db = getWritableDatabase();
            String keyword1 = keyword.trim();
            String keyword2 = keyword.trim();

            if (keyword1.length() > 1) {
                if (keyword1.substring(0, 1).equals("62")) {
                    keyword2 = "0" + keyword.substring(2);
                } else {
                    keyword2 = "62" + keyword.substring(2);
                }
            }

            String[] args = {keyword1, keyword2};
            Cursor cursor = db.rawQuery("SELECT * FROM kontak where col_phone=? COLLATE NOCASE or col_phone=? COLLATE NOCASE LIMIT 1", args);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(0);
                        String phone = cursor.getString(1);
                        String sapaan = cursor.getString(2);
                        String firstname = cursor.getString(3);
                        String lastname = cursor.getString(4);
                        result.add(new String[]{id, phone, sapaan, firstname, lastname});
                    } while (cursor.moveToNext());
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void insertLog(String created, String service, String log, String status, String id_user) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("INSERT INTO log (col_created,col_service,col_log,col_status,id_user) VALUES(?,?,?,?,?);", new String[]{created, service, log, status, id_user});
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }

    }

    public void insertReceived(String sender, String message, String id_user, String timestamp) {
        try {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c);
            String created = formattedDate;
            SQLiteDatabase db = getWritableDatabase();
            if (cariReceived(timestamp) == false) {
                db.execSQL("INSERT INTO received (col_sender,col_message,id_user,timestamp,created) VALUES(?,?,?,?,?);", new String[]{sender, message, id_user, timestamp, created});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean cariReceived(String timestamp) {
        try {
            String[] args = {timestamp};
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM received where timestamp=? COLLATE NOCASE", args);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void insertAutoText(String shorcut, String template) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("INSERT INTO autotext (col_shorcut,col_template) VALUES(?,?);", new String[]{shorcut, template});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertDictionary(String id, String keyword, String nilai) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("INSERT INTO template_dictionary (col_id,col_keyword,col_nilai) VALUES(?,?);", new String[]{id, keyword, nilai});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllContact() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM kontak");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllAutoText() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM autotext");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllDictionary() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM template_dictionary");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String cari_keyword(String keyword) {
        try {
            String[] args = {keyword};
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM autoreply where col_keyword=? COLLATE NOCASE", args);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                cursor.moveToPosition(0);
                String result = cursor.getString(1);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<String> getAutoreply() {
        List<String> list = new ArrayList<>();
        try {
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM autoreply", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    String keyword = cursor.getString(0);
                    list.add(keyword);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String[]> getAllKamus() {
        List<String[]> list = new ArrayList<>();
        try {
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM template_dictionary", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                do {
                    String keyword = cursor.getString(1);
                    String nilai = cursor.getString(2);
                    list.add(new String[]{keyword, nilai});
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String[]> getReceivePending(String id_user) {
        List<String[]> result = new ArrayList<>();

        try {
            SQLiteDatabase db = getWritableDatabase();
            String[] args = {"0", id_user};
            Cursor cursor = db.rawQuery("SELECT * FROM received WHERE col_upload=? AND id_user=? ORDER BY id ASC", args);
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(0);
                        String sender = cursor.getString(1);
                        String message = cursor.getString(2);
                        String status = cursor.getString(3);
                        String when = cursor.getString(5);
                        String created = cursor.getString(6);
                        result.add(new String[]{id, sender, message, status, when, created});
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<String[]> getLog(String id_user, int page, int limit) {
        List<String[]> result = new ArrayList<>();

        try {
            SQLiteDatabase db = getWritableDatabase();
            int pg = (page - 1) * limit;
            String[] args = {id_user, String.valueOf(pg), String.valueOf(limit)};
            Cursor cursor = db.rawQuery("SELECT * FROM log WHERE id_user=? ORDER BY id DESC LIMIT ?,?", args);
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(0);
                        String created = cursor.getString(1);
                        String service = cursor.getString(2);
                        String log = cursor.getString(3);
                        String status = cursor.getString(4);
                        result.add(new String[]{id, created, service, log, status});
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void deleteAllLog(String id_user) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM log WHERE id_user=\"" + id_user + "\"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getVersionCodeDB() {
        try {
            String[] args = {"ver_db"};
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM setting where col_key=?", args);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                cursor.moveToPosition(0);
                String result = cursor.getString(1);
                return Integer.parseInt(result);
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getVersionCodeDB2(String key) {
        try {
            String[] args = {key};
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM setting where col_key=?", args);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                cursor.moveToPosition(0);
                String result = cursor.getString(1);
                return Integer.parseInt(result);
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void deleteAllAutoReply() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM autoreply");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllOutbox(int status) {
        try {
            SQLiteDatabase db = getWritableDatabase();

            String[] args = new String[]{String.valueOf(status), "0"};
            db.execSQL("DELETE FROM outbox WHERE col_status_sent=? AND col_lock=?", args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertAutoReply(String keyword, String balasan) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("INSERT INTO autoreply (col_keyword,col_balasan) VALUES(?,?);", new String[]{keyword, balasan});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertOutbox(String id, String destination_number, String message_outbox, String image_hash, String image_url) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("INSERT INTO outbox (col_id,col_destnumber,col_message,col_image_hash,col_image_url,col_index_order) VALUES(?,?,?,?,?,?);", new String[]{id, destination_number, message_outbox, image_hash, image_url, "0"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateOutbox(String id, String destination_number, String message_outbox, String image_hash, String image_url, String col_index_order) {
        try {
            int ver = getVersionCodeDB();

            SQLiteDatabase db = getWritableDatabase();
            if (col_index_order.isEmpty()) {
                db.execSQL("UPDATE outbox SET col_destnumber=?,col_message=?,col_image_hash=?,col_image_url=? WHERE col_id=?;", new String[]{destination_number, message_outbox, image_hash, image_url, id});
            } else {
                db.execSQL("UPDATE outbox SET col_destnumber=?,col_message=?,col_image_hash=?,col_image_url=?,col_index_order=? WHERE col_id=?;", new String[]{destination_number, message_outbox, image_hash, image_url, col_index_order, id});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteOutboxById(String id) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM outbox WHERE col_id=?;", new String[]{id});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateReceived(String id, String status) {
        try {
            int ver = getVersionCodeDB();

            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("UPDATE received SET col_upload=? WHERE id=?;", new String[]{status, id});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDBVersion(String versionCode) {
        try {
            int ver = getVersionCodeDB();

            SQLiteDatabase db = getWritableDatabase();
            if (ver < 0) {
                db.execSQL("INSERT INTO setting (col_key,col_val) VALUES(?,?);", new String[]{"ver_db", versionCode});
            } else {
                db.execSQL("UPDATE setting SET col_val=? WHERE col_key=?;", new String[]{versionCode, "ver_db"});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDBVersion2(String versionCode, String key) {
        try {
            int ver = getVersionCodeDB2(key);

            SQLiteDatabase db = getWritableDatabase();
            if (ver < 0) {
                Log.i(TAG, "insert version db " + key + " version " + versionCode);
                db.execSQL("INSERT INTO setting (col_key,col_val) VALUES(?,?);", new String[]{key, versionCode});
            } else {
                Log.i(TAG, "update version db " + key + " version " + versionCode);
                db.execSQL("UPDATE setting SET col_val=? WHERE col_key=?;", new String[]{versionCode, key});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getAntrianPesan() {
        List<String[]> result = new ArrayList<>();

        try {
            String[] args = {"0", "0"};
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM outbox where col_status_sent=? AND col_lock=? ORDER BY col_index_order ASC", args);
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        String[] str = new String[cursor.getColumnCount()];
                        for (int x = 0; x < cursor.getColumnCount(); x++) {
                            str[x] = cursor.getString(x);
                        }
                        result.add(str);
                    } while (cursor.moveToNext());
                }
            }
            for (String[] str : result) {
                for (int i = 0; i < str.length; i++) {
                    //Log.e(TAG,"result-"+i+":" + str[i]);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void updateAntrianPesan(String id, String value) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            Log.e(TAG, "value-index:" + value);
            Log.e(TAG, "id-index:" + id);
            if (value.isEmpty()) {
                db.execSQL("UPDATE outbox SET col_index_order = col_index_order+1 WHERE col_id=?;", new String[]{id});
            } else {
                db.execSQL("UPDATE outbox SET col_index_order =" + value + " WHERE col_id=?;", new String[]{id});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSent(String id, String status, String date, String uploaded) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("UPDATE outbox SET col_sentdate = ?, col_status_sent = ?, col_upload=? WHERE col_id=?;", new String[]{date, status, uploaded, id});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor outboxById(String id) {
        Cursor cursor = null;
        try {
            String[] args = {id};
            SQLiteDatabase db = getWritableDatabase();
            cursor = db.rawQuery("SELECT * FROM outbox where col_id=?", args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public void updateOutboxUpload(String id) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("UPDATE outbox SET col_upload=? WHERE col_id=?;", new String[]{"1", id});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String[]> outboxByUpload(String upload, String sent) {
        List<String[]> result = new ArrayList<>();
        try {
            String[] args = {upload, sent};
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM outbox where col_upload=? AND col_status_sent=?", args);
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(0);
                        String date = cursor.getString(3);
                        result.add(new String[]{id, date});
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean outboxIsLockById(String idMessage) {
        boolean result = false;

        try {
            String[] args = {idMessage};
            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT col_lock FROM outbox where col_id=? ", args);
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        int isLock = cursor.getInt(0);
                        if (isLock == 1) {
                            result = true;
                        }
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void clearData() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM autoreply");
            db.execSQL("DELETE FROM setting");
            db.execSQL("DELETE FROM autotext");
            db.execSQL("DELETE FROM outbox WHERE col_status_sent=0 OR (col_status_sent=1 AND col_upload=1)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertContact(String id, String phone, String sapaan, String firstname, String lastname) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("INSERT INTO kontak (col_id,col_phone,col_sapaan,col_firstname,col_lastname) VALUES(?,?,?,?,?);", new String[]{id, phone, sapaan, firstname, lastname});
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateOutboxImageById(String idMessage, String sha1) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("UPDATE outbox SET col_image_hash=? WHERE col_id=?;", new String[]{sha1, idMessage});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void lockOutboxById(String idMessage) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("UPDATE outbox SET col_lock=? WHERE col_id=?;", new String[]{"1", idMessage});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unlockOutboxById(String idMessage) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("UPDATE outbox SET col_lock=? WHERE col_id=?;", new String[]{"0", idMessage});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean updateKamus(String templateId, JSONObject requestBody) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("UPDATE template_dictionary SET col_keyword=?,col_nilai=? WHERE col_id=?;", new String[]{String.valueOf(requestBody.get("keyword")), String.valueOf(requestBody.get("nilai")), templateId});
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public boolean insertKamus(String id, String keyword, String nilai) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("INSERT INTO template_dictionary (col_id,col_keyword,col_nilai) VALUES (?,?,?);", new String[]{id, keyword, nilai});
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
