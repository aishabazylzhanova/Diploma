package com.abazy.otbasym.Details;

import static com.abazy.otbasym.Global.gc;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import org.folg.gedcom.model.ChildRef;
import org.folg.gedcom.model.EventFact;
import org.folg.gedcom.model.Family;
import org.folg.gedcom.model.ParentFamilyRef;
import org.folg.gedcom.model.Person;
import org.folg.gedcom.model.SpouseFamilyRef;
import org.folg.gedcom.model.SpouseRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.abazy.otbasym.DetailActivity;
import com.abazy.otbasym.Global;
import com.abazy.otbasym.PersonEditorActivity;
import com.abazy.otbasym.Memory;
import com.abazy.otbasym.ProfileActivity;
import com.abazy.otbasym.R;
import com.abazy.otbasym.U;
import com.abazy.otbasym.Сonstants.Gender;
import com.abazy.otbasym.Сonstants.Relation;
import com.abazy.otbasym.Сonstants.Status;

public class FamilyActivity extends DetailActivity {

    Family f;
    static String[] lineageTexts = {U.s(R.string.undefined) + " (" + U.s(R.string.birth).toLowerCase() + ")",
            U.s(R.string.birth), U.s(R.string.adopted), U.s(R.string.foster)};
    static String[] lineageTypes = {null, "birth", "adopted", "foster"};

    @Override
    public void format() {
        setTitle(R.string.family);
        f = (Family)cast(Family.class);
        placeSlug("FAM", f.getId());
        for (SpouseRef husbandRef : f.getHusbandRefs())
            addMember(husbandRef, Relation.PARTNER);
        for (SpouseRef wifeRef : f.getWifeRefs())
            addMember(wifeRef, Relation.PARTNER);
        for (ChildRef childRef : f.getChildRefs())
            addMember(childRef, Relation.CHILD);
        for (EventFact ef : f.getEventsFacts()) {
            place(writeEventTitle(f, ef), ef);
        }
        placeExtensions(f);
        U.placeNotes(box, f, true);
        U.placeMedia(box, f, true);
        U.placeSourceCitations(box, f);
        U.placeChangeDate(box, f.getChange());
    }

    /**
     * Adds a member to the family.
     */
    void addMember(SpouseRef sr, Relation relation) {
        Person p = sr.getPerson(gc);
        if (p == null) return;
        View personView = U.placeIndividual(box, p, getRole(p, f, relation, true) + writeLineage(p, f));
        personView.setTag(R.id.tag_object, p); // For the context menu in DetailActivity

        /* Ref inside the individual towards the family.
           If the same person is present several times with the same role (partner or child) in the same family
           the 2 following loops identify in the person the first FamilyRef (INDI.FAMS / INDI.FAMC) that refers to that family.
           They do not take the FamilyRef with the same index as the corresponding Ref in the family (FAM.HUSB / FAM.WIFE).
           It could be a problem in case of 'Unlink person', but not anymore because all the Family content is reloaded.
         */
        if (relation == Relation.PARTNER) {
            for (SpouseFamilyRef sfr : p.getSpouseFamilyRefs())
                if (f.getId().equals(sfr.getRef())) {
                    personView.setTag(R.id.tag_spouse_family_ref, sfr);
                    break;
                }
        } else if (relation == Relation.CHILD) {
            for (ParentFamilyRef pfr : p.getParentFamilyRefs())
                if (f.getId().equals(pfr.getRef())) {
                    personView.setTag(R.id.tag_spouse_family_ref, pfr);
                    break;
                }
        }
        personView.setTag(R.id.tag_spouse_ref, sr);
        registerForContextMenu(personView);
        personView.setOnClickListener(v -> {
            List<Family> parentFam = p.getParentFamilies(gc);
            List<Family> spouseFam = p.getSpouseFamilies(gc);
            // A spouse with one or more families in which he is a child
            if (relation == Relation.PARTNER && !parentFam.isEmpty()) {
                U.askWhichParentsToShow(this, p, 2);
            } // A child with one or more families in which he is a partner
            else if (relation == Relation.CHILD && !p.getSpouseFamilies(gc).isEmpty()) {
                U.askWhichSpouceToShow(this, p, null);
            } // An unmarried child who has multiple parental families
            else if (parentFam.size() > 1) {
                if (parentFam.size() == 2) { // Swaps between the 2 parental families
                    Global.indi = p.getId();
                    Global.familyNum = parentFam.indexOf(f) == 0 ? 1 : 0;
                    Memory.replaceFirst(parentFam.get(Global.familyNum));
                    recreate();
                } else // More than two families
                    U.askWhichParentsToShow(this, p, 2);
            } // A spouse without parents but with multiple spouse families
            else if (spouseFam.size() > 1) {
                if (spouseFam.size() == 2) { // Swaps between the 2 spouse families
                    Global.indi = p.getId();
                    Family otherFamily = spouseFam.get(spouseFam.indexOf(f) == 0 ? 1 : 0);
                    Memory.replaceFirst(otherFamily);
                    recreate();
                } else
                    U.askWhichSpouceToShow(this, p, null);
            } else {
                Memory.setFirst(p);
                startActivity(new Intent(this, ProfileActivity.class));
            }
        });
        if (oneFamilyMember == null)
            oneFamilyMember = p;
    }

