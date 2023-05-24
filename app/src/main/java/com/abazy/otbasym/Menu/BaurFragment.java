package com.abazy.otbasym.Menu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abazy.otbasym.Kinship;
import com.abazy.otbasym.KinshipAdapter;
import com.abazy.otbasym.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BaurFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class BaurFragment extends Fragment {

    // to_Do: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<Kinship> kinships = new ArrayList<Kinship>();
    // to_Do: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BaurFragment.
     */
    // to_Do: Rename and change types and number of parameters
    public static BaurFragment newInstance(String param1, String param2) {
        BaurFragment fragment = new BaurFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public BaurFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.kinship);

        return inflater.inflate(R.layout.fragment_baur, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setInitialData();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_kinship);
        KinshipAdapter adapter = new KinshipAdapter(getContext(), kinships);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    private void setInitialData() {
        kinships.add(new Kinship ("Ата", "баланың әкесінің және анасының әкесі."));
        kinships.add(new Kinship ("Әже ", "баланың әкесінің, сондай-ақ, анасының шешесі. Одан арғылары үлкен әже деп аталады.."));
        kinships.add(new Kinship ("Әке ", "баласы бар ер адам."));
        kinships.add(new Kinship ("Ана", "балалы әйел, туған шеше."));
        kinships.add(new Kinship ("Аға", "бірге туған ағайындылардың ер жағынан жасы үлкені."));
        kinships.add(new Kinship ("Іні", "бауырлас ер адамдардың жас жағынан кішісі."));
        kinships.add(new Kinship ("Бауыр", "бірге туған қыздардың үлкендеріне бауырлас ер адамдардың жасы жағынан кішісі."));
        kinships.add(new Kinship ("Әпке, апа", "бірге туған қыздардың жас жағынан үлкені."));
        kinships.add(new Kinship ("Сіңілі", "бірге туған қыздардың үлкендеріне жас жағынан кішісі."));
        kinships.add(new Kinship ("Қарындас ", "бірге туған ағайынды ер адамдардан жасы кіші қыздар."));
        kinships.add(new Kinship ("Немере ", "ата-ананың ұлынан туған бала."));
        kinships.add(new Kinship ("Шөбере ", "немереден туған бала."));
        kinships.add(new Kinship ("Шөпшек ", "шөбереден туған бала."));
        kinships.add(new Kinship ("Немене ", "шөпшектен туған бала."));
        kinships.add(new Kinship ("Туажат ", "неменеден туған бала."));
        kinships.add(new Kinship ("Жүрежат ", "алыс туысқандықты білдіретін атау, яғни туажаттан туған бала."));
        kinships.add(new Kinship ("Қайнаға ", "қайнаға ерлі-зайыптылардан жасы үлкен ер адам. Күйеуіне әйелінің ағалары, әйеліне күйеуінің ағалары қайнаға болады."));
        kinships.add(new Kinship ("Қайны ", "ерлі-зайыпты адамдардың өздерінен жасы кіші інісі. Күйеуіне әйелінің інілері, әйеліне күйеуінің інілері қайны болады."));
        kinships.add(new Kinship ("Балдыз ", "әйелінің сіңлілері мен інілерінің арадағы ер адамның туыстығын білдіретін ұғым. Әйеліне туыстық жақындығына қарай туған Балдыз, немере Балдыз, нағашы Балдыз, жиен Балдыз, деп бөлінеді."));
        kinships.add(new Kinship ("Жиен ", "баланың нағашы жұртымен туыстық қарым-қатынасын білдіретін ұғым, қыздан туған баланы қыздың төркіні және рулас адамдары, яғни нағашылары “жиен” деп атайды."));
        kinships.add(new Kinship ("Жеңге ", "ағаның әйелі. Бірге туған бауырлардың жасы үлкенінің әйелі іні-қарындастарына жеңге болады."));
        kinships.add(new Kinship ("Күйеу бала ", "сіңлінің күйеуі."));
        kinships.add(new Kinship ("Құда ", "күйеу мен қалыңдықтың аталары мен әкелері, аға-інілері мен туған туыстары."));
        kinships.add(new Kinship ("Құдағи ", "қарама-қарсы жақтың, әжелері, шешелері, апалары."));
        kinships.add(new Kinship ("Құдаша ", "қарама-қарсы жақтың сіңлілері."));
        kinships.add(new Kinship ("Бел құда ", "туылмаған (белдегі) балаға ниет қылысқан құдалар."));
        kinships.add(new Kinship ("Бесік құда", "құданың бұл түрі ел ішінде \"қызылдай құда\", \"қарын құда\", \"құрсақ құда\" деп те аталады."));
        kinships.add(new Kinship ("Қарсы құда", "ұлы мен қызын бесіктегі кезінде атастырған құдалар, кейбір жерде \"бесік кертпе құда\", \"бесік кертті құда\" делінеді."));
        kinships.add(new Kinship ("Бас құда ", "бір-бірімен қыз алысып, қыз беріскен қат-қабат құдалар."));
        kinships.add(new Kinship ("Жанама құда ", "екі жақты сөзін сөйлер құдалар. Бас құданың жанына еріп келген жора-жолдастары."));
        kinships.add(new Kinship ("Бауыздау құда ", "мал бауыздағанда бата берген құда."));
        kinships.add(new Kinship ("Бөле", "әке немесе шеше жақтан қарындас, сіңлілерінің балалары."));
        kinships.add(new Kinship ("Жиеншар", "жиеннен туған бала."));
        kinships.add(new Kinship ("Қайын ене", "неке-отбасылық қатынаста әйелдің күйеуінің анасына және күйеудің әйелінің анасына қатысты айтылатын туыстық атау."));
        kinships.add(new Kinship ("Жезде", "туысқан адамдардың өзінен үлкен апасының күйеуі."));
        kinships.add(new Kinship ("Келін ", "ата-ана үшін баласының әйелі де, жасы үлкен ағалары мен туысқандары үшін інісінің әйелі."));
        kinships.add(new Kinship ("Қайын ата", "күйеу мен келіннің туған әкелерінің оларға туыстық қатынасы."));
        kinships.add(new Kinship ("Қайын бике", "күйеу мен келіннің туған әпкелерінің оларға туыстық қатынасы."));
        kinships.add(new Kinship ("Абысын", "ағайынды адамдардың әйелдері."));
        kinships.add(new Kinship ("Бажа ", "бір адамның немесе ағайынды кісілердің қыздарына үйленген адамдар."));

    }
}