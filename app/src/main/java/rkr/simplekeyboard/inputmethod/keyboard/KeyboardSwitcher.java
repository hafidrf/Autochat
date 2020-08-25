/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rkr.simplekeyboard.inputmethod.keyboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import id.co.kamil.autochat.MainActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.ui.PengaturanActivity;
import id.co.kamil.autochat.ui.followup.MainFollowupActivity;
import id.co.kamil.autochat.utils.SharPref;
import rkr.simplekeyboard.inputmethod.event.Event;
import rkr.simplekeyboard.inputmethod.keyboard.KeyboardLayoutSet.KeyboardLayoutSetException;
import rkr.simplekeyboard.inputmethod.keyboard.internal.KeyboardState;
import rkr.simplekeyboard.inputmethod.keyboard.internal.KeyboardTextsSet;
import rkr.simplekeyboard.inputmethod.latin.InputView;
import rkr.simplekeyboard.inputmethod.latin.LatinIME;
import rkr.simplekeyboard.inputmethod.latin.RichInputMethodManager;
import rkr.simplekeyboard.inputmethod.latin.settings.Settings;
import rkr.simplekeyboard.inputmethod.latin.settings.SettingsValues;
import rkr.simplekeyboard.inputmethod.latin.utils.CapsModeUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.LanguageOnSpacebarUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.RecapitalizeStatus;
import rkr.simplekeyboard.inputmethod.latin.utils.ResourceUtils;

import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDER;

public final class KeyboardSwitcher implements KeyboardState.SwitchActions {
    private static final String TAG = KeyboardSwitcher.class.getSimpleName();

    private InputView mCurrentInputView;
    private View mMainKeyboardFrame;
    private MainKeyboardView mKeyboardView;
    private LatinIME mLatinIME;
    private RichInputMethodManager mRichImm;

    private KeyboardState mState;

    private KeyboardLayoutSet mKeyboardLayoutSet;
    // TODO: The following {@link KeyboardTextsSet} should be in {@link KeyboardLayoutSet}.
    private final KeyboardTextsSet mKeyboardTextsSet = new KeyboardTextsSet();

    private KeyboardTheme mKeyboardTheme;
    private Context mThemeContext;

    private static final KeyboardSwitcher sInstance = new KeyboardSwitcher();
    private InputConnection currentInputConnection;
    private int selStart,selEnd;
    private List<String[]> mSuggestions = new ArrayList<>();
    private SuggestionsAdapter adapterSuggestion;
    private DBHelper dbHelper;
    private SharPref sharePref;

    public static KeyboardSwitcher getInstance() {
        return sInstance;
    }

    private KeyboardSwitcher() {
        // Intentional empty constructor for singleton.
    }

    public static void init(final LatinIME latinIme) {
        sInstance.initInternal(latinIme);
    }

    private void initInternal(final LatinIME latinIme) {
        mLatinIME = latinIme;
        mRichImm = RichInputMethodManager.getInstance();
        mState = new KeyboardState(this);
    }

    public void updateKeyboardTheme() {
        final boolean themeUpdated = updateKeyboardThemeAndContextThemeWrapper(
                mLatinIME, KeyboardTheme.getKeyboardTheme(mLatinIME /* context */));
        if (themeUpdated && mKeyboardView != null) {
            mLatinIME.setInputView(onCreateInputView());
        }
    }

    private boolean updateKeyboardThemeAndContextThemeWrapper(final Context context,
                                                              final KeyboardTheme keyboardTheme) {
        if (mThemeContext == null || !keyboardTheme.equals(mKeyboardTheme)) {
            mKeyboardTheme = keyboardTheme;
            mThemeContext = new ContextThemeWrapper(context, keyboardTheme.mStyleId);
            KeyboardLayoutSet.onKeyboardThemeChanged();
            return true;
        }
        return false;
    }

    public void loadKeyboard(final EditorInfo editorInfo, final SettingsValues settingsValues,
                             final int currentAutoCapsState, final int currentRecapitalizeState) {
        final KeyboardLayoutSet.Builder builder = new KeyboardLayoutSet.Builder(
                mThemeContext, editorInfo);
        final Resources res = mThemeContext.getResources();
        final int keyboardWidth = mLatinIME.getMaxWidth();
        final int keyboardHeight = ResourceUtils.getKeyboardHeight(res, settingsValues);
        builder.setKeyboardGeometry(keyboardWidth, keyboardHeight);
        builder.setSubtype(mRichImm.getCurrentSubtype());

        builder.setLanguageSwitchKeyEnabled(false);
        //builder.setLanguageSwitchKeyEnabled(mLatinIME.shouldShowLanguageSwitchKey());
        builder.setShowSpecialChars(false);
        //builder.setShowSpecialChars(!settingsValues.mHideSpecialChars);
        builder.setShowNumberRow(false);
        //builder.setShowNumberRow(settingsValues.mShowNumberRow);
        mKeyboardLayoutSet = builder.build();
        try {
            mState.onLoadKeyboard(currentAutoCapsState, currentRecapitalizeState);
            mKeyboardTextsSet.setLocale(mRichImm.getCurrentSubtypeLocale(), mThemeContext);
        } catch (KeyboardLayoutSetException e) {
            Log.w(TAG, "loading keyboard failed: " + e.mKeyboardId, e.getCause());
        }
    }

    public void saveKeyboardState() {
        if (getKeyboard() != null) {
            mState.onSaveKeyboardState();
        }
    }

    public void onHideWindow() {
        if (mKeyboardView != null) {
            mKeyboardView.onHideWindow();
        }
    }

    private void setKeyboard(
            final int keyboardId,
            final KeyboardSwitchState toggleState) {
        final SettingsValues currentSettingsValues = Settings.getInstance().getCurrent();
        setMainKeyboardFrame(currentSettingsValues, toggleState);
        // TODO: pass this object to setKeyboard instead of getting the current values.
        final MainKeyboardView keyboardView = mKeyboardView;
        final Keyboard oldKeyboard = keyboardView.getKeyboard();
        final Keyboard newKeyboard = mKeyboardLayoutSet.getKeyboard(keyboardId);
        keyboardView.setKeyboard(newKeyboard);
        keyboardView.setKeyPreviewPopupEnabled(
                currentSettingsValues.mKeyPreviewPopupOn,
                currentSettingsValues.mKeyPreviewPopupDismissDelay);
        keyboardView.updateShortcutKey(mRichImm.isShortcutImeReady());
        final boolean subtypeChanged = (oldKeyboard == null)
                || !newKeyboard.mId.mSubtype.equals(oldKeyboard.mId.mSubtype);
        final int languageOnSpacebarFormatType = LanguageOnSpacebarUtils
                .getLanguageOnSpacebarFormatType(newKeyboard.mId.mSubtype);
        keyboardView.startDisplayLanguageOnSpacebar(subtypeChanged, languageOnSpacebarFormatType);
    }

    public Keyboard getKeyboard() {
        if (mKeyboardView != null) {
            return mKeyboardView.getKeyboard();
        }
        return null;
    }

    // TODO: Remove this method. Come up with a more comprehensive way to reset the keyboard layout
    // when a keyboard layout set doesn't get reloaded in LatinIME.onStartInputViewInternal().
    public void resetKeyboardStateToAlphabet(final int currentAutoCapsState,
                                             final int currentRecapitalizeState) {
        mState.onResetKeyboardStateToAlphabet(currentAutoCapsState, currentRecapitalizeState);
    }

    public void onPressKey(final int code, final boolean isSinglePointer,
                           final int currentAutoCapsState, final int currentRecapitalizeState) {
        mState.onPressKey(code, isSinglePointer, currentAutoCapsState, currentRecapitalizeState);
    }

    public void onReleaseKey(final int code, final boolean withSliding,
                             final int currentAutoCapsState, final int currentRecapitalizeState) {
        mState.onReleaseKey(code, withSliding, currentAutoCapsState, currentRecapitalizeState);
    }

    public void onFinishSlidingInput(final int currentAutoCapsState,
                                     final int currentRecapitalizeState) {
        mState.onFinishSlidingInput(currentAutoCapsState, currentRecapitalizeState);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetManualShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetManualShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_MANUAL_SHIFTED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetAutomaticShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetAutomaticShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_AUTOMATIC_SHIFTED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetShiftLockedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetShiftLockedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCKED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setAlphabetShiftLockShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setAlphabetShiftLockShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCK_SHIFTED, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setSymbolsKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setSymbolsKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_SYMBOLS, KeyboardSwitchState.OTHER);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void setSymbolsShiftedKeyboard() {
        if (DEBUG_ACTION) {
            Log.d(TAG, "setSymbolsShiftedKeyboard");
        }
        setKeyboard(KeyboardId.ELEMENT_SYMBOLS_SHIFTED, KeyboardSwitchState.SYMBOLS_SHIFTED);
    }

    public boolean isImeSuppressedByHardwareKeyboard(
            final SettingsValues settingsValues,
            final KeyboardSwitchState toggleState) {
        return settingsValues.mHasHardwareKeyboard && toggleState == KeyboardSwitchState.HIDDEN;
    }

    private void setMainKeyboardFrame(
            final SettingsValues settingsValues,
            final KeyboardSwitchState toggleState) {
        final int visibility =  isImeSuppressedByHardwareKeyboard(settingsValues, toggleState)
                ? View.GONE : View.VISIBLE;
        mKeyboardView.setVisibility(visibility);
        // The visibility of {@link #mKeyboardView} must be aligned with {@link #MainKeyboardFrame}.
        // @see #getVisibleKeyboardView() and
        // @see LatinIME#onComputeInset(android.inputmethodservice.InputMethodService.Insets)
        mMainKeyboardFrame.setVisibility(visibility);
    }

    public enum KeyboardSwitchState {
        HIDDEN(-1),
        SYMBOLS_SHIFTED(KeyboardId.ELEMENT_SYMBOLS_SHIFTED),
        OTHER(-1);

        final int mKeyboardId;

        KeyboardSwitchState(int keyboardId) {
            mKeyboardId = keyboardId;
        }
    }

    public KeyboardSwitchState getKeyboardSwitchState() {
        boolean hidden = mKeyboardLayoutSet == null
                || mKeyboardView == null
                || !mKeyboardView.isShown();
        if (hidden) {
            return KeyboardSwitchState.HIDDEN;
        } else if (isShowingKeyboardId(KeyboardId.ELEMENT_SYMBOLS_SHIFTED)) {
            return KeyboardSwitchState.SYMBOLS_SHIFTED;
        }
        return KeyboardSwitchState.OTHER;
    }

    // Future method for requesting an updating to the shift state.
    @Override
    public void requestUpdatingShiftState(final int autoCapsFlags, final int recapitalizeMode) {
        if (DEBUG_ACTION) {
            Log.d(TAG, "requestUpdatingShiftState: "
                    + " autoCapsFlags=" + CapsModeUtils.flagsToString(autoCapsFlags)
                    + " recapitalizeMode=" + RecapitalizeStatus.modeToString(recapitalizeMode));
        }
        mState.onUpdateShiftState(autoCapsFlags, recapitalizeMode);
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void startDoubleTapShiftKeyTimer() {
        if (DEBUG_TIMER_ACTION) {
            Log.d(TAG, "startDoubleTapShiftKeyTimer");
        }
        final MainKeyboardView keyboardView = getMainKeyboardView();
        if (keyboardView != null) {
            keyboardView.startDoubleTapShiftKeyTimer();
        }
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public void cancelDoubleTapShiftKeyTimer() {
        if (DEBUG_TIMER_ACTION) {
            Log.d(TAG, "setAlphabetKeyboard");
        }
        final MainKeyboardView keyboardView = getMainKeyboardView();
        if (keyboardView != null) {
            keyboardView.cancelDoubleTapShiftKeyTimer();
        }
    }

    // Implements {@link KeyboardState.SwitchActions}.
    @Override
    public boolean isInDoubleTapShiftKeyTimeout() {
        if (DEBUG_TIMER_ACTION) {
            Log.d(TAG, "isInDoubleTapShiftKeyTimeout");
        }
        final MainKeyboardView keyboardView = getMainKeyboardView();
        return keyboardView != null && keyboardView.isInDoubleTapShiftKeyTimeout();
    }

    /**
     * Updates state machine to figure out when to automatically switch back to the previous mode.
     */
    public void onEvent(final Event event, final int currentAutoCapsState,
                        final int currentRecapitalizeState) {
        mState.onEvent(event, currentAutoCapsState, currentRecapitalizeState);
    }

    public boolean isShowingKeyboardId(int... keyboardIds) {
        if (mKeyboardView == null || !mKeyboardView.isShown()) {
            return false;
        }
        int activeKeyboardId = mKeyboardView.getKeyboard().mId.mElementId;
        for (int keyboardId : keyboardIds) {
            if (activeKeyboardId == keyboardId) {
                return true;
            }
        }
        return false;
    }

    public boolean isShowingMoreKeysPanel() {
        return mKeyboardView.isShowingMoreKeysPanel();
    }

    public View getVisibleKeyboardView() {
        return mKeyboardView;
    }

    public MainKeyboardView getMainKeyboardView() {
        return mKeyboardView;
    }

    public void deallocateMemory() {
        if (mKeyboardView != null) {
            mKeyboardView.cancelAllOngoingEvents();
            mKeyboardView.deallocateMemory();
        }
    }
    public void setSuggestions(List<String> suggestions, int start, int end, InputConnection inputConnection){
        mSuggestions.clear();
        if (suggestions != null){

            int limit = 0;
            if (suggestions.size()==1){
                limit = 0;
            }else if(suggestions.size()>1){
                limit = 10;
            }
            for (int i = 0; i<suggestions.size();i++){
                String str_limit = suggestions.get(i);
                if(limit>0){
                    if(str_limit.length()>limit){
                        str_limit = str_limit.substring(0,limit) + "...";
                    }
                }
                mSuggestions.add(new String[]{suggestions.get(i),str_limit});
            }
        }
        adapterSuggestion.notifyDataSetChanged();
        currentInputConnection = inputConnection;
        selStart = start;
        selEnd = end;

    }
    public View onCreateInputView() {
        if (mKeyboardView != null) {
            mKeyboardView.closing();
        }
        updateKeyboardThemeAndContextThemeWrapper(
                mLatinIME, KeyboardTheme.getKeyboardTheme(mLatinIME /* context */));

        mCurrentInputView = (InputView) LayoutInflater.from(mThemeContext).inflate(R.layout.input_view,null);
        mMainKeyboardFrame = mCurrentInputView.findViewById(R.id.main_keyboard_frame);

        dbHelper = new DBHelper(mThemeContext);
        sharePref = new SharPref(mThemeContext);

        boolean status_bulk_sender = sharePref.getSessionBool(STATUS_BULK_SENDER);

        final LinearLayout layAutoText = (LinearLayout) mCurrentInputView.findViewById(R.id.layAutoText);
        final LinearLayout layToolbar = (LinearLayout) mCurrentInputView.findViewById(R.id.layToolbar);
        ImageView imgApps = (ImageView) mCurrentInputView.findViewById(R.id.imgApps);
        ImageView imgBack = (ImageView) mCurrentInputView.findViewById(R.id.imgBack);

        RecyclerView recyclerView = (RecyclerView) mCurrentInputView.findViewById(R.id.recyclerView);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(mThemeContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        adapterSuggestion = new SuggestionsAdapter(mThemeContext, mSuggestions);
        recyclerView.setAdapter(adapterSuggestion);

        adapterSuggestion.setClickListener(new SuggestionsAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (currentInputConnection!=null){
                    try{
                        currentInputConnection.setComposingRegion(selStart,selEnd);
                        currentInputConnection.setComposingText(mSuggestions.get(position)[0],selStart + mSuggestions.get(position)[0].length());
                        currentInputConnection.finishComposingText();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });

        final List<String[]> dataMenu = new ArrayList<>();
        dataMenu.add(new String[]{"Bulk Sender",String.valueOf(R.drawable.ic_add_contact_white),"switch"});
        dataMenu.add(new String[]{"Tambah Kontak",String.valueOf(R.drawable.ic_add_contact_white),"text"});
        dataMenu.add(new String[]{"Kontak",String.valueOf(R.drawable.ic_contact_white),"text"});
        dataMenu.add(new String[]{"Template Promosi",String.valueOf(R.drawable.ic_marketing_kit_dark),"text"});
        dataMenu.add(new String[]{"Broadcast",String.valueOf(R.drawable.ic_menu_send_dark),"text"});
        dataMenu.add(new String[]{"Follow Up",String.valueOf(R.drawable.ic_followup_dark),"text"});
        dataMenu.add(new String[]{"Auto Text",String.valueOf(R.drawable.ic_menu_autotext_dark),"text"});
        dataMenu.add(new String[]{"Pengaturan",String.valueOf(R.drawable.ic_pengaturan_dark),"text"});
        dataMenu.add(new String[]{"Dashboard",String.valueOf(R.drawable.ic_home_dark),"text"});

        RecyclerView recyclerMenu = (RecyclerView) mCurrentInputView.findViewById(R.id.recyclerMenu);
        LinearLayoutManager horizontalLayoutManagerMenu
                = new LinearLayoutManager(mThemeContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerMenu.setLayoutManager(horizontalLayoutManagerMenu);
        MenuAdapter adapterMenu = new MenuAdapter(mThemeContext, dataMenu);
        recyclerMenu.setAdapter(adapterMenu);
        adapterMenu.setClickListener(new MenuAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position == 1) {
                    Intent intent = new Intent();
                    String mPackage = "id.co.kamil.autochat";
                    String mClass = ".ui.kontak.FormKontakActivity";
                    intent.setComponent(new ComponentName(mPackage,mPackage+mClass));
                    intent.putExtra("tipe","add");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mThemeContext.startActivity(intent);
                }
                if (position == 2) {
                    Intent intent = new Intent(mThemeContext, MainActivity.class);
                    intent.putExtra("fragment","kontak");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mThemeContext.startActivity(intent);
                }
                if (position == 3) {
                    Intent intent = new Intent(mThemeContext, MainActivity.class);
                    intent.putExtra("fragment","templatepromosi");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mThemeContext.startActivity(intent);
                }
                if (position == 4) {
                    Intent intent = new Intent(mThemeContext, MainActivity.class);
                    intent.putExtra("fragment","broadcast");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mThemeContext.startActivity(intent);
                }
                if (position == 5) {
                    Intent intent = new Intent(mThemeContext, MainFollowupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mThemeContext.startActivity(intent);
                }
                if (position == 6) {
                    Intent intent = new Intent(mThemeContext, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("fragment","autotext");
                    mThemeContext.startActivity(intent);
                }
                if (position == 7) {
                    Intent intent = new Intent(mThemeContext, PengaturanActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mThemeContext.startActivity(intent);
                }
                if (position == 8) {
                    Intent intent = new Intent();
                    String mPackage = "id.co.kamil.autochat";
                    String mClass = ".MainActivity";
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(new ComponentName(mPackage,mPackage+mClass));
                    mThemeContext.startActivity(intent);
                }
            }
        });
        imgApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layToolbar.setVisibility(View.VISIBLE);
                layAutoText.setVisibility(View.GONE);


            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layToolbar.setVisibility(View.GONE);
                layAutoText.setVisibility(View.VISIBLE);
            }
        });


        mKeyboardView = (MainKeyboardView) mCurrentInputView.findViewById(R.id.keyboard_view);
        mKeyboardView.setKeyboardActionListener(mLatinIME);
        return mCurrentInputView;
    }
    static class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

        private List<String[]> mData;
        private LayoutInflater mInflater;
        private ItemClickListener mClickListener;
        private ItemCheckedListener mCheckedListener;
        SharPref sharePref;

        // data is passed into the constructor
        MenuAdapter(Context context, List<String[]> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
            sharePref = new SharPref(context);

        }

        // inflates the row layout from xml when needed
        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_keyboard_menu, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the view and textview in each row
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String[] suggestion = mData.get(position);
            holder.txtTitle.setText(suggestion[0]);
            holder.imgIcon.setImageResource(Integer.parseInt(suggestion[1]));

            boolean status_bulk_sender = sharePref.getSessionBool(STATUS_BULK_SENDER);

            holder.aSwitch.setChecked(status_bulk_sender);
            holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    sharePref.createSession(STATUS_BULK_SENDER,isChecked);
                }
           });
            if (suggestion[2] == "switch") {
                holder.aSwitch.setVisibility(View.VISIBLE);
                holder.imgIcon.setVisibility(View.GONE);
            }else {
                holder.aSwitch.setVisibility(View.GONE);
                holder.imgIcon.setVisibility(View.VISIBLE);
            }
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mData.size();
        }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,CompoundButton.OnCheckedChangeListener {
            TextView txtTitle;
            ImageView imgIcon;
            Switch aSwitch;

            ViewHolder(View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.txtTitle);
                imgIcon = itemView.findViewById(R.id.icon);
                aSwitch = itemView.findViewById(R.id.switchMenu);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        }

        // convenience method for getting data at click position
        public String[] getItem(int id) {
            return mData.get(id);
        }

        // allows clicks events to be caught
        public void setClickListener(ItemClickListener itemClickListener) {
            this.mClickListener = itemClickListener;
        }
        public void setCheckedListener(ItemCheckedListener itemCheckedListener) {
            this.mCheckedListener = itemCheckedListener;
        }
        // parent activity will implement this method to respond to click events
        public interface ItemClickListener {
            void onItemClick(View view, int position);
        }
        public interface ItemCheckedListener {
            void onItemClick(CompoundButton buttonView, boolean isChecked);
        }
    }
    static class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {

        private List<String[]> mSuggestions;
        private LayoutInflater mInflater;
        private ItemClickListener mClickListener;

        // data is passed into the constructor
        SuggestionsAdapter(Context context, List<String[]> suggestions) {
            this.mInflater = LayoutInflater.from(context);
            this.mSuggestions = suggestions;
        }

        // inflates the row layout from xml when needed
        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_suggestions, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the view and textview in each row
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String[] suggestion = mSuggestions.get(position);
            holder.txtSuggestion.setText(suggestion[1]);
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return mSuggestions.size();
        }

        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView txtSuggestion;

            ViewHolder(View itemView) {
                super(itemView);
                txtSuggestion = itemView.findViewById(R.id.txtSuggestions);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            }
        }

        // convenience method for getting data at click position
        public String[] getItem(int id) {
            return mSuggestions.get(id);
        }

        // allows clicks events to be caught
        public void setClickListener(ItemClickListener itemClickListener) {
            this.mClickListener = itemClickListener;
        }

        // parent activity will implement this method to respond to click events
        public interface ItemClickListener {
            void onItemClick(View view, int position);
        }
    }
}