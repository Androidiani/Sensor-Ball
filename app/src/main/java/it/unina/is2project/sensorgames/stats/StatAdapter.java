package it.unina.is2project.sensorgames.stats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.unina.is2project.sensorgames.R;


public class StatAdapter extends ArrayAdapter<StatOnePlayerRow> {

    public StatAdapter(Context context, int textViewResourceId, List<StatOnePlayerRow> objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.stat_one_player_row, null);

        TextView data = (TextView) convertView.findViewById(R.id.textViewData);
        TextView nome = (TextView) convertView.findViewById(R.id.textViewNome);
        TextView score = (TextView) convertView.findViewById(R.id.textViewScore);
        TextView pos = (TextView) convertView.findViewById(R.id.textViewPosition);

        StatOnePlayerRow s = getItem(position);
        pos.setText("" + (position + 1));
        nome.setText("" + s.getPlayer().getNome());
        score.setText("" + s.getScore());
        data.setText("" + s.getData());

        return convertView;
    }

}