    /**
     * Finds the role of a person from their relation with a family.
     *
     * @param family        Can be null
     * @param respectFamily The role to find is relative to the family (it becomes 'parent' when there are children)
     * @return A descriptor text of the person's role
     */
    public static String getRole(Person person, Family family, Relation relation, boolean respectFamily) {
        int role = 0;
        if (respectFamily && relation == Relation.PARTNER && family != null && !family.getChildRefs().isEmpty())
            relation = Relation.PARENT;
        Status status = Status.getStatus(family);
        if (Gender.isMale(person)) {
            switch (relation) {
                case PARENT:
                    role = R.string.father;
                    break;
                case SIBLING:
                    role = R.string.brother;
                    break;
                case HALF_SIBLING:
                    role = R.string.half_brother;
                    break;
                case PARTNER:
                    switch (status) {
                        case MARRIED:
                            role = R.string.husband;
                            break;
                        case DIVORCED:
                            role = R.string.ex_husband;
                            break;
                        case SEPARATED:
                            role = R.string.ex_male_partner;
                            break;
                        default:
                            role = R.string.male_partner;
                    }
                    break;
                case CHILD:
                    role = R.string.son;
            }
        } else if (Gender.isFemale(person)) {
            switch (relation) {
                case PARENT:
                    role = R.string.mother;
                    break;
                case SIBLING:
                    role = R.string.sister;
                    break;
                case HALF_SIBLING:
                    role = R.string.half_sister;
                    break;
                case PARTNER:
                    switch (status) {
                        case MARRIED:
                            role = R.string.wife;
                            break;
                        case DIVORCED:
                        case SEPARATED:
                            role = R.string.ex_wife;
                            break;
                        default:
                            role = R.string.female_partner;
                    }
                    break;
                case CHILD:
                    role = R.string.daughter;
            }
        } else { // Neutral roles
            switch (relation) {
                case PARENT:
                    role = R.string.parent;
                    break;
                case SIBLING:
                    role = R.string.sibling;
                    break;
                case HALF_SIBLING:
                    role = R.string.half_sibling;
                    break;
                case PARTNER:
                    switch (status) {
                        case MARRIED:
                            role = R.string.spouse;
                            break;
                        case DIVORCED:
                            role = R.string.ex_spouse;
                            break;
                        case SEPARATED:
                            role = R.string.ex_partner;
                            break;
                        default:
                            role = R.string.partner;
                    }
                    break;
                case CHILD:
                    role = R.string.child;
            }
        }
        return Global.context.getString(role);
    }

    /**
     * Finds the ParentFamilyRef of a child person in a family.
     */
    public static ParentFamilyRef findParentFamilyRef(Person person, Family family) {
        for (ParentFamilyRef parentFamilyRef : person.getParentFamilyRefs()) {
            if (parentFamilyRef.getRef().equals(family.getId())) {
                return parentFamilyRef;
            }
        }
        return null;
    }

    /**
     * Composes the lineage definition to be added to the role in the person card.
     */
    public static String writeLineage(Person person, Family family) {
        ParentFamilyRef parentFamilyRef = findParentFamilyRef(person, family);
        if (parentFamilyRef != null) {
            int actual = Arrays.asList(lineageTypes).indexOf(parentFamilyRef.getRelationshipType());
            if (actual > 0)
                return " – " + lineageTexts[actual];
        }
        return "";
    }

    /**
     * Displays the alert dialog to choose the lineage of one person.
     */
    public static void chooseLineage(Context context, Person person, Family family) {
        ParentFamilyRef parentFamilyRef = findParentFamilyRef(person, family);
        if (parentFamilyRef != null) {
            int actual = Arrays.asList(lineageTypes).indexOf(parentFamilyRef.getRelationshipType());
            new AlertDialog.Builder(context).setSingleChoiceItems(lineageTexts, actual, (dialog, i) -> {
                parentFamilyRef.setRelationshipType(lineageTypes[i]);
                dialog.dismiss();
                if (context instanceof ProfileActivity)
                    ((ProfileActivity)context).refresh();
                else if (context instanceof FamilyActivity)
                    ((FamilyActivity)context).refresh();
                U.save(true, person);
            }).show();
        }
    }

