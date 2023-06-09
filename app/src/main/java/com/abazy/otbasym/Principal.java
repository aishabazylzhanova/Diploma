package com.abazy.otbasym;

import static com.abazy.otbasym.Global.gc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.abazy.otbasym.Menu.BaurFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.List;

import com.abazy.otbasym.Сonstants.Choice;
import com.abazy.otbasym.Menu.FamiliesFragment;
import com.abazy.otbasym.Menu.PersonsFragment;
import com.abazy.otbasym.Menu.MediaFragment;
import com.abazy.otbasym.Menu.NotesFragment;
import com.abazy.otbasym.Visitors.MediaList;
import com.abazy.otbasym.Visitors.NoteList;

public class Principal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView menuPrincipe;
    List<Integer> idMenu = Arrays.asList(R.id.nav_diagramma, R.id.nav_persone, R.id.nav_famiglie,
            R.id.nav_media, R.id.nav_note, R.id.nav_baur);
    List<Class> frammenti = Arrays.asList(DiagramFragment.class, PersonsFragment.class, FamiliesFragment.class,
            MediaFragment.class, NotesFragment.class, BaurFragment.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_navigation);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.scatolissima);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        menuPrincipe = findViewById(R.id.menu);
        menuPrincipe.setNavigationItemSelectedListener(this);
        Global.mainView = drawerLayout;
        U.ensureGlobalGedcomNotNull(gc);
        furnishMenu();

        if (savedInstanceState == null) {  // loads the home only the first time, not rotating the screen
            Fragment fragment;
            String backName = null; // Label to locate diagram in fragment backstack
            if (getIntent().getBooleanExtra(Choice.PERSON, false))
                fragment = new PersonsFragment();
            else if (getIntent().getBooleanExtra(Choice.MEDIA, false))
                fragment = new MediaFragment();
            else if (getIntent().getBooleanExtra(Choice.NOTE, false))
                fragment = new NotesFragment();
            else {
                fragment = new DiagramFragment();
                backName = "diagram";
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.contenitore_fragment, fragment)
                    .addToBackStack(backName).commit();
        }

        menuPrincipe.getHeaderView(0).findViewById(R.id.menu_alberi).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(Principal.this, TreesActivity.class));
        });



    }

    //
    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (!(fragment instanceof NewRelativeDialog))
            aggiornaInterfaccia(fragment);
    }


    @Override
    public void onRestart() {
        super.onRestart();
        if (Global.edited) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contenitore_fragment);
            if (fragment instanceof DiagramFragment) {
                ((DiagramFragment)fragment).forceDraw = true; // Così ridisegna il diagramma
            } else if (fragment instanceof PersonsFragment) {
                ((PersonsFragment)fragment).restart();
            } else if (fragment instanceof FamiliesFragment) {
                ((FamiliesFragment)fragment).refresh(FamiliesFragment.What.RELOAD);
            } else if (fragment instanceof MediaFragment) {
                ((MediaFragment)fragment).recreate();
            } else {
                recreate(); // questo dovrebbe andare a scomparire man mano
            }
            furnishMenu(); // To display the Save button and update items count
            Global.edited = false;
        }
    }


    private boolean frammentoAttuale(Class classe) {
        Fragment attuale = getSupportFragmentManager().findFragmentById(R.id.contenitore_fragment);
        return classe.isInstance(attuale);
    }

    // Update title
    void furnishMenu() {
        NavigationView navigation = drawerLayout.findViewById(R.id.menu);
        View menuHeader = navigation.getHeaderView(0);

        TextView mainTitle = menuHeader.findViewById(R.id.menu_titolo);

        mainTitle.setText("");
        if (Global.gc != null) {
            MediaList cercaMedia = new MediaList(Global.gc, 3);
            Global.gc.accept(cercaMedia);

            mainTitle.setText(Global.settings.getCurrentTree().title);
            if (Global.settings.expert) {
                TextView treeNumView = menuHeader.findViewById(R.id.menu_number);
                treeNumView.setText(String.valueOf(Global.settings.openTree));
                treeNumView.setVisibility(ImageView.VISIBLE);
            }
            // Put count of existing records in menu items
            Menu menu = navigation.getMenu();
            for (int i = 1; i <= 5; i++) {
                int count = 0;
                switch (i) {
                    case 1:
                        count = gc.getPeople().size();
                        break;
                    case 2:
                        count = gc.getFamilies().size();
                        break;
                    case 3:
                        MediaList mediaList = new MediaList(gc, 0);
                        gc.accept(mediaList);
                        count = mediaList.list.size();
                        break;
                    case 4:
                        NoteList notesList = new NoteList();
                        gc.accept(notesList);
                        count = notesList.noteList.size() + gc.getNotes().size();
                        break;


                }
                TextView countView = menu.getItem(i).getActionView().findViewById(R.id.menu_item_text);
                if (count > 0)
                    countView.setText(String.valueOf(count));
                else
                    countView.setVisibility(View.GONE);
            }
        }
        // Save button



    }


    void aggiornaInterfaccia(Fragment fragment) {
        if (fragment == null)
            fragment = getSupportFragmentManager().findFragmentById(R.id.contenitore_fragment);
        if (fragment != null) {
            int numFram = frammenti.indexOf(fragment.getClass());
            if (menuPrincipe != null)
                menuPrincipe.setCheckedItem(idMenu.get(numFram));
            if (toolbar == null)
                toolbar = findViewById(R.id.toolbar);
            if (toolbar != null)
                toolbar.setVisibility(numFram == 0 ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                // Returns TreesActivity instead of reviewing the first backstack diagram
                super.onBackPressed();
            } else
                aggiornaInterfaccia(null);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        try {
            fragment = (Fragment)frammenti.get(idMenu.indexOf(item.getItemId())).newInstance();
        } catch (Exception e) {
        }
        if (fragment != null) {
            if (fragment instanceof DiagramFragment) {
                int cosaAprire = 0; // Show the chart without asking about multiple parents
                //  If I'm already in diagram and I click Diagram, it shows the root person
                if (frammentoAttuale(DiagramFragment.class)) {
                    Global.indi = Global.settings.getCurrentTree().root;
                    cosaAprire = 1; // Possibly ask about multiple parents
                }
                U.askWhichParentsToShow(this, Global.gc.getPerson(Global.indi), cosaAprire);
            } else {
                FragmentManager fm = getSupportFragmentManager();
                // Remove previous fragment from story if it is the same one we are about to see
                if (frammentoAttuale(fragment.getClass())) fm.popBackStack();
                fm.beginTransaction().replace(R.id.contenitore_fragment, fragment).addToBackStack(null).commit();
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Automatically opens the 'Sort by' sub-menu
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        MenuItem item0 = menu.getItem(0);
        if (item0.getTitle().equals(getString(R.string.order_by))) {
            item0.setVisible(false); // a little hack to prevent options menu to appear
            new Handler().post(() -> {
                item0.setVisible(true);
                menu.performIdentifierAction(item0.getItemId(), 0);
            });
        }
        return super.onMenuOpened(featureId, menu);
    }
}
