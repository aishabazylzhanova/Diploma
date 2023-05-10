package com.abazy.otbasym;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.facciata);

        // Set app locale for application context and resources (localized gedcom.jar library)
        Locale locale = AppCompatDelegate.getApplicationLocales().get(0); // Find app locale, or null if not existing
        if (locale != null) {
            Configuration config = getResources().getConfiguration();
            config.setLocale(locale);
            getApplicationContext().getResources().updateConfiguration(config, null); // Change locale both for static methods and jar library
        }

        /*
        Import of a tree occurring after clicking on various types of links:
            https://www.familygem.app/share.php?tree=20190802224208
                Eg. in a WhatsApp message
                Clicked in Chrome in old Androids opens the choice of the app including Family Gem to directly import the tree
                Normally opens the sharing page on the website
            intent://www.familygem.app/condivisi/20200218134922.zip#Intent;scheme=https;end
                Official link on the website's sharing page
                It is the only one that seems guarantee to work, in Chrome, in the browser inside Libero, in the L90 Browser
            https://www.familygem.app/condivisi/20190802224208.zip
                Direct URL to the ZIP file
                It works in old Androids, in new ones simply the file is downloaded
        */
        Intent intent = getIntent();
        Uri uri = intent.getData();
        // By opening the app from the Recents screen, avoids re-importing a newly imported shared tree
        boolean fromHistory = (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY;
        if (uri != null && !fromHistory) {
            String dataId;
            if (uri.getPath().equals("/share.php")) // click on the first message received
                dataId = uri.getQueryParameter("tree");
            else if (uri.getLastPathSegment().endsWith(".zip")) // click on the invitation page
                dataId = uri.getLastPathSegment().replace(".zip", "");
            else {
                U.toast(this, R.string.cant_understand_uri);
                return;
            }
        } else {
            Intent treesIntent = new Intent(this, TreesActivity.class);
            // Open last tree at startup
            if (Global.settings.loadTree) {
                treesIntent.putExtra("apriAlberoAutomaticamente", true);
                treesIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); // perhaps ineffective but so be it
            }
            startActivity(treesIntent);
        }
    }


}
