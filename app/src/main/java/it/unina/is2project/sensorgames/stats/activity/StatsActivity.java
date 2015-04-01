package it.unina.is2project.sensorgames.stats.activity;

import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import it.unina.is2project.sensorgames.R;
import it.unina.is2project.sensorgames.stats.StatAdapter;
import it.unina.is2project.sensorgames.stats.entity.Player;
import it.unina.is2project.sensorgames.stats.entity.StatTwoPlayer;
import it.unina.is2project.sensorgames.stats.service.StatService;


public class StatsActivity extends ActionBarActivity {

    // Font typeface
    private Typeface typeFace;

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
        setContentView(R.layout.activity_stats2);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /** Set the fullscreen window */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        /** Load the font */
        typeFace = Typeface.createFromAsset(getAssets(), "font/secrcode.ttf");

        // Find View by ID
        findView();

        // Set Font Typeface
        setFont();


        StatService statService = new StatService(getApplicationContext());

        StatAdapter adapter = new StatAdapter(this, R.layout.stat_one_player_row, statService.getStatOnePlayerList());
        listView.setAdapter(adapter);

        Spinner spinner = (Spinner) findViewById(R.id.spinnerTwoPlayer);
        ArrayAdapter<Player> adapterSpinner = new ArrayAdapter<Player>(this, android.R.layout.simple_spinner_item, statService.getPlayers());
        spinner.setAdapter(adapterSpinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            StatService statService;

            @Override
            public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
                Player selected = (Player) adapter.getItemAtPosition(pos);
                //Toast.makeText( getApplicationContext(), "hai selezionato " + selected.getId(), Toast.LENGTH_LONG).show();
                statService = new StatService(getApplicationContext());
                StatTwoPlayer s = statService.getStatTwoPlayer(selected.getId());
                if (s != null) {
                    textViewGiocate.setText("" + s.getPartiteGiocate());
                    textViewVinte.setText("" + s.getPartiteVinte());
                    textViewWinningRate.setText("" + (float) s.getPartiteGiocate() / s.getPartiteVinte() * 100 + "%");
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
    }

    public void findView() {
        top = (TextView) findViewById(R.id.txt_stat);
        single = (TextView) findViewById(R.id.txt_single);
        multi = (TextView) findViewById(R.id.txt_multi);

//        textViewColVinte = (TextView) findViewById(R.id.textViewColVinte);
//        textViewColGiocate = (TextView) findViewById(R.id.textViewColGiocate);
//        textViewColWinningRate = (TextView) findViewById(R.id.textViewColWinningRate);

        listView = (ListView) findViewById(R.id.listViewDemo);
        textViewVinte = (TextView) findViewById(R.id.textViewVinte);
        textViewGiocate = (TextView) findViewById(R.id.textViewGiocate);
        textViewWinningRate = (TextView) findViewById(R.id.textViewWinningRate);
    }

    public void setFont() {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
