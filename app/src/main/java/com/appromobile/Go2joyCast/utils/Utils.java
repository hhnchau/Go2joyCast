package com.appromobile.Go2joyCast.utils;

import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by appro on 11/12/2017.
 */

public class Utils {
    public static String cutString(String url) {
        String s = "";
        String temp = url.replace(".", "!");
        Matcher siteNameMatcher = Pattern.compile("!(.*?)!").matcher(temp);
        while (siteNameMatcher.find()) {
            s = siteNameMatcher.group(1);
        }
        if (s.equals("")) {
            siteNameMatcher = Pattern.compile("//(.*?)!").matcher(temp);
            while (siteNameMatcher.find()) {
                s = siteNameMatcher.group(1);
            }
        }

        return s;
    }

    public static String isUrl(String input) {
        if (Patterns.WEB_URL.matcher(input).matches()) {
            if (input.startsWith("http")) {
                return input;
            } else {
                return "http://" + input;
            }
        } else {
            return "https://www.google.com/search?q=" + input;
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            //imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static String convertSiteName(String url) {
        String s = "";
        Matcher siteNameMatcher = Pattern.compile("//(.*?)/").matcher(url + "/");
        while (siteNameMatcher.find()) {
            s = siteNameMatcher.group(1);
        }
        return s;
    }

    public static String cutUrl(String url) {
        String s = "";
        url = url.replace("\\", "!");
        Matcher siteNameMatcher = Pattern.compile("src=!(.*?)!").matcher(url);
        while (siteNameMatcher.find()) {
            s = siteNameMatcher.group(1);
        }
        if (s.length() > 2) {
            s = s.substring(1);
        }
        return s;
    }

    public static String regexUrl(String url) {
        String s = "";
        Matcher siteNameMatcher = Pattern.compile("src=\"(.*?)\"").matcher(url);
        while (siteNameMatcher.find()) {
            s = siteNameMatcher.group(1);
        }

        return s;
    }

    public static String regexYoutube(String url) {
        String s = "";
        url = url.replace("\\", "!");
        Matcher siteNameMatcher = Pattern.compile("href=!(.*?)!").matcher(url);
        while (siteNameMatcher.find()) {
            s = siteNameMatcher.group(1);
        }
        if (s.length() > 2) {
            s = s.substring(1);
        }
        return s;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public static String getIdYoutube(String url) {
        if (url.contains("watch?ajax")) {
            Matcher siteNameMatcher = Pattern.compile("v=([^\"]+)").matcher(url);
            while (siteNameMatcher.find()) {
                String s = siteNameMatcher.group(1);
                Log.d("LINKS-ID-YOUTUBE", s);
                return s;
            }
        } else {
            Matcher siteNameMatcher = Pattern.compile("v=([^\"]{11})").matcher(url);
            while (siteNameMatcher.find()) {
                String s = siteNameMatcher.group(1);
                Log.d("LINKS-ID-YOUTUBE", s);
                return s;
            }
        }
        return "";
    }

    public static String getTitleHdViet(String url) {
        Matcher siteNameMatcher = Pattern.compile("slug=([^\"]+)").matcher(url);
        while (siteNameMatcher.find()) {
            String s = siteNameMatcher.group(1);
            Log.d("LINKS-HDVIET-TITLE", s);
            return s;
        }

        return "";
    }
}
