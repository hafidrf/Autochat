package id.co.kamil.autochat.utils;

public class API {
    public static final int SOCKET_TIMEOUT = 30000;//30 seconds - change to what you want

    public static final String BASE_URL = "https://sandbox.wabot.id/";
    public static final String URL_POST_LIST_CONTACT = BASE_URL + "api/contact/list";
    public static final String URL_POST_HAPUS_CONTACT = BASE_URL + "api/contact/delete";
    public static final String URL_POST_LIST_GRUP = BASE_URL + "api/group/list";
    public static final String URL_POST_CREATE_CONTACT = BASE_URL + "api/contact/add";
    public static final String URL_POST_EDIT_CONTACT = BASE_URL + "api/contact/edit";
    public static final String URL_POST_IMPORT_CONTACT = BASE_URL + "api/contact/import";
    public static final String URL_POST_ALL_CONTACT = BASE_URL + "api/contact/all";
    public static final String URL_POST_IMPORT_CONTACT_CSV = BASE_URL + "api/contact/importcsv";

    public static final String URL_POST_LIST_TEMPLATE = BASE_URL + "api/template";
    public static final String URL_POST_CREATE_TEMPLATE = BASE_URL + "api/template/create";
    public static final String URL_POST_EDIT_TEMPLATE = BASE_URL + "api/template/update";
    public static final String URL_POST_HAPUS_TEMPLATE = BASE_URL + "api/template/delete";
    public static final String URL_POST_GET_TEMPLATE = BASE_URL + "api/template/id";
    public static final String URL_POST_LIST_TEMPLATE_SHARE = BASE_URL + "api/template/share/";
    public static final String URL_POST_DETAIL_TEMPLATE_SHARE = BASE_URL + "api/template/share/detail";
    public static final String URL_POST_CREATE_TEMPLATE_SHARE = BASE_URL + "api/template/share/create";
    public static final String URL_POST_HAPUS_TEMPLATE_SHARE = BASE_URL + "api/template/share/delete";
    public static final String URL_POST_HAPUS_TEMPLATE_SHARE_ALL = BASE_URL + "api/template/share/delete_all";

    public static final String URL_POST_LIST_TEMPLATE_DICTIONARY = BASE_URL + "api/template/dictionary";
    public static final String URL_POST_CREATE_TEMPLATE_DICTIONARY = BASE_URL + "api/template/dictionary/create";
    public static final String URL_POST_EDIT_TEMPLATE_DICTIONARY = BASE_URL + "api/template/dictionary/update";
    public static final String URL_POST_HAPUS_TEMPLATE_DICTIONARY = BASE_URL + "api/template/dictionary/delete";
    public static final String URL_POST_GET_TEMPLATE_DICTIONARY = BASE_URL + "api/template/dictionary/id";


    public static final String URL_POST_LIST_WAFORM = BASE_URL + "api/waform";
    public static final String URL_POST_CREATE_WAFORM = BASE_URL + "api/waform/create";
    public static final String URL_POST_EDIT_WAFORM = BASE_URL + "api/waform/update";
    public static final String URL_POST_HAPUS_WAFORM = BASE_URL + "api/waform/delete";
    public static final String URL_POST_GET_WAFORM = BASE_URL + "api/waform/id";
    public static final String URL_POST_CHECK_SUBDOMAIN_WAFORM = BASE_URL + "api/waform/available_subdomain";


    public static final String URL_POST_LIST_LINKPAGE = BASE_URL + "api/linkpage";
    public static final String URL_POST_GET_LINKPAGE = BASE_URL + "api/linkpage/id";
    public static final String URL_POST_HAPUS_LINKPAGE = BASE_URL + "api/linkpage/delete";
    public static final String URL_POST_CREATE_LINKPAGE = BASE_URL + "api/linkpage/create";
    public static final String URL_POST_CHECK_SUBDOMAIN_LINKPAGE = BASE_URL + "api/linkpage/available_subdomain";


    public static final String URL_POST_CREATE_SETTINGS = BASE_URL + "api/settings/create";
    public static final String URL_POST_GET_SETTINGS = BASE_URL + "api/settings/id";


    public static final String URL_POST_UPDATE_TOKEN_FIREBASE = BASE_URL + "api/firebase/update_token";
    public static final String URL_POST_NOTIF_AUTOREPLY = BASE_URL + "api/reversal/add";

