package org.eyeseetea.uicapp;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ScrollView;
import android.widget.Toast;

import org.eyeseetea.uicapp.views.EditCard;
import org.eyeseetea.uicapp.views.TextCard;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ScrollingActivity extends AppCompatActivity {

    //Flag to prevent the bad positive errors in the validation when the user clear all the fields
    public static boolean isValidationErrorActive =true;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about_us) {
            return true;
        }
        if(id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        createActionBar();
        initValues();
        refreshCode();
        hideKeyboardEvent();
    }



    private void hideKeyboardEvent() {
        (findViewById(R.id.container_scrolled)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(v);
                return true;
            }
        });

    }
    public void hideSoftKeyboard(View view){
        InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void refreshCode() {
        TextCard textView = (TextCard) findViewById(R.id.code_text);
        if(validateAllFields()){
            textView.setText(generateCode());
            findViewById(R.id.code_button).setEnabled(true);
        }
        else{
            textView.setText(getApplicationContext().getString(R.string.code_invalid));
            findViewById(R.id.code_button).setEnabled(false);
        }
    }

    private String generateCode() {
        String code="";
        code = addCodeChars(code, R.string.shared_key_mother);
        code = addCodeChars(code, R.string.shared_key_surname);
        code = addCodeChars(code, R.string.shared_key_district);

        Long defaultNoDate=Long.parseLong(getApplicationContext().getString(R.string.default_no_date));
        Long timestamp = getLongFromSharedPreference(R.string.shared_key_timestamp_date, defaultNoDate);
        Calendar newCalendar= Calendar.getInstance();
        newCalendar.setTimeInMillis(timestamp);
        String day = String.valueOf(Utils.getDay(newCalendar));
        String month = String.valueOf(Utils.getMonth(newCalendar));
        String year = String.valueOf(Utils.getYear(newCalendar));
        if(day.length()<2){
            day="0"+day;
        }
        code = code + day;
        if(month.length()<2){
            month="0"+month;
        }
        code = code + month;
        code = code + year.substring(year.length()-2);

        code = code + getStringFromSharedPreference(R.string.shared_key_sex).substring(0,1);
        return code.toUpperCase();
    }

    @NonNull
    private String addCodeChars(String code, int keyId) {
        String temporalValue = getStringFromSharedPreference(keyId);
        temporalValue = temporalValue.replace(" ", "");
        code = code + temporalValue.substring(temporalValue.length()-2);
        return code;
    }

    private boolean validateAllFields() {

        if(!validateText(R.string.shared_key_mother)) {
            return false;
        }

        if(!validateText(R.string.shared_key_surname)) {
            return false;
        }

        if(!validateText(R.string.shared_key_district)) {
            return false;
        }

        if(getStringFromSharedPreference(R.string.shared_key_sex).equals("")) {
            return false;
        }

        if(!validateDate()) {
            return false;
        }

        return true;
    }

    private boolean validateDate() {
        Long defaultNoDate=Long.parseLong(getApplicationContext().getString(R.string.default_no_date));
        Long timestamp = getLongFromSharedPreference(R.string.shared_key_timestamp_date, defaultNoDate);
        if(timestamp.equals(defaultNoDate)){
            return false;
        }
        Calendar savedDate = Calendar.getInstance();
        savedDate.setTimeInMillis(timestamp);
        //Not pass the validation if the saved data is bigger than today.
        if(savedDate.getTimeInMillis()>= Utils.getTodayFirstTimestamp().getTime()){
            return false;
        }
        return true;
    }

    private boolean validateText(int keyId) {
        String value = getStringFromSharedPreference(keyId);
        //At least two characters without numbers and with possible blank spaces
        String regExp="^[ A-zÀ-ÿ]*([A-zÀ-ÿ]{1,}[ ]*[A-zÀ-ÿ]{1,})[ A-zÀ-ÿ]*$";
        if(value.matches(regExp)){
            return true;
        }else {
            return false;
        }
    }
    /**
     * Creates the menu actionBar
     *
     * @return
     */
    private void createActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.action_bar_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Init the saved or new values and add the listeners in the app components
     *
     * @return
     */
    private void initValues() {
        //Init mother
        initTextValue((EditCard) findViewById(R.id.mother_edit_text), R.string.shared_key_mother, R.string.mother_error);
        //Init surname
        initTextValue((EditCard) findViewById(R.id.surname_edit_text), R.string.shared_key_surname, R.string.surname_error);
        //Init district
        initTextValue((EditCard) findViewById(R.id.district_edit_text), R.string.shared_key_district, R.string.district_error);

        //Init district
        initDate();

        //Init sex
        initSex(R.string.shared_key_sex);
    }

    private void initDate() {
        EditCard dateEditCard= (EditCard)findViewById(R.id.date_value);
        dateEditCard.setInputType(0);
        dateEditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (findViewById(R.id.date_value)).requestFocus();
                showDatePicker(v);
            }
        });
        //This listener solve a problem when the user click on dateEditText but other editText has the focus.
        dateEditCard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                (findViewById(R.id.date_value)).requestFocus();
                return false;
            }
        });
        recoveryAndShowDate();
    }

    private void initSex(final int keyId) {
        String value= getStringFromSharedPreference(keyId);
        //Set value if exist in sharedPreferences
        if(!value.equals("")){
            final String male=getApplication().getApplicationContext().getString(R.string.sex_male);
            String female=getApplication().getApplicationContext().getString(R.string.sex_female);
            String trasngender=getApplication().getApplicationContext().getString(R.string.sex_transgender);
            if(value.equals(male)){
                onMaleClicked(null);
            }else if( value.equals(female)){
                onFemaleClicked(null);
            }else if (value.equals(trasngender)){
                onTransgenderClicked(null);
            }
            else{
                (findViewById(R.id.radio_male)).setEnabled(false);
                (findViewById(R.id.radio_female)).setEnabled(false);
                (findViewById(R.id.radio_transgender)).setEnabled(false);
            }
            //Refresh the generated code
            refreshCode();
        }
    }


    /**
     * Init editText and listeners
     *
     */
    private void initTextValue(final EditCard editText, final int keyId, final int errorId) {
        //Has value? show it
        String value= getStringFromSharedPreference(keyId);
        if(!value.equals("")){
            editText.setText(value);
        }
        editText.setFilters(new InputFilter[] { Utils.filter });
        //Editable? add listener
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Ignore the clear fields validation.
                String oldValue=getStringFromSharedPreference(keyId);
                putStringInSharedPreference(String.valueOf(s),keyId);
                if(!validateText(keyId) && isValidationErrorActive){
                    editText.setError(getApplicationContext().getString(errorId));
                }
                else{
                    editText.setError(null);
                }
                //Refresh the generated code
                refreshCode();
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v.getId() == editText.getId() && !hasFocus) {

                    InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                }
            }
        });

    }

    /**
     * Gets the string value for the given key
     * @return
     */
    private String getStringFromSharedPreference(int keyId){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getString(getApplicationContext().getResources().getString(keyId), "");
    }
    /**
     *  Puts the string value in the given key
     * @return
     */
    private void putStringInSharedPreference(String value, int keyId){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString(getApplication().getBaseContext().getString(keyId), value);
        editor.commit();
    }
    /**
     *  Puts the Long value in the given key
     * @return
     */
    private void putLongInSharedPreferences(Long value, int keyId){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putLong(getApplication().getBaseContext().getString(keyId), value);
        editor.commit();
    }
    /**
     *  Puts the Long value in the given key
     * @return
     */
    private Long getLongFromSharedPreference(int keyId, Long defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getLong(getApplicationContext().getResources().getString(keyId), Long.parseLong(defaultValue+""));
    }

    /**
     *  On click on sex male this method save the male value
     * @return
     */
    public void onMaleClicked(View view) {
        putStringInSharedPreference(getApplicationContext().getString(R.string.sex_male), R.string.shared_key_sex);
        (findViewById(R.id.radio_male)).setActivated(true);
        (findViewById(R.id.radio_female)).setActivated(false);
        (findViewById(R.id.radio_transgender)).setActivated(false);
        refreshCode();
    }


    /**
     *  On click on sex female this method save the female value
     * @return
     */
    public void onFemaleClicked(View view) {
        putStringInSharedPreference(getApplicationContext().getString(R.string.sex_female), R.string.shared_key_sex);
        (findViewById(R.id.radio_male)).setActivated(false);
        (findViewById(R.id.radio_female)).setActivated(true);
        (findViewById(R.id.radio_transgender)).setActivated(false);
        refreshCode();
    }

    /**
     *  On click on sex transgender this method save the transgender value
     * @return
     */
    public void onTransgenderClicked(View view) {
        putStringInSharedPreference(getApplicationContext().getString(R.string.sex_transgender), R.string.shared_key_sex);
        (findViewById(R.id.radio_male)).setActivated(false);
        (findViewById(R.id.radio_female)).setActivated(false);
        (findViewById(R.id.radio_transgender)).setActivated(true);
        refreshCode();
    }

    /**
     *  On click on copy button this method copy the code in the clipboard.
     * @return
     */
    public void copyCode(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText((getApplicationContext().getString(R.string.code_copy)), ((TextCard)findViewById(R.id.code_text)).getText());
        clipboard.setPrimaryClip(clip);
    }

    /**
     *  Clear shared preferences and reInit values and refresh generated code.
     * @return
     */
    public void clearFields(View view) {
        isValidationErrorActive=false;
        ((EditCard) findViewById(R.id.mother_edit_text)).setText("");
        putStringInSharedPreference("", R.string.shared_key_mother);
        ((EditCard) findViewById(R.id.surname_edit_text)).setText("");
        putStringInSharedPreference("", R.string.shared_key_surname);
        ((EditCard) findViewById(R.id.district_edit_text)).setText("");
        putStringInSharedPreference("", R.string.shared_key_district);

        (findViewById(R.id.radio_male)).setActivated(false);
        (findViewById(R.id.radio_female)).setActivated(false);
        (findViewById(R.id.radio_transgender)).setActivated(false);

        putStringInSharedPreference("", R.string.shared_key_sex);
        Long defaultNoDate = Long.parseLong(getApplicationContext().getString(R.string.default_no_date));
        putLongInSharedPreferences(defaultNoDate, R.string.shared_key_timestamp_date);

        refreshCode();
        isValidationErrorActive=true;
        //move to up
        runOnUiThread( new Runnable(){
            @Override
            public void run(){
                ((NestedScrollView)findViewById(R.id.nested_scroll_view)).fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    /**
     *  Date editText listener
     * @return
     */
    public void showDatePicker(View view) {
        new DatePickerListener(view);
    }


    /**
     * DatepickerListener
     * @return
     */
    public class DatePickerListener implements Button.OnClickListener {
        int day,month,year;
        public DatePickerListener(View v) {
            Long defaultNoDate=Long.parseLong(getApplicationContext().getString(R.string.default_no_date));
            Long timestamp=getLongFromSharedPreference(R.string.shared_key_timestamp_date,defaultNoDate);
            if(timestamp.equals(defaultNoDate)){
                //Set new calendar if the timestamp is a default date and set day,month,year.
                Calendar newCalendar = Calendar.getInstance();
                convertCalendarToLocalVariables(newCalendar);
            }
            else{
                //Parse the saved date in SharedPreference to calendar and set day,month,year.
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.setTimeInMillis(timestamp);
                convertCalendarToLocalVariables(newCalendar);
            }
            onClick(v);
        }

        @Override
        public void onClick(final View v) {
            if (!v.isShown()) {
                return;
            }
            DatePickerDialog.OnDateSetListener datepickerlistener = new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int newYear, int newMonthOfYear, int newDayOfMonth) {
                    Calendar newCalendar = Calendar.getInstance();
                    newCalendar.set(newYear, newMonthOfYear, newDayOfMonth);
                    convertCalendarToLocalVariables(newCalendar);
                    putLongInSharedPreferences(newCalendar.getTimeInMillis(), R.string.shared_key_timestamp_date);
                    recoveryAndShowDate();
                    if(!validateDate()){
                        EditCard editCard = (EditCard)findViewById(R.id.date_value);
                        editCard.setError(getApplicationContext().getString(R.string.date_error));
                    }else {
                        EditCard editCard = (EditCard)findViewById(R.id.date_value);
                        editCard.setError(null);
                        //Refresh the generated code
                    }
                    refreshCode();
                }
            };

            //Init a datepicker with the old values if exist, of with new values.
            DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), datepickerlistener, year, month-1, day);
            datePickerDialog.show();

            //Hide the week numbers on the datepickerdialog
            try {
                if(datePickerDialog.getDatePicker().getCalendarView()!=null)
                    datePickerDialog.getDatePicker().getCalendarView().setShowWeekNumber(false);
                //In API23+ the showweeknumber is deprecated and week numbers is not shown in the phone but the application crash
                //https://developer.android.com/reference/android/widget/CalendarView.html#setShowWeekNumber(boolean)
            }catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }

        }

        /**
         *  Used to set the datepicker local variables of  day/month/year
         * @return
         */
        private void convertCalendarToLocalVariables(Calendar calendar) {
            day = Utils.getDay(calendar);
            month = Utils.getMonth(calendar);
            year = Utils.getYear(calendar);
        }
    }

    /**
     *  Set date in day/month/year textviews
     * @return
     */
    private void showDate(Calendar calendar) {
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat("d MMM yyyy");
        String calendarDay= simpleDateFormat.format(calendar.getTime());
        EditCard dateEditCard =(EditCard) findViewById(R.id.date_value);
        dateEditCard.setText(calendarDay);
    }

    /**
     *  Recovery date from sharedPreferences and show in textviews
     * @return
     */
    private void recoveryAndShowDate() {
        Long defaultNoDate=Long.parseLong(getApplicationContext().getString(R.string.default_no_date));
        Long timestamp=getLongFromSharedPreference(R.string.shared_key_timestamp_date,defaultNoDate);
        Calendar calendar;
        if(timestamp.equals(defaultNoDate)){
             calendar = Calendar.getInstance();
        }
        else{
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
        }
        showDate(calendar);
    }
}
