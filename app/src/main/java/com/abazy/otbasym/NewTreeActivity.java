package com.abazy.otbasym;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.apache.commons.io.FileUtils;
import org.folg.gedcom.model.CharacterSet;
import org.folg.gedcom.model.Gedcom;
import org.folg.gedcom.model.GedcomVersion;
import org.folg.gedcom.model.Generator;
import org.folg.gedcom.model.Header;
import org.folg.gedcom.parser.JsonParser;

import java.io.File;
import java.util.Locale;

public class NewTreeActivity extends BaseActivity {

    ProgressBar progress;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.new_tree);
        progress = findViewById(R.id.new_progress);
        String referrer = Global.settings.referrer; // DataID from a share
        boolean esisteDataId = referrer != null && referrer.matches("\\d{14}");


        // Create an empty tree
        Button emptyTree = findViewById(R.id.new_empty_tree);
        if (esisteDataId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                emptyTree.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary_light)));
        }
        emptyTree.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.title_tree, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView).setTitle(R.string.title);
            TextView textView = dialogView.findViewById(R.id.nuovo_nome_testo);
            textView.setText(R.string.modify_later);
            textView.setVisibility(View.VISIBLE);
            EditText nuovoNome = dialogView.findViewById(R.id.nuovo_nome_albero);
            builder.setPositiveButton(R.string.create, (dialog, id) -> newTree(nuovoNome.getText().toString()))
                    .setNeutralButton(R.string.cancel, null).create().show();
            nuovoNome.setOnEditorActionListener((view, action, event) -> {
                if (action == EditorInfo.IME_ACTION_DONE) {
                    newTree(nuovoNome.getText().toString());
                    return true;
                }
                return false;
            });
            dialogView.postDelayed(() -> {
                nuovoNome.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(nuovoNome, InputMethodManager.SHOW_IMPLICIT);
            }, 300);
        });




    }

    // Create a brand new tree
    void newTree(String title) {
        int num = Global.settings.max() + 1;
        File jsonFile = new File(getFilesDir(), num + ".json");
        Global.gc = new Gedcom();
        Global.gc.setHeader(createHeader(jsonFile.getName()));
        Global.gc.createIndexes();
        JsonParser jp = new JsonParser();
        try {
            FileUtils.writeStringToFile(jsonFile, jp.toJson(Global.gc), "UTF-8");
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        Global.settings.add(new Settings.Tree(num, title, null, 0, 0, null, 0));
        Global.settings.openTree = num;
        Global.settings.save();
        onBackPressed();
        Toast.makeText(this, R.string.tree_created, Toast.LENGTH_SHORT).show();
    }

    public static Header createHeader(String nomeFile) {
        Header testa = new Header();
        Generator app = new Generator();
        app.setValue("Otbasym");
        app.setName("Otbasym");
        app.setVersion(BuildConfig.VERSION_NAME);
        testa.setGenerator(app);
        testa.setFile(nomeFile);
        GedcomVersion versione = new GedcomVersion();
        versione.setForm("LINEAGE-LINKED");
        versione.setVersion("5.5.1");
        testa.setGedcomVersion(versione);
        CharacterSet codifica = new CharacterSet();
        codifica.setValue("UTF-8");
        testa.setCharacterSet(codifica);
        Locale loc = new Locale(Locale.getDefault().getLanguage());
        testa.setLanguage(loc.getDisplayLanguage(Locale.ENGLISH));
        testa.setDateTime(U.actualDateTime());
        return testa;
    }

    // Back arrow in the toolbar like the hardware one
    @Override
    public boolean onOptionsItemSelected(MenuItem i) {
        onBackPressed();
        return true;
    }
}
