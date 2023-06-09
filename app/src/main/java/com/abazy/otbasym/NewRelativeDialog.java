// DialogFragment which creates the dialogue to connect a relative in expert mode

package com.abazy.otbasym;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.Keep;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import org.folg.gedcom.model.Family;
import org.folg.gedcom.model.Person;

import java.util.ArrayList;
import java.util.List;

import com.abazy.otbasym.Сonstants.Choice;

public class NewRelativeDialog extends DialogFragment {

    private Person perno;
    private Family famPrefChild; // Family as a child to possibly show first in the spinner
    private Family famPrefSposo; // Family as a spouse to possibly show first in the spinner
    private boolean parenteNuovo;
    private Fragment frammento;
    private AlertDialog dialog;
    private Spinner spinner;
    private List<VoceFamiglia> voci = new ArrayList<>();
    private int relation;

    public NewRelativeDialog(Person perno, Family preferitaFiglio, Family preferitaSposo, boolean nuovo, Fragment frammento) {
        this.perno = perno;
        famPrefChild = preferitaFiglio;
        famPrefSposo = preferitaSposo;
        parenteNuovo = nuovo;
        this.frammento = frammento;
    }

    // Zero-argument constructor: nececessary to re-instantiate this fragment (e.g. rotating the device screen)
    @Keep // Request to don't remove when minify
    public NewRelativeDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        // Recreate dialog
        if (bundle != null) {
            perno = Global.gc.getPerson(bundle.getString("idPerno"));
            famPrefChild = Global.gc.getFamily(bundle.getString("idFamFiglio"));
            famPrefSposo = Global.gc.getFamily(bundle.getString("idFamSposo"));
            parenteNuovo = bundle.getBoolean("nuovo");
            frammento = getActivity().getSupportFragmentManager().getFragment(bundle, "frammento");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        //builder.setTitle( nuovo ? R.string.new_relative : R.string.link_person );
        View vista = requireActivity().getLayoutInflater().inflate(R.layout.new_person, null);
        // Spinner per scegliere la famiglia
        spinner = vista.findViewById(R.id.nuovoparente_famiglie);
        ArrayAdapter<VoceFamiglia> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        ((View)spinner.getParent()).setVisibility(View.GONE); // inizialmente lo spinner è nascosto

        RadioButton ruolo1 = vista.findViewById(R.id.nuovoparente_1);
        ruolo1.setOnCheckedChangeListener((r, selected) -> {
            if (selected) popolaSpinner(1);
        });
        RadioButton ruolo2 = vista.findViewById(R.id.nuovoparente_2);
        ruolo2.setOnCheckedChangeListener((r, selected) -> {
            if (selected) popolaSpinner(2);
        });
        RadioButton ruolo3 = vista.findViewById(R.id.nuovoparente_3);
        ruolo3.setOnCheckedChangeListener((r, selected) -> {
            if (selected) popolaSpinner(3);
        });
        RadioButton ruolo4 = vista.findViewById(R.id.nuovoparente_4);
        ruolo4.setOnCheckedChangeListener((r, selected) -> {
            if (selected) popolaSpinner(4);
        });

        builder.setView(vista).setPositiveButton(android.R.string.ok, (dialog, id) -> {
            // Setta alcuni valori che verranno passati a EditaIndividuo o ad PersonsFragment e arriveranno ad addParente()
            Intent intent = new Intent();
            intent.putExtra("idPerson", perno.getId());
            intent.putExtra("relation", relation);
            VoceFamiglia voceFamiglia = (VoceFamiglia)spinner.getSelectedItem();
            if (voceFamiglia.famiglia != null)
                intent.putExtra("idFamily", voceFamiglia.famiglia.getId());
            else if (voceFamiglia.genitore != null) // Uso 'collocazione' per veicolare l'id del genitore (il terzo attore della scena)
                intent.putExtra("collocazione", "NUOVA_FAMIGLIA_DI" + voceFamiglia.genitore.getId());
            else if (voceFamiglia.esistente) // veicola a PersonsFragment l'intenzione di congiungersi a famiglia esistente
                intent.putExtra("collocazione", "FAMIGLIA_ESISTENTE");
            if (parenteNuovo) { // Collega persona nuova
                intent.setClass(getContext(), PersonEditorActivity.class);
                startActivity(intent);
            } else { // Collega persona esistente
                intent.putExtra(Choice.PERSON, true);
                intent.setClass(getContext(), Principal.class);
                if (frammento != null)
                    frammento.startActivityForResult(intent, 1401);
                else
                    getActivity().startActivityForResult(intent, 1401);
            }
        }).setNeutralButton(R.string.cancel, null);
        dialog = builder.create();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); // Initially disabled
    }

    @Override
    public void onSaveInstanceState(Bundle bandolo) {
        bandolo.putString("idPerno", perno.getId());
        if (famPrefChild != null)
            bandolo.putString("idFamFiglio", famPrefChild.getId());
        if (famPrefSposo != null)
            bandolo.putString("idFamSposo", famPrefSposo.getId());
        bandolo.putBoolean("nuovo", parenteNuovo);
        //Save the fragment's instance
        if (frammento != null)
            getActivity().getSupportFragmentManager().putFragment(bandolo, "frammento", frammento);
    }

    // Dice se in una famiglia c'è spazio vuoto per aggiungere uno dei due genitori
    boolean carenzaConiugi(Family fam) {
        return fam.getHusbandRefs().size() + fam.getWifeRefs().size() < 2;
    }

    private void popolaSpinner(int relation) {
        this.relation = relation;
        voci.clear();
        int select = -1; // Indice della voce da selezionare nello spinner
        // Se rimane -1 seleziona la prima voce dello spinner
        switch (relation) {
            case 1: // Genitore
                for (Family fam : perno.getParentFamilies(Global.gc)) {
                    voci.add(new VoceFamiglia(getContext(), fam));
                    if ((fam.equals(famPrefChild)   // Seleziona la famiglia preferenziale in cui è figlio
                            || select < 0)           // oppure la prima disponibile
                            && carenzaConiugi(fam)) // se hanno spazio genitoriale vuoto
                        select = voci.size() - 1;
                }
                voci.add(new VoceFamiglia(getContext(), false));
                if (select < 0)
                    select = voci.size() - 1; // Seleziona "Nuova famiglia"
                break;
            case 2: // Fratello
                for (Family fam : perno.getParentFamilies(Global.gc)) {
                    voci.add(new VoceFamiglia(getContext(), fam));
                    for (Person padre : fam.getHusbands(Global.gc)) {
                        for (Family fam2 : padre.getSpouseFamilies(Global.gc))
                            if (!fam2.equals(fam))
                                voci.add(new VoceFamiglia(getContext(), fam2));
                        voci.add(new VoceFamiglia(getContext(), padre));
                    }
                    for (Person madre : fam.getWives(Global.gc)) {
                        for (Family fam2 : madre.getSpouseFamilies(Global.gc))
                            if (!fam2.equals(fam))
                                voci.add(new VoceFamiglia(getContext(), fam2));
                        voci.add(new VoceFamiglia(getContext(), madre));
                    }
                }
                voci.add(new VoceFamiglia(getContext(), false));
                // Seleziona la famiglia preferenziale come figlio
                select = 0;
                for (VoceFamiglia voce : voci)
                    if (voce.famiglia != null && voce.famiglia.equals(famPrefChild)) {
                        select = voci.indexOf(voce);
                        break;
                    }
                break;
            case 3: // Coniuge
            case 4: // Figlio
                for (Family fam : perno.getSpouseFamilies(Global.gc)) {
                    voci.add(new VoceFamiglia(getContext(), fam));
                    if ((voci.size() > 1 && fam.equals(famPrefSposo)) // Seleziona la famiglia preferita come coniuge (tranne la prima)
                            || (carenzaConiugi(fam) && select < 0)) // Seleziona la prima famiglia dove mancano coniugi
                        select = voci.size() - 1;
                }
                voci.add(new VoceFamiglia(getContext(), perno));
                if (select < 0)
                    select = voci.size() - 1; // Seleziona "Nuova famiglia di..."
                // Per un figlio seleziona la famiglia preferenziale (se esiste) altrimenti la prima
                if (relation == 4) {
                    select = 0;
                    for (VoceFamiglia voce : voci)
                        if (voce.famiglia != null && voce.famiglia.equals(famPrefSposo)) {
                            select = voci.indexOf(voce);
                            break;
                        }
                }
        }
        if (!parenteNuovo) {
            voci.add(new VoceFamiglia(getContext(), true));
        }
        ArrayAdapter<VoceFamiglia> adapter = (ArrayAdapter)spinner.getAdapter();
        adapter.clear();
        adapter.addAll(voci);
        ((View)spinner.getParent()).setVisibility(View.VISIBLE);
        spinner.setSelection(select);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    // Classe per le voci degli elenchi di famiglie nei dialoghi "A quale famiglia vuoi aggiungere...?"
    static class VoceFamiglia {
        Context contesto;
        Family famiglia;
        Person genitore;
        boolean esistente; // perno cercerà di inseririrsi in famiglia già esistente

        // Famiglia esistente
        VoceFamiglia(Context contesto, Family famiglia) {
            this.contesto = contesto;
            this.famiglia = famiglia;
        }

        // Nuova famiglia di un genitore
        VoceFamiglia(Context contesto, Person genitore) {
            this.contesto = contesto;
            this.genitore = genitore;
        }

        // Nuova famiglia vuota (false) OPPURE famiglia acquisita dal destinatario (true)
        VoceFamiglia(Context contesto, boolean esistente) {
            this.contesto = contesto;
            this.esistente = esistente;
        }

        @Override
        public String toString() {
            if (famiglia != null)
                return U.testoFamiglia(contesto, Global.gc, famiglia, true);
            else if (genitore != null)
                return contesto.getString(R.string.new_family_of, U.properName(genitore));
            else if (esistente)
                return contesto.getString(R.string.existing_family);
            else
                return contesto.getString(R.string.new_family);
        }
    }
}
