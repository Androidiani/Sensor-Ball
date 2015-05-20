package it.unina.is2project.sensorgames.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import it.unina.is2project.sensorgames.R;


public class ScreenSlidePageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_screen_slide, container, false);
        ImageView imageView = (ImageView)rootView.findViewById(R.id.imageView);
        Bundle bundle = this.getArguments();
        switch (bundle.getInt("id")){
            case 1:
                rootView.setPadding(0, 0, 0, 60);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.help1));
                break;
            case 2:
                rootView.setPadding(0, 0, 0, 60);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.help2));
                break;
            case 3:
                rootView.setPadding(0, 0, 0, 60);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.help3));
                break;
            case 4:
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.help4));
                break;
            case 5:
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.help5));
                break;
            case 6:
                rootView.setPadding(0, 0, 0, 60);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.help6));
                break;
            default:
                break;
        }
        return rootView;
    }
}
