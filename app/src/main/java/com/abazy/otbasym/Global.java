package com.abazy.otbasym;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;
import org.folg.gedcom.model.Gedcom;
import org.folg.gedcom.model.Media;

import java.io.File;
import java.util.Locale;

public class Global extends MultiDexApplication {

    public static Gedcom gc;
    public static Context context;
    public static Settings settings;
    public static String indi; // ID of the selected person displayed across the app
    /**
     * Which parents' family to show in the diagram, usually 0.
     */
    public static int familyNum;
    static View mainView;
    public static boolean edited; // There has been an editing in ProfileActivity or in DetailActivity and therefore the content of the previous pages must be updated
    static boolean shouldSave; // The Gedcom content has been changed and needs to be saved
    /**
     * Path where a camera app puts the taken photo.
     */
    public static String pathOfCameraDestination;
    public static Media croppedMedia; // Temporary parking of the Media in the cropping process


    /**
     * This is called when the application starts, and also when it is restarted.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        start(context);
    }

    public static void start(Context context) {
        File settingsFile = new File(context.getFilesDir(), "settings.json");
        try {
            String jsonString = FileUtils.readFileToString(settingsFile, "UTF-8");
//            jsonString = updateSettings(jsonString);
            Gson gson = new Gson();
            settings = gson.fromJson(jsonString, Settings.class);
        } catch (Exception e) {
            // At first boot avoids to show the toast saying that settings.json doesn't exist
            if (!(e instanceof java.io.FileNotFoundException)) {
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (settings == null) {
            settings = new Settings();
            settings.init();
            // Restores possibly lost trees
            for (File file : context.getFilesDir().listFiles()) {
                String name = file.getName();
                if (file.isFile() && name.endsWith(".json")) {
                    try {
                        int treeId = Integer.parseInt(name.substring(0, name.lastIndexOf(".json")));
                        File mediaDir = new File(context.getExternalFilesDir(null), String.valueOf(treeId));
                        settings.trees.add(new Settings.Tree(treeId, String.valueOf(treeId),
                                mediaDir.exists() ? mediaDir.getPath() : null,
                                0, 0, null, 0));
                    } catch (Exception e) {
                    }
                }
            }
            // Some tree has been restored
            if (!settings.trees.isEmpty())
                settings.referrer = null;
            settings.save();
        }
        // Diagram settings were (probably) introduced in version 0.7.4
        if (settings.diagram == null) {
            settings.diagram = new Settings.Diagram().init();
            settings.save();
        }
    }

    /**
     * Modifications to the text coming from files/settings.json
     */


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Keep the app locale if system language is changed while the app is running
        Locale appLocale = AppCompatDelegate.getApplicationLocales().get(0);
        if (appLocale != null) {
            Locale.setDefault(appLocale); // Keep the gedcom.jar library locale
            newConfig.setLocale(appLocale);
            getApplicationContext().getResources().updateConfiguration(newConfig, null); // Keep global context
        }
        super.onConfigurationChanged(newConfig);
    }
}
