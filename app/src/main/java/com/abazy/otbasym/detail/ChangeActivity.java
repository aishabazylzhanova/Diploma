package com.abazy.otbasym.detail;

import android.view.Menu;

import org.folg.gedcom.model.Change;
import org.folg.gedcom.model.DateTime;

import com.abazy.otbasym.DetailActivity;
import com.abazy.otbasym.R;
import com.abazy.otbasym.U;

/**
 * Detail of the change date and time of a record.
 * Date and time can't be edited here, as they are automatically updated on saving the tree.
 */
public class ChangeActivity extends DetailActivity {

    Change c;

    @Override
    public void format() {
        setTitle(R.string.change_date);
        placeSlug("CHAN");
        c = (Change)cast(Change.class);
        DateTime dateTime = c.getDateTime();
        if (dateTime != null) {
            if (dateTime.getValue() != null)
                U.place(box, getString(R.string.value), dateTime.getValue());
            if (dateTime.getTime() != null)
                U.place(box, getString(R.string.time), dateTime.getTime());
        }
        placeExtensions(c);
        U.placeNotes(box, c, true);
    }

    // Options menu not needed
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        return false;
    }
}
