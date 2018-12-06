package com.mualab.org.user.data.local.prefs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import com.mualab.org.user.activity.authentication.LoginActivity;
import com.mualab.org.user.data.model.SearchBoard.RefineSearchBoard;
import com.mualab.org.user.data.model.User;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Session {

    private static final String PREF_NAME = "imLink";
    private static final String PREF_NAME2 = "appSession";
    private static final String IS_LOGGEDIN = "isLoggedIn";
    private static final String IS_FIrebaseLogin = "isFirebaseLogin";
    private static final String IS_UPDATE_UID = "isUpdateUid";
    private static final String SAVESEARCH = "save_search";
    private Context _context;
    private SharedPreferences mypref, mypref2;
    private SharedPreferences.Editor editor, editor2;

    public Session(Context context) {
        this._context = context;
        mypref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mypref2 = _context.getSharedPreferences(PREF_NAME2, Context.MODE_PRIVATE);
        editor = mypref.edit();
        editor2 = mypref2.edit();
        editor.apply();
        editor2.apply();
    }


    public boolean isUpdateUid() {
        return mypref.getBoolean(IS_UPDATE_UID, false);
    }

    public void setUpdateUid(boolean isUpdate) {
        editor.putBoolean(IS_UPDATE_UID, isUpdate);
        editor.commit();
    }

    public void saveFilter(RefineSearchBoard searchBoard){
        Gson gson = new Gson();
        String board = gson.toJson(searchBoard);
        editor.putString(SAVESEARCH, board);
        editor.commit();
    }

    public RefineSearchBoard getSaveSearch (){
        Gson gson = new Gson();
        RefineSearchBoard board = gson.fromJson(mypref.getString(SAVESEARCH,""),RefineSearchBoard.class);
        return board;
    }

    public void createSession(User user) {
        createSession(user, false);
    }

    public void createSession(User user, boolean isFirebaseLogin) {
        Gson gson = new Gson();
        String json = gson.toJson(user); // myObject - instance of MyObject
        editor.putString("user", json);
        editor.putBoolean(IS_LOGGEDIN, true);
        editor.putBoolean(IS_FIrebaseLogin, isFirebaseLogin);
        editor.putString("authToken", user.authToken);
        editor.commit();
    }

    public void setPassword(String pwd) {
        try {
            byte[] data = pwd.getBytes("UTF-8");
            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
            editor.putString("pwd", base64);
            editor.commit();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    public String getPassword(){
        // Receiving side
        try {
            byte[] data = Base64.decode(mypref.getString("pwd", null), Base64.DEFAULT);
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public User getUser() {
        Gson gson = new Gson();
        String string = mypref.getString("user", "");
        if (!string.isEmpty())
            return gson.fromJson(string, User.class);
        else return new User();
    }

    public String getAuthToken() {
        return mypref.getString("authToken", "");
    }

    public boolean getIsOutCallFilter() {
        return mypref.getBoolean("outcall",false);
    }

    public void setIsOutCallFilter(boolean value){
        editor.putBoolean("outcall", value);
        this.editor.commit();
    }

    public void saveCurrentLocation(String lat,String lng){
        editor.putString("lat", lat);
        editor.putString("lng", lng);
        this.editor.commit();
    }

    public String getCurrentLatltude() {
        return mypref.getString("lat", "");
    }

    public String getCurrentLongitude() {
        return mypref.getString("lng", "");
    }


    public String getUserChangedLocName() {
        return mypref.getString("locName", "");
    }

    public void setUserChangedLocName(String locName){
        editor.putString("locName", locName);
        this.editor.commit();
    }

    public boolean getIsFirebaseLogin() {
        return mypref.getBoolean(IS_FIrebaseLogin, false);
    }


    public void logout() {
        editor.clear();
        editor.apply();
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent showLogin = new Intent(_context, LoginActivity.class);
        showLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        showLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(showLogin);
    }

    public boolean isLoggedIn() {
        return mypref.getBoolean(IS_LOGGEDIN, false);
    }

}
