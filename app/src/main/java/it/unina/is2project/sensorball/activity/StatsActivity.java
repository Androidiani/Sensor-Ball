package it.unina.is2project.sensorball.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import it.unina.is2project.sensorball.R;
import it.unina.is2project.sensorball.stats.entity.StatOnePlayerRow;
import it.unina.is2project.sensorball.stats.entity.Player;
import it.unina.is2project.sensorball.stats.entity.StatTwoPlayer;
import it.unina.is2project.sensorball.stats.service.StatService;


public class StatsActivity extends Activity {
    //TODO: risolvere il problema del metodo close su StatService

    // TextView
    private TextView top;
    private TextView single;
    private TextView multi;
    private ListView listView;

    private TextView textViewColVinte;
    private TextView textViewColGiocate;
    private TextView textViewColWinningRate;

    private TextView textViewVinte;
    private TextView textViewGiocate;
    private TextView textViewWinningRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Set the fullscreen window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Find view
        findView();
        // Set-up typeface
        setFont();

        StatService statService = new StatService(getApplicationContext());

        ArrayAdapter<StatOnePlayerRow> adapter = new ArrayAdapter<StatOnePlayerRow>(this, R.layout.stat_one_player_row, statService.getStatOnePlayerList()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.stat_one_player_row, parent, false);
                }

                TextView pos = (TextView) convertView.findViewById(R.id.textViewPosition);
                TextView nome = (TextView) convertView.findViewById(R.id.textViewNome);
                TextView score = (TextView) convertView.findViewById(R.id.textViewScore);
                TextView data = (TextView) convertView.findViewById(R.id.textViewData);

                // Load the font
                Typeface typeFace = Typeface.createFromAsset(getContext().getAssets(), "font/secrcode.ttf");
                pos.setTypeface(typeFace);
                nome.setTypeface(typeFace);
                score.setTypeface(typeFace);
                data.setTypeface(typeFace);

                StatOnePlayerRow s = getItem(position);
                pos.setText("" + (position + 1));
                nome.setText("" + s.getPlayer().getNome());
                score.setText("" + s.getScore());
                data.setText("" + s.getData());

                return convertView;
            }
        };
        listView.setAdapter(adapter);

        Spinner spinner = (Spinner) findViewById(R.id.spinnerTwoPlayer);
        ArrayAdapter<Player> adapterSpinner = new ArrayAdapter<Player>(this, R.layout.spinner_stats_item, statService.getPlayers()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.spinner_stats_item, parent, false);
                }

                TextView name = (TextView) convertView.findViewById(R.id.spinner_stat_item);

                // Load the font
                Typeface typeFace = Typeface.createFromAsset(getContext().getAssets(), "font/secrcode.ttf");
                name.setTypeface(typeFace);

                Player p = getItem(position);
                name.setText(p.getNome());

                return convertView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.spinner_stats_item, parent, false);
                }

                TextView name = (TextView) convertView.findViewById(R.id.spinner_stat_item);

                // Load the font
                Typeface typeFace = Typeface.createFromAsset(getContext().getAssets(), "font/secrcode.ttf");
                name.setTypeface(typeFace);

                Player p = getItem(position);
                name.setText(p.getNome());

                return convertView;
            }
        };
        spinner.setAdapter(adapterSpinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            StatService statService;

            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
                Player selected = (Player) adapter.getItemAtPosition(pos);
                statService = new StatService(getApplicationContext());
                StatTwoPlayer s = statService.getStatTwoPlayer(selected.getId());
                if (s != null) {
                    textViewGiocate.setText("" + s.getPartiteGiocate());
                    textViewVinte.setText("" + s.getPartiteVinte());
                    textViewWinningRate.setText("" + (float) s.getPartiteVinte() / s.getPartiteGiocate() * 100 + "%");
                } else {
                    textViewGiocate.setText("0");
                    textViewVinte.setText("0");
                    textViewWinningRate.setText("0");
                }
                //statService.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //statService.close();
    }

    private void findView() {
        top = (TextView) findViewById(R.id.txt_stat);
        single = (TextView) findViewById(R.id.txt_single);
        multi = (TextView) findViewById(R.id.txt_multi);

        listView = (ListView) findViewById(R.id.listViewDemo);

        textViewColVinte = (TextView) findViewById(R.id.textViewColVinte);
        textViewColGiocate = (TextView) findViewById(R.id.textViewColGiocate);
        textViewColWinningRate = (TextView) findViewById(R.id.textViewColWinningRate);

        textViewVinte = (TextView) findViewById(R.id.textViewVinte);
        textViewGiocate = (TextView) findViewById(R.id.textViewGiocate);
        textViewWinningRate = (TextView) findViewById(R.id.textViewWinningRate);
    }

    private void setFont() {
        // Load the font
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "font/secrcode.ttf");

        top.setTypeface(typeFace);
        single.setTypeface(typeFace);
        multi.setTypeface(typeFace);

        textViewColVinte.setTypeface(typeFace);
        textViewColGiocate.setTypeface(typeFace);
        textViewColWinningRate.setTypeface(typeFace);

        textViewVinte.setTypeface(typeFace);
        textViewGiocate.setTypeface(typeFace);
        textViewWinningRate.setTypeface(typeFace);
    }
}