    public static final String URL_POST_CREATE_GROUP = BASE_URL + "api/group/add";
    public static final String URL_POST_EDIT_GROUP = BASE_URL + "api/group/edit";
    public static final String URL_POST_HAPUS_GRUP = BASE_URL + "api/group/delete";
    public static final String URL_POST_GROUP_ADD_CONTACT = BASE_URL + "api/group/add_contact";
    public static final String URL_POST_HAPUS_CONTACT_FROM_GROUP = BASE_URL + "api/group/delete_contact";
    public static final String URL_POST_LIST_CONTACT_GROUP = BASE_URL + "api/group/list_grup_kontak";

    public static final String URL_POST_LIST_FOLLOW_UP = BASE_URL + "api/followup/list";
    public static final String URL_POST_HAPUS_FOLLOW_UP = BASE_URL + "api/followup/delete";
    public static final String URL_POST_CREATE_FOLLOWUP = BASE_URL + "api/followup/add";
    public static final String URL_POST_GET_FOLLOWUP = BASE_URL + "api/followup/by_id";
    public static final String URL_POST_EDIT_FOLLOWUP = BASE_URL + "api/followup/edit";
    public static final String URL_POST_ADD_CONTACT_FOLLOW_UP = BASE_URL + "api/followup/add_contact";

    public static final String URL_POST_LIST_ANTRIAN_PESAN = BASE_URL + "api/message/list";
    public static final String URL_POST_CREATE_PESAN_ANTRIAN_CONTACT = BASE_URL + "api/message/add_by_contact";
    public static final String URL_POST_CREATE_PESAN_ANTRIAN_GROUP = BASE_URL + "api/message/add_by_group";
    public static final String URL_POST_HAPUS_PESAN_ANTRIAN = BASE_URL + "api/message/delete";
    public static final String URL_POST_HAPUS_SEMUA_PESAN_ANTRIAN = BASE_URL + "api/message/delete_all";

    public static final String URL_POST_LIST_SCHEDULE = BASE_URL + "api/message/schedule";
    public static final String URL_POST_HAPUS_SCHEDULE = BASE_URL + "api/message/schedule_delete";


    public static final String URL_POST_LIST_AUTO_REPLY = BASE_URL + "api/message/autoreply";
    public static final String URL_POST_HAPUS_AUTO_REPLY = BASE_URL + "api/message/autoreply_delete";
    public static final String URL_POST_UPDATE_AUTOREPLY = BASE_URL + "api/message/autoreply_edit";
    public static final String URL_POST_CREATE_AUTOREPLY = BASE_URL + "api/message/autoreply_add";
    public static final String URL_POST_GET_AUTOREPLY = BASE_URL + "api/message/autoreply_get";

    public static final String URL_POST_LIST_GRUP_AUTO_REPLY = BASE_URL + "api/group/autoreply";
    public static final String URL_POST_HAPUS_GRUP_AUTO_REPLY = BASE_URL + "api/group/autoreply_delete";
    public static final String URL_POST_EDIT_GROUP_AUTO_REPLY = BASE_URL + "api/group/autoreply_edit";
    public static final String URL_POST_CREATE_GROUP_AUTO_REPLY = BASE_URL + "api/group/autoreply_add";


    public static final String URL_POST_LIST_AUTO_TEXT = BASE_URL + "api/autotext/list";
    public static final String URL_POST_HAPUS_AUTO_TEXT = BASE_URL + "api/autotext/delete";
    public static final String URL_POST_GET_AUTOTEXT = BASE_URL + "api/autotext/get";
    public static final String URL_POST_UPDATE_AUTOTEXT = BASE_URL + "api/autotext/edit";
    public static final String URL_POST_CREATE_AUTOTEXT = BASE_URL + "api/autotext/add";

    public static final String URL_POST_CREATE_GROUP_AUTOTEXT = BASE_URL + "api/group/autotext_add";
    public static final String URL_POST_EDIT_GROUP_AUTOTEXT = BASE_URL + "api/group/autotext_edit";
    public static final String URL_POST_HAPUS_GRUP_AUTOTEXT = BASE_URL + "api/group/autotext_delete";
    public static final String URL_POST_LIST_GROUP_AUTOTEXT = BASE_URL + "api/group/autotext";