    /**
     * Connects a person to a family as a parent or child.
     */
    public static void connect(Person person, Family family, int role) {
        switch (role) {
            case 5: // Parent // TODO: code smell: use of magic number
                // The person Ref inside the family
                SpouseRef sr = new SpouseRef();
                sr.setRef(person.getId());
                PersonEditorActivity.addSpouse(family, sr);
                // The family Ref inside the person
                SpouseFamilyRef sfr = new SpouseFamilyRef();
                sfr.setRef(family.getId());
                //person.getSpouseFamilyRefs().add(sfr); // Throws UnsupportedOperationException if the list is empty
                //List<SpouseFamilyRef> sfrList = person.getSpouseFamilyRefs(); // This doesn't work:
                // when the list doesn't exist, instead of returning an ArrayList it returns a Collections$EmptyList which is immutable: it does not allow add()
                List<SpouseFamilyRef> sfrList = new ArrayList<>(person.getSpouseFamilyRefs());
                sfrList.add(sfr);
                person.setSpouseFamilyRefs(sfrList);
                break;
            case 6: // Child
                ChildRef cr = new ChildRef();
                cr.setRef(person.getId());
                family.addChild(cr);
                ParentFamilyRef pfr = new ParentFamilyRef();
                pfr.setRef(family.getId());
                List<ParentFamilyRef> pfrList = new ArrayList<>(person.getParentFamilyRefs());
                pfrList.add(pfr);
                person.setParentFamilyRefs(pfrList);
        }
    }

    /**
     * Removes the single SpouseFamilyRef from the person and the corresponding SpouseRef from the family.
     */
    public static void disconnect(SpouseFamilyRef sfr, SpouseRef sr) {
        // Inside the person towards the family
        Person person = sr.getPerson(gc);
        person.getSpouseFamilyRefs().remove(sfr);
        if (person.getSpouseFamilyRefs().isEmpty())
            person.setSpouseFamilyRefs(null); // Any empty list is deleted
        person.getParentFamilyRefs().remove(sfr);
        if (person.getParentFamilyRefs().isEmpty())
            person.setParentFamilyRefs(null);
        // Inside the family towards the person
        Family fam = sfr.getFamily(gc);
        fam.getHusbandRefs().remove(sr);
        if (fam.getHusbandRefs().isEmpty())
            fam.setHusbandRefs(null);
        fam.getWifeRefs().remove(sr);
        if (fam.getWifeRefs().isEmpty())
            fam.setWifeRefs(null);
        fam.getChildRefs().remove(sr);
        if (fam.getChildRefs().isEmpty())
            fam.setChildRefs(null);
    }

    /**
     * Unlinks a person from a family.
     */
    public static void disconnect(String indiId, Family family) {
        // Removes the refs of the person inside the family
        Iterator<SpouseRef> spouseRefs = family.getHusbandRefs().iterator();
        while (spouseRefs.hasNext())
            if (spouseRefs.next().getRef().equals(indiId))
                spouseRefs.remove();
        if (family.getHusbandRefs().isEmpty())
            family.setHusbandRefs(null); // Deletes any empty list

        spouseRefs = family.getWifeRefs().iterator();
        while (spouseRefs.hasNext())
            if (spouseRefs.next().getRef().equals(indiId))
                spouseRefs.remove();
        if (family.getWifeRefs().isEmpty())
            family.setWifeRefs(null);

        Iterator<ChildRef> childRefs = family.getChildRefs().iterator();
        while (childRefs.hasNext())
            if (childRefs.next().getRef().equals(indiId))
                childRefs.remove();
        if (family.getChildRefs().isEmpty())
            family.setChildRefs(null);

        // Removes family refs inside the person
        Person person = gc.getPerson(indiId);
        Iterator<SpouseFamilyRef> iterSfr = person.getSpouseFamilyRefs().iterator();
        while (iterSfr.hasNext())
            if (iterSfr.next().getRef().equals(family.getId()))
                iterSfr.remove();
        if (person.getSpouseFamilyRefs().isEmpty())
            person.setSpouseFamilyRefs(null);

        Iterator<ParentFamilyRef> iterPfr = person.getParentFamilyRefs().iterator();
        while (iterPfr.hasNext())
            if (iterPfr.next().getRef().equals(family.getId()))
                iterPfr.remove();
        if (person.getParentFamilyRefs().isEmpty())
            person.setParentFamilyRefs(null);
    }
}
