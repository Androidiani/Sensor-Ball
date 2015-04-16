package it.unina.is2project.sensorgames.stats;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.unina.is2project.sensorgames.R;
import it.unina.is2project.sensorgames.stats.activity.StatOnePlayerRow;

public class StatAdapter extends ArrayAdapter<StatOnePlayerRow> {

    public StatAdapter(Context context, int textViewResourceId, List<StatOnePlayerRow> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.stat_one_player_row, null);

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
}