    public static final String URL_SYNC_DB = BASE_URL + "api/sync";
    public static final String URL_SYNC_DB2 = BASE_URL + "api/sync2";
    public static final String URL_SYNC_DB_OUTBOX = BASE_URL + "api/sync/sync_outbox";
    public static final String URL_SYNC_DB_OUTBOX_REAL = BASE_URL + "api/sync/sync_outbox_real";


    public static final String URL_POST_LOGIN = BASE_URL + "api/authentication/login";
    public static final String URL_POST_REGISTER = BASE_URL + "api/authentication/signup";
    public static final String URL_POST_LIST_PESAN = BASE_URL + "api/v1/notification/queue/list";
    public static final String URL_POST_LIST_PESAN_JADWAL = BASE_URL + "api/v1/scheduled-message/list";
    public static final String URL_POST_HAPUS_PESAN_TERJADWAL = BASE_URL + "api/v1/scheduled-message/multiple-delete";
    public static final String URL_POST_CREATE_JADWAL_BY_GROUP = BASE_URL + "api/message/schedule_add_group";
    public static final String URL_POST_CREATE_JADWAL_BY_CONTACT = BASE_URL + "api/message/schedule_add_contact";
    public static final String URL_POST_UPDATE_JADWAL_BY_GROUP = BASE_URL + "api/message/schedule_update_group";
    public static final String URL_POST_UPDATE_JADWAL_BY_CONTACT = BASE_URL + "api/message/schedule_update_contact";
    public static final String URL_POST_GET_SCHEDULE = BASE_URL + "api/message/get_schedule";

    public static final String URL_POST_LIST_CONTACT_ID = BASE_URL + "api/contact/list_id";


    public static final String URL_POST_CREATE_RIWAYAT = BASE_URL + "api/contact/histori_add";
    public static final String URL_POST_LIST_RIWAYAT = BASE_URL + "api/contact/histori";
    public static final String URL_POST_DELETE_RIWAYAT = BASE_URL + "api/contact/histori_delete";

    public static final String URL_POST_CREATE_NOTIF_FIREBASE = BASE_URL + "api/firebase/create";
    public static final String URL_POST_EDIT_NOTIF_FIREBASE = BASE_URL + "api/firebase/edit";
    public static final String URL_POST_LIST_NOTIF_FIREBASE = BASE_URL + "api/firebase/list";
    public static final String URL_POST_DELETE_NOTIF_FIREBASE = BASE_URL + "api/firebase/delete";
    public static final String URL_POST_LIST_USER_FIREBASE = BASE_URL + "api/firebase/user";


    public static final String URL_GET_PROVINSI = BASE_URL + "api/wilayah/provinsi";
    public static final String URL_GET_KOTA = BASE_URL + "api/wilayah/kota";

    public static final String URL_POST_LIST_SHORTEN = BASE_URL + "api/shorten/listshorten";
    public static final String URL_POST_DELETE_SHORTEN = BASE_URL + "api/shorten/hapus";
    public static final String URL_POST_CREATE_SHORTEN = BASE_URL + "api/shorten/add";
    public static final String URL_POST_EDIT_SHORTEN = BASE_URL + "api/shorten/edit";
    public static final String URL_POST_CHECK_SUBDOMAIN = BASE_URL + "api/shorten/available_subdomain";
    public static final String URL_POST_SHORTEN_BY_ID = BASE_URL + "api/shorten/shorten_by_id";

    public static final String URL_POST_REFERAL = BASE_URL + "api/affiliasi/load";
    public static final String URL_POST_DASHBOARD = BASE_URL + "api/dashboard/load";

    public static final String URL_POST_LIST_WHATSAPP = BASE_URL + "api/v1/whatsapp-instance/list";

    public static final String URL_POST_LOGOUT = BASE_URL + "api/logout";
    public static final String URL_POST_NOTIF_FIREBASE = BASE_URL + "api/firebase/push";


    public static final String URL_GET_OLSHOP_PROPINSI = "https://olshop.id/index.php?route=account/account/country";
    public static final String URL_GET_OLSHOP_KOTA = "https://olshop.id/index.php?route=extension/shipping/rajaongkir/zone";
    public static final String URL_GET_OLSHOP_KECAMATAN = "https://olshop.id/index.php?route=extension/shipping/rajaongkir/city";

