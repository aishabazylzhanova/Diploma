package com.abazy.otbasym;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.abazy.otbasym.Сonstants.Format;
import com.abazy.otbasym.Сonstants.Kind;

public class DateEditorLayout extends LinearLayout {

    GedcomDateConverter gedcomDateConverter;
    GedcomDateConverter.Data data1;
    GedcomDateConverter.Data data2;
    EditText editaTesto;
    String[] giorniRuota = {"-", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
            "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    String[] mesiRuota = {"-", s(R.string.january), s(R.string.february), s(R.string.march), s(R.string.april), s(R.string.may), s(R.string.june),
            s(R.string.july), s(R.string.august), s(R.string.september), s(R.string.october), s(R.string.november), s(R.string.december)};
    String[] anniRuota = new String[101];
    int[] dateKinds = {R.string.exact, R.string.approximate, R.string.calculated, R.string.estimated,
            R.string.after, R.string.before, R.string.between_and,
            R.string.from, R.string.to, R.string.from_to, R.string.date_phrase};
    Calendar calenda = GregorianCalendar.getInstance();
    boolean veroImputTesto; // stabilisce se l'utente sta effettivamente digitando sulla tastiera virtuale o se il testo viene cambiato in altro modo
    InputMethodManager tastiera;
    boolean tastieraVisibile;

    public DateEditorLayout(Context contesto, AttributeSet as) {
        super(contesto, as);
    }

    /**
     * Actions to be done only once at the beginning.
     */
    void initialize(final EditText editaTesto) {

        addView(inflate(getContext(), R.layout.edit_data, null), this.getLayoutParams());
        this.editaTesto = editaTesto;

        for (int i = 0; i < anniRuota.length - 1; i++)
            anniRuota[i] = i < 10 ? "0" + i : "" + i;
        anniRuota[100] = "-";

        gedcomDateConverter = new GedcomDateConverter(editaTesto.getText().toString());
        data1 = gedcomDateConverter.data1;
        data2 = gedcomDateConverter.data2;

        // Arreda l'editore data
        if (Global.settings.expert) {
            final TextView elencoTipi = findViewById(R.id.editadata_tipi);
            elencoTipi.setOnClickListener(vista -> {
                PopupMenu popup = new PopupMenu(getContext(), vista);
                Menu menu = popup.getMenu();
                for (int i = 0; i < dateKinds.length - 1; i++)
                    menu.add(0, i, 0, dateKinds[i]);
                popup.show();
                popup.setOnMenuItemClickListener(item -> {
                    gedcomDateConverter.kind = Kind.values()[item.getItemId()];
                    // Se eventualmente invisibile
                    findViewById(R.id.editadata_prima).setVisibility(View.VISIBLE);
                    if (data1.date == null) // micro settaggio del carro
                        ((NumberPicker)findViewById(R.id.prima_anno)).setValue(100);
                    if (gedcomDateConverter.kind == Kind.BETWEEN_AND || gedcomDateConverter.kind == Kind.FROM_TO) {
                        findViewById(R.id.editadata_seconda_avanzate).setVisibility(VISIBLE);
                        findViewById(R.id.editadata_seconda).setVisibility(VISIBLE);
                        if (data2.date == null)
                            ((NumberPicker)findViewById(R.id.seconda_anno)).setValue(100);
                    } else {
                        findViewById(R.id.editadata_seconda_avanzate).setVisibility(GONE);
                        findViewById(R.id.editadata_seconda).setVisibility(GONE);
                    }
                    elencoTipi.setText(dateKinds[item.getItemId()]);
                    veroImputTesto = false;
                    generate();
                    return true;
                });
            });


            findViewById(R.id.editadata_negativa2).setOnClickListener(vista -> {
                data2.negative = ((CompoundButton)vista).isChecked();
                veroImputTesto = false;
                generate();
            });
            findViewById(R.id.editadata_doppia2).setOnClickListener(vista -> {
                data2.doubleDate = ((CompoundButton)vista).isChecked();
                veroImputTesto = false;
                generate();
            });

        } else {

            findViewById(R.id.editadata_avanzate).setVisibility(GONE);
        }

        arrange(1, findViewById(R.id.prima_giorno), findViewById(R.id.prima_mese),
                findViewById(R.id.prima_secolo), findViewById(R.id.prima_anno));

        arrange(2, findViewById(R.id.seconda_giorno), findViewById(R.id.seconda_mese),
                findViewById(R.id.seconda_secolo), findViewById(R.id.seconda_anno));

        // At first focus it shows itself (EditoreData) hiding the keyboard
        tastiera = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        editaTesto.setOnFocusChangeListener((v, ciapaFocus) -> {
            if (ciapaFocus) {
                if (gedcomDateConverter.kind == Kind.PHRASE) {
                    //genera(); // Toglie le parentesi alla frase
                    editaTesto.setText(gedcomDateConverter.phrase);
                } else {
                    tastieraVisibile = tastiera.hideSoftInputFromWindow(editaTesto.getWindowToken(), 0); // ok nasconde tastiera
					/*Window finestra = ((Activity)getContext()).getWindow(); non aiuta la scomparsa della tastiera
					finestra.setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );*/
                    editaTesto.setInputType(InputType.TYPE_NULL); // disabilita input testo con tastiera
                    // necessario in versioni recenti di android in cui la tastiera ricompare
                }
                gedcomDateConverter.data1.date = null; // un resettino
                setAll();
                setVisibility(View.VISIBLE);
            } else
                setVisibility(View.GONE);
        });

        // The second touch brings up the keyboard
        editaTesto.setOnTouchListener((vista, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                editaTesto.setInputType(InputType.TYPE_CLASS_TEXT); // riabilita l'input
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                tastieraVisibile = tastiera.showSoftInput(editaTesto, 0); // fa ricomparire la tastiera
                //veroImputTesto = true;
                //vista.performClick(); non ne vedo l'utilità
            }
            return false;
        });
        // Set the date publisher based on what is written
        editaTesto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence testo, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence testo, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable testo) {
                if (veroImputTesto)
                    setAll();
                veroImputTesto = true;
            }
        });
    }

     void arrange(final int which, final NumberPicker numberPicker, final NumberPicker numberPicker1, final NumberPicker numberPicker2, final NumberPicker numberPicker3) {
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(31);
        numberPicker.setDisplayedValues(giorniRuota);
        prepareWheel(numberPicker);
        numberPicker.setOnValueChangedListener((picker, vecchio, nuovo) ->
                update(which == 1 ? data1 : data2, numberPicker, numberPicker1, numberPicker2, numberPicker3)
        );
        numberPicker1.setMinValue(0);
        numberPicker1.setMaxValue(12);
        numberPicker1.setDisplayedValues(mesiRuota);
        prepareWheel(numberPicker1);
        numberPicker1.setOnValueChangedListener((picker, vecchio, nuovo) ->
                update(which == 1 ? data1 : data2, numberPicker, numberPicker1, numberPicker2, numberPicker3)
        );
        numberPicker2.setMinValue(0);
        numberPicker2.setMaxValue(20);
        prepareWheel(numberPicker2);
        numberPicker2.setOnValueChangedListener((picker, vecchio, nuovo) ->
                update(which == 1 ? data1 : data2, numberPicker, numberPicker1, numberPicker2, numberPicker3)
        );
        numberPicker3.setMinValue(0);
        numberPicker3.setMaxValue(100);
        numberPicker3.setDisplayedValues(anniRuota);
        prepareWheel(numberPicker3);
        numberPicker3.setOnValueChangedListener((picker, vecchio, nuovo) ->
                update(which == 1 ? data1 : data2, numberPicker, numberPicker1, numberPicker2, numberPicker3)
        );
    }

    void prepareWheel(NumberPicker wheel) {
        // Removes the dividing blue lines on API <= 22
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                Field field = NumberPicker.class.getDeclaredField("mSelectionDivider");
                field.setAccessible(true);
                field.set(wheel, null);
            } catch (Exception e) {
            }
        }
        // Fixes the bug https://issuetracker.google.com/issues/37055335
        wheel.setSaveFromParentEnabled(false);
    }


    void setAll() {
        gedcomDateConverter.analyze(editaTesto.getText().toString());

        ((TextView)findViewById(R.id.editadata_tipi)).setText(dateKinds[gedcomDateConverter.kind.ordinal()]);


        setTank(data1, findViewById(R.id.prima_giorno), findViewById(R.id.prima_mese),
                findViewById(R.id.prima_secolo), findViewById(R.id.prima_anno));
        if (Global.settings.expert)
            setVis(data1);


        if (gedcomDateConverter.kind == Kind.BETWEEN_AND || gedcomDateConverter.kind == Kind.FROM_TO) {
            setTank(data2, findViewById(R.id.seconda_giorno), findViewById(R.id.seconda_mese),
                    findViewById(R.id.seconda_secolo), findViewById(R.id.seconda_anno));
            if (Global.settings.expert) {
                findViewById(R.id.editadata_seconda_avanzate).setVisibility(VISIBLE);
                setVis(data2);
            }
            findViewById(R.id.editadata_seconda).setVisibility(VISIBLE);
        } else {
            findViewById(R.id.editadata_seconda_avanzate).setVisibility(GONE);
            findViewById(R.id.editadata_seconda).setVisibility(GONE);
        }
    }


    void setTank(GedcomDateConverter.Data data, NumberPicker numberPicker, NumberPicker numberPicker1, NumberPicker numberPicker2, NumberPicker numberPicker3) {
        calenda.clear();
        if (data.date != null)
            calenda.setTime(data.date);
        numberPicker.setMaxValue(calenda.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (data.date != null && (data.isFormat(Format.D_M_Y) || data.isFormat(Format.D_M)))
            numberPicker.setValue(data.date.getDate());
        else
            numberPicker.setValue(0);
        if (data.date == null || data.isFormat(Format.Y))
            numberPicker1.setValue(0);
        else
            numberPicker1.setValue(data.date.getMonth() + 1);
        if (data.date == null || data.isFormat(Format.D_M))
            numberPicker2.setValue(0);
        else
            numberPicker2.setValue((data.date.getYear() + 1900) / 100);
        if (data.date == null || data.isFormat(Format.D_M))
            numberPicker3.setValue(100);
        else
            numberPicker3.setValue((data.date.getYear() + 1900) % 100);
    }


    void setVis(GedcomDateConverter.Data data) {
        CheckBox ceccoBC, ceccoDoppia;
        if (data.equals(data1)) {
            ceccoBC = findViewById(R.id.editadata_negativa2);
            ceccoDoppia = findViewById(R.id.editadata_doppia2);
        } else {
            ceccoBC = findViewById(R.id.editadata_negativa2);
            ceccoDoppia = findViewById(R.id.editadata_doppia2);
        }
        if (data.date == null || data.isFormat(Format.EMPTY) || data.isFormat(Format.D_M)) { // date senza anno
            ceccoBC.setVisibility(INVISIBLE);
            ceccoDoppia.setVisibility(INVISIBLE);
        } else {
            ceccoBC.setChecked(data.negative);
            ceccoBC.setVisibility(VISIBLE);
            ceccoDoppia.setChecked(data.doubleDate);
            ceccoDoppia.setVisibility(VISIBLE);
        }
    }

    // Update a Date with the new values ​​taken from the wheels
    void update(GedcomDateConverter.Data data, NumberPicker ruotaGiorno, NumberPicker ruotaMese, NumberPicker ruotaSecolo, NumberPicker ruotaAnno) {
        if (tastieraVisibile) {    // Nasconde eventuale tastiera visibile
            tastieraVisibile = tastiera.hideSoftInputFromWindow(editaTesto.getWindowToken(), 0);
            // Nasconde subito la tastiera, ma ha bisogno di un secondo tentativo per restituire false. Comunque non è un problema
        }
        int giorno = ruotaGiorno.getValue();
        int mese = ruotaMese.getValue();
        int secolo = ruotaSecolo.getValue();
        int anno = ruotaAnno.getValue();
        // Set the days of the month in wheelDay
        calenda.set(secolo * 100 + anno, mese - 1, 1);
        ruotaGiorno.setMaxValue(calenda.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (data.date == null) data.date = new Date();
        data.date.setDate(giorno == 0 ? 1 : giorno);  // altrimenti la data M_A arretra di un mese
        data.date.setMonth(mese == 0 ? 0 : mese - 1);
        data.date.setYear(anno == 100 ? -1899 : secolo * 100 + anno - 1900);
        if (giorno != 0 && mese != 0 && anno != 100)
            data.format.applyPattern(Format.D_M_Y);
        else if (giorno != 0 && mese != 0)
            data.format.applyPattern(Format.D_M);
        else if (mese != 0 && anno != 100)
            data.format.applyPattern(Format.M_Y);
        else if (anno != 100)
            data.format.applyPattern(Format.Y);
        else
            data.format.applyPattern(Format.EMPTY);
        setVis(data);
        veroImputTesto = false;
        generate();
    }

    // Rebuilds the string with the end date and puts it in Textedit
    void generate() {
        String redone;
        if (gedcomDateConverter.kind == Kind.EXACT)
            redone = redo(data1);
        else if (gedcomDateConverter.kind == Kind.BETWEEN_AND)
            redone = "BET " + redo(data1) + " AND " + redo(data2);
        else if (gedcomDateConverter.kind == Kind.FROM_TO)
            redone = "FROM " + redo(data1) + " TO " + redo(data2);
        else if (gedcomDateConverter.kind == Kind.PHRASE) {
            // La frase viene sostituita da data esatta
            gedcomDateConverter.kind = Kind.EXACT;
            ((TextView)findViewById(R.id.editadata_tipi)).setText(dateKinds[0]);
            redone = redo(data1);
        } else
            redone = gedcomDateConverter.kind.prefix + " " + redo(data1);
        editaTesto.setText(redone);
    }

    // Writes the single date according to the format
    String redo(GedcomDateConverter.Data data) {
        String done = "";
        if (data.date != null) {
            // Date con l'anno doppio
            if (data.doubleDate && !(data.isFormat(Format.EMPTY) || data.isFormat(Format.D_M))) {
                Date unAnnoDopo = new Date();
                unAnnoDopo.setYear(data.date.getYear() + 1);
                String secondoAnno = String.format(Locale.ENGLISH, "%tY", unAnnoDopo);
                done = data.format.format(data.date) + "/" + secondoAnno.substring(2);
            } else // Le altre date normali
                done = data.format.format(data.date);
        }
        if (data.negative)
            done += " B.C.";
        return done;
    }

    /**
     * If the date is a phrase adds parentheses around it.
     */
    public void finishEditing() {
        if (gedcomDateConverter.kind == Kind.PHRASE) {
            String s = "(" + editaTesto.getText() + ")";
            editaTesto.setText(s);
        }
    }

    String s(int id) {
        return Global.context.getString(id);
    }
}
