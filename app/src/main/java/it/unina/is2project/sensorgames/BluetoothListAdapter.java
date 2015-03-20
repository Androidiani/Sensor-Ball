package it.unina.is2project.sensorgames;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BluetoothListAdapter extends ArrayAdapter{

    private Context mContext;
    private int id;
    private List <String>items;

    public BluetoothListAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        mContext = context;
        id = resource;
        items = objects ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View mView = convertView ;
        if(mView == null){
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = vi.inflate(id, null);
        }

        TextView text = (TextView) mView.findViewById(R.id.textListElement);

        if(items.get(position) != null )
        {
            text.setTextColor(Color.WHITE);
            text.setText(items.get(position));
            text.setBackgroundColor(Color.GRAY);
            int color = Color.argb( 200, 255, 64, 64 );
            text.setBackgroundColor( color );

        }

        return mView;
    }
}