    public static final int LIMIT_KONTAK = 100;
    public static final int LIMIT_AUTO_REPLY = 10;
    public static final int LIMIT_PESAN = 10;
    public static final int LIMIT_WAFORM_BASIC = 1;
    public static final int LIMIT_WAFORM_PREMIUM = 3;
    public static final int LIMIT_LINKPAGE_BASIC = 1;
    public static final int LIMIT_LINKPAGE_PREMIUM = 3;
    public static final int LIMIT_TEMPLATE_BASIC = 10;
    public static final int LIMIT_TEMPLATE_PREMIUM = 100;

    public static final int LIMIT_SHORTEN = 1;
    public static final int LIMIT_LEAD_MAGNET_BASIC = 1;
    public static final int LIMIT_LEAD_MAGNET_PREMIUM = 5;

    public static final String URL_HELP = "https://api.whatsapp.com/send?phone=6283128302901&text=Halo%20Wabot,%20Tolong%20Bantu%20Saya%20";
    public static final String URL_ECOURSE = "https://api.whatsapp.com/send?phone=6283128302901&text=Whatsapp%20Marketing%20";

    public static final String URL_MARKETING_TOOL = "https://olshop.id/account/tools";
    public static final String URL_TUTORIAL = "https://olshop.id/tutorialwabot";
    public static final String URL_LANDING_PAGE = "https://olshop.id/wabot";
    public static final String URL_DOWNLINE = "https://olshop.id/affiliate/result";
    public static final String URL_LUPA_PASSWORD = "https://olshop.id/account/forgotten";
    public static final String URL_EDIT_AKUN = "https://olshop.id/account/edit";
    public static final String URL_DIRECT_LINK_UPGRADE = "https://olshop.id/index.php?route=account/upgrade&customer_group_id=1";

    public static final String TEMPLATE_SHARE = "\"Halo semua! Aku baru nemu aplikasi Whatsapp Marketing yang keren banget. Sebelumnya Saya tidak pernah merasa segampang ini promosi melalui whatsapp karena bisa kirim pesan otomatis, kirim pesan \n" +
            "terjadwal, auto text, auto reply dan fitur canggih lainnya. Semua bisa dilakukan dari HP android. Cobain download aplikasinya disini:\n" +
            "\n" +
            "[linklanding]\n" +
            "\n" +
            "Info lebih lengkap:\n" +
            "[linkweb]";

    public static final String DESKRIPSI_INFO = "Untuk penjelasan apa itu WABOT, bagaimana cara penggunaan dan informasi lainnya. Silakan tanyakan ke whastapp nya  WABOT langsung, WABOT akan balas secara otomatis sesuai Keyword dan intruksi yang dimasukan. <a href=\"https://api.whatsapp.com/send?phone=6283128302901&text=Halo,%20Tolong%20Bantu%20Saya%20untuk%20Mengetahui%20Apa%20itu%20WABOT?%20\">TANYA SEKARANG!</a>.\n";

    public static final String URL_POST_LIST_OPERATOR = BASE_URL + "api/operator/list";
    public static final String URL_POST_HAPUS_OPERATOR = BASE_URL + "api/operator/delete";
    public static final String URL_POST_CREATE_OPERATOR = BASE_URL + "api/operator/create";
    public static final String URL_GET_API_KEY = BASE_URL + "api/apikey";
    public static final String URL_POST_GENERATE_API_KEY = BASE_URL + "api/apikey/generate";
    public static final String URL_GET_VERSION = BASE_URL + "api/collection/version";
    public static final String URL_POST_URL_CALLBACK = BASE_URL + "api/apikey/update_callback";

    //lead magnet
    public static final String URL_POST_LIST_LEAD_MAGNET = BASE_URL + "api/leadmagnet/list";
    public static final String URL_POST_HAPUS_LEADMAGNET = BASE_URL + "api/leadmagnet/delete";
    public static final String URL_POST_CHECK_SUBDOMAIN_LEAD = BASE_URL + "api/leadmagnet/available_subdomain";
    public static final String URL_POST_LEAD_MAGNET_BY_ID = BASE_URL + "api/leadmagnet/by_id";
    public static final String URL_POST_CREATE_LEAD_MAGNET = BASE_URL + "api/leadmagnet/add";
    public static final String URL_POST_EDIT_LEAD_MAGNET = BASE_URL + "api/leadmagnet/update";

}
