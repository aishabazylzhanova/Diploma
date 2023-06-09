package com.abazy.otbasym;

import static com.abazy.otbasym.Global.gc;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.theartofdev.edmodo.cropper.CropImage;

import org.folg.gedcom.model.EventFact;
import org.folg.gedcom.model.Family;
import org.folg.gedcom.model.Media;
import org.folg.gedcom.model.MediaRef;
import org.folg.gedcom.model.Name;
import org.folg.gedcom.model.Note;
import org.folg.gedcom.model.NoteRef;
import org.folg.gedcom.model.Person;
import org.folg.gedcom.model.SourceCitation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.abazy.otbasym.Сonstants.Choice;
import com.abazy.otbasym.Сonstants.Gender;
import com.abazy.otbasym.Details.EventActivity;
import com.abazy.otbasym.Details.NameActivity;
import com.abazy.otbasym.Details.NoteActivity;
import com.abazy.otbasym.Menu.PersonsFragment;
import com.abazy.otbasym.Menu.MediaFragment;
import com.abazy.otbasym.Menu.NotesFragment;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class ProfileActivity extends AppCompatActivity {

    Person one;
    TabLayout tabLayout;
    Fragment[] tabs = new Fragment[3];
    String[] mainEventTags = {"BIRT", "RESI", "OCCU", "DEAT", "BURI"};
    List<Pair<String, String>> otherEvents; // List of tag + label

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        U.ensureGlobalGedcomNotNull(gc);
        one = (Person)Memory.getObject();
        // If the app goes into the background and is stopped, 'Memory' is reset and therefore 'one' will be null
        if (one == null && bundle != null) {
            one = gc.getPerson(bundle.getString("idUno")); //The individual's id is saved in the bundle
            Memory.setFirst(one); // Otherwise the memory is without a stack
        }
        if (one == null) return; // Rarely does the bundle not do its job
        Global.indi = one.getId();
        setContentView(R.layout.person);

        // Barra
        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // brings up the back arrow and menu

        // Give the page view an adapter that manages the three tabs
        ViewPager viewPager = findViewById(R.id.profile_pager);
        ImpaginatoreSezioni impaginatoreSezioni = new ImpaginatoreSezioni();
        viewPager.setAdapter(impaginatoreSezioni);

        // enriches the tablayout
        tabLayout = findViewById(R.id.profile_tabs);
        tabLayout.setupWithViewPager(viewPager); // altrimenti il testo nei TabItem scompare (?!)
        tabLayout.getTabAt(0).setText(R.string.media);
        tabLayout.getTabAt(1).setText(R.string.events);
        tabLayout.getTabAt(2).setText(R.string.relatives);
        tabLayout.getTabAt(getIntent().getIntExtra("scheda", 1)).select();

        // to animate the FAB
        final FloatingActionButton fab = findViewById(R.id.fab);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position,  
                                       float positionOffset, 
                                       int positionOffsetPixels) {
                if (positionOffset > 0)
                    fab.hide();
                else
                    fab.show();
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // List of other events
        String[] otherEventTags = {"CHR", "CREM", "ADOP", "BARM", "BATM", "BLES", "CONF", "FCOM", "ORDN", //Events
                "NATU", "EMIG", "IMMI", "CENS", "PROB", "WILL", "GRAD", "RETI", "EVEN",
                "CAST", "DSCR", "EDUC", "NATI", "NCHI", "PROP", "RELI", "SSN", "TITL", // Attributes
                "_MILT"}; // User-defined
        otherEvents = new ArrayList<>();
        for (String tag : otherEventTags) {
            EventFact event = new EventFact();
            event.setTag(tag);
            String label = event.getDisplayType();
            if (Global.settings.expert)
                label += " — " + tag;
            otherEvents.add(new Pair<>(tag, label));
        }
        // Alphabetically sorted by label
        Collections.sort(otherEvents, (item1, item2) -> item1.second.compareTo(item2.second));
    }

    class ImpaginatoreSezioni extends FragmentPagerAdapter {

        ImpaginatoreSezioni() {
            super(getSupportFragmentManager());
        }

        @Override 
        public Fragment getItem(int position) {
            if (position == 0)
                tabs[0] = new ProfileMediaFragment();
            else if (position == 1)
                tabs[1] = new ProfileFactsFragment();
            else if (position == 2)
                tabs[2] = new ProfileRelativesFragment();
            return tabs[position];
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (one == null || Global.edited)
            one = gc.getPerson(Global.indi);

        if (one == null) { // going back to the Record of an individual who has been eliminated
            onBackPressed();
            return;
        }

        // Person ID in the header
        TextView idView = findViewById(R.id.profile_id);
        if (Global.settings.expert) {
            idView.setText("INDI " + one.getId());
            idView.setOnClickListener(v -> {
                U.editId(this, one, this::refresh);
            });
        } else idView.setVisibility(View.GONE);
        // Person name in the header
        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.profile_toolbar_layout);
        toolbarLayout.setTitle(U.properName(one));
        toolbarLayout.setExpandedTitleTextAppearance(R.style.AppTheme_ExpandedAppBar);
        toolbarLayout.setCollapsedTitleTextAppearance(R.style.AppTheme_CollapsedAppBar);
        setImages();
        if (Global.edited) {
            // Reload the 3 tabs coming back to the profile
            for (Fragment tab : tabs) {
                if (tab != null) { // At the first activity creation they are null
                    getSupportFragmentManager().beginTransaction().detach(tab).commit();
                    getSupportFragmentManager().beginTransaction().attach(tab).commit();
                }
            }
            invalidateOptionsMenu();
        }

        // Menu FAB
        findViewById(R.id.fab).setOnClickListener(vista -> {
            PopupMenu popup = new PopupMenu(this, vista);
            Menu menu = popup.getMenu();
            switch (tabLayout.getSelectedTabPosition()) {
                case 0: //  Media
                    menu.add(0, 10, 0, R.string.new_media);
                    menu.add(0, 11, 0, R.string.new_shared_media);
                    if (!gc.getMedia().isEmpty())
                        menu.add(0, 12, 0, R.string.link_shared_media);
                    break;
                case 1: //  Event
                    menu.add(0, 20, 0, R.string.name);
                    menu.add(0, 22, 0, R.string.note);

                    if (Gender.getGender(one) == Gender.NONE)
                        menu.add(0, 21, 0, R.string.sex);
                    // Main events
                    SubMenu eventSubMenu = menu.addSubMenu(R.string.event);
                    CharSequence[] mainEventLabels = {getText(R.string.birth), getText(R.string.residence), getText(R.string.occupation), getText(R.string.death), getText(R.string.burial)};
                    int i;
                    for (i = 0; i < mainEventLabels.length; i++) {
                        CharSequence label = mainEventLabels[i];
                        if (Global.settings.expert)
                            label += " — " + mainEventTags[i];
                        eventSubMenu.add(0, 40 + i, 0, label);
                    }

                    break;
                case 2: //  Family
                    menu.add(0, 30, 0, R.string.new_relative);
                    if (U.linkablePersons(one))
                        menu.add(0, 31, 0, R.string.link_person);
            }
            popup.show();
            popup.setOnMenuItemClickListener(item -> {
                CharSequence[] familiari = {getText(R.string.parent), getText(R.string.sibling), getText(R.string.partner), getText(R.string.child)};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                switch (item.getItemId()) {

                    case 0:
                        break;
                    // Media
                    case 10: // Search local media
                        F.displayImageCaptureDialog(this, null, 2173, one);
                        break;
                    case 11: // Search media object
                        F.displayImageCaptureDialog(this, null, 2174, one);
                        break;
                    case 12: // Link media in MediaFragment
                        Intent inten = new Intent(this, Principal.class);
                        inten.putExtra(Choice.MEDIA, true);
                        startActivityForResult(inten, 43614);
                        break;
                    case 20: // Create name
                        Name name = new Name();
                        name.setValue("//");
                        one.addName(name);
                        Memory.add(name);
                        startActivity(new Intent(this, NameActivity.class));
                        U.save(true, one);
                        break;
                    case 21: // Create sex
                        String[] sexNames = {getString(R.string.male), getString(R.string.female), getString(R.string.unknown)};
                        new AlertDialog.Builder(tabLayout.getContext())
                                .setSingleChoiceItems(sexNames, -1, (dialog, i) -> {
                                    EventFact gender = new EventFact();
                                    gender.setTag("SEX");
                                    String[] sexValues = {"M", "F", "U"};
                                    gender.setValue(sexValues[i]);
                                    one.addEventFact(gender);
                                    dialog.dismiss();
                                    ProfileFactsFragment.updateMaritalRoles(one);
                                    refresh();
                                    U.save(true, one);
                                }).show();
                        break;
                    case 22: // Create note
                        Note note = new Note();
                        note.setValue("");
                        one.addNote(note);
                        Memory.add(note);
                        startActivity(new Intent(this, NoteActivity.class));

                        U.save(true, one);
                        break;
                    case 23: // Create shared note
                        NotesFragment.newNote(this, one);
                        break;
                    case 24: // Link shared note
                        Intent intent = new Intent(this, Principal.class);
                        intent.putExtra(Choice.NOTE, true);
                        startActivityForResult(intent, 4074);
                        break;

                    // Family tab
                    case 30:
                        if (Global.settings.expert) {
                            DialogFragment dialog = new NewRelativeDialog(one, null, null, true, null);
                            dialog.show(getSupportFragmentManager(), "scegli");
                        } else {
                            builder.setItems(familiari, (dialog, quale) -> {
                                Intent intent1 = new Intent(getApplicationContext(), PersonEditorActivity.class);
                                intent1.putExtra("idPerson", one.getId());
                                intent1.putExtra("relation", quale + 1);
                                if (U.controllaMultiMatrimoni(intent1, this, null))
                                    return;
                                startActivity(intent1);
                            }).show();
                        }
                        break;
                    case 31:
                        if (Global.settings.expert) {
                            DialogFragment dialog = new NewRelativeDialog(one, null, null, false, null);
                            dialog.show(getSupportFragmentManager(), "scegli");
                        } else {
                            builder.setItems(familiari, (dialog, quale) -> {
                                Intent intent2 = new Intent(getApplication(), Principal.class);
                                intent2.putExtra("idPerson", one.getId());
                                intent2.putExtra(Choice.PERSON, true);
                                intent2.putExtra("relation", quale + 1);
                                if (U.controllaMultiMatrimoni(intent2, this, null))
                                    return;
                                startActivityForResult(intent2, 1401);
                            }).show();
                        }
                        break;
                    default:
                        String keyTag = null;
                        if (item.getItemId() >= 50) {
                            keyTag = otherEvents.get(item.getItemId() - 50).first;
                        } else if (item.getItemId() >= 40)
                            keyTag = mainEventTags[item.getItemId() - 40];
                        if (keyTag == null)
                            return false;
                        EventFact nuovoEvento = new EventFact();
                        nuovoEvento.setTag(keyTag);
                        switch (keyTag) {
                            case "OCCU":
                                nuovoEvento.setValue("");
                                break;
                            case "RESI":
                                nuovoEvento.setPlace("");
                                break;
                            case "BIRT":
                            case "DEAT":
                            case "CHR":
                            case "BAPM":
                            case "BURI":
                                nuovoEvento.setPlace("");
                                nuovoEvento.setDate("");
                        }
                        one.addEventFact(nuovoEvento);
                        Memory.add(nuovoEvento);
                        startActivity(new Intent(this, EventActivity.class));
                        U.save(true, one);
                }
                return true;
            });
        });
    }

    /* Displays an image in the profile header.
       The blurred background image is displayed in most cases (jpg, png, gif...).
       to_Do but not in case of a video preview, or image downloaded from the web with ZuppaMedia */
    void setImages() {
        ImageView imageView = findViewById(R.id.profile_image);
        Media media = F.showMainImageForPerson(Global.gc, one, imageView);
        // Same image blurred on background
        if (media != null) {
            String path = F.mediaPath(Global.settings.openTree, media);
            Uri uri = null;
            if (path == null)
                uri = F.mediaUri(Global.settings.openTree, media);
            if (path != null || uri != null) {
                RequestCreator creator;
                ImageView backImageView = findViewById(R.id.profile_background);
                backImageView.setColorFilter(ContextCompat.getColor(
                        this, R.color.primary_grayed), PorterDuff.Mode.MULTIPLY);
                if (path != null)
                    creator = Picasso.get().load("file://" + path);
                else
                    creator = Picasso.get().load(uri);
                creator.resize(200, 200).centerCrop()
                        .transform(new BlurTransformation(Global.context, 5, 1))
                        .into(backImageView);
            }
        }
    }

    // Refresh everyting without recreating the activity
    public void refresh() {
        // Name in the header
        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.profile_toolbar_layout);
        toolbarLayout.setTitle(U.properName(one));
        // Header images
        setImages();
        // ID in the header
        if (Global.settings.expert) {
            TextView idView = findViewById(R.id.profile_id);
            idView.setText("INDI " + one.getId());
        }
        // 3 tabs
        for (Fragment tab : tabs) {
            if (tab != null) {
                FragmentManager manager = getSupportFragmentManager();
                manager.beginTransaction().detach(tab).commit();
                manager.beginTransaction().attach(tab).commit();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("idUno", one.getId());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 2173) {
                Media media = new Media();
                media.setFileTag("FILE");
                one.addMedia(media);
                if (F.proposeCropping(this, null, data, media)) {
                    U.save(true, one);
                    return;
                }
            } else if (requestCode == 2174) {
                Media media = MediaFragment.newMedia(one);
                if (F.proposeCropping(this, null, data, media)) {
                    U.save(true, media, one);
                    return;
                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                F.endImageCropping(data);
                U.save(true);

                return;
            } else if (requestCode == 43614) {
                MediaRef rifMedia = new MediaRef();
                rifMedia.setRef(data.getStringExtra("mediaId"));
                one.addMediaRef(rifMedia);
            } else if (requestCode == 4074) { // Note
                NoteRef rifNota = new NoteRef();
                rifNota.setRef(data.getStringExtra("noteId"));
                one.addNoteRef(rifNota);
            } else if (requestCode == 50473) {
                SourceCitation citaz = new SourceCitation();
                citaz.setRef(data.getStringExtra("sourceId"));
                one.addSourceCitation(citaz);
            } else if (requestCode == 1401) {
                Object[] modificati = PersonEditorActivity.addParent(
                        data.getStringExtra("idPerson"),
                        data.getStringExtra("idParente"),
                        data.getStringExtra("idFamily"),
                        data.getIntExtra("relation", 0),
                        data.getStringExtra("collocazione"));
                U.save(true, modificati);
                return;
            }
            U.save(true, one);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) // if click back arrow in Crop Image
            Global.edited = true;
    }

    @Override
    public void onBackPressed() {
        Memory.clearStackAndRemove();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.diagram);
        String[] familyLabels = DiagramFragment.getFamilyLabels(this, one, null);
        if (familyLabels[0] != null)
            menu.add(0, 1, 0, familyLabels[0]);
        if (familyLabels[1] != null)
            menu.add(0, 2, 0, familyLabels[1]);
        if (Global.settings.getCurrentTree().root == null || !Global.settings.getCurrentTree().root.equals(one.getId()))
            menu.add(0, 3, 0, R.string.make_root);
        menu.add(0, 4, 0, R.string.modify);
        menu.add(0, 5, 0, R.string.delete);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0: // DiagramFragment
                U.askWhichParentsToShow(this, one, 1);
                return true;
            case 1: // Family as child
                U.askWhichParentsToShow(this, one, 2);
                return true;
            case 2: // Family as partner
                U.askWhichSpouceToShow(this, one, null);
                return true;
            case 3: // Set as root
                Global.settings.getCurrentTree().root = one.getId();
                Global.settings.save();
                Toast.makeText(this, getString(R.string.this_is_root, U.properName(one)), Toast.LENGTH_LONG).show();
                return true;
            case 4: // Edit
                Intent intent1 = new Intent(this, PersonEditorActivity.class);
                intent1.putExtra("idPerson", one.getId());
                startActivity(intent1);
                return true;
            case 5:    // Delete
                new AlertDialog.Builder(this).setMessage(R.string.really_delete_person)
                        .setPositiveButton(R.string.delete, (dialog, i) -> {
                            Family[] famiglie = PersonsFragment.deletePerson(this, one.getId());
                            if (!U.controllaFamiglieVuote(this, this::onBackPressed, true, famiglie))
                                onBackPressed();
                        }).setNeutralButton(R.string.cancel, null).show();
                return true;
            default:
                onBackPressed();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        F.permissionsResult(this, null, requestCode, permissions, grantResults, one);
    }
}
