package com.example.denal.husky;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by denal on 12/1/2017.
 */

public class SelectionFrag extends android.support.v4.app.Fragment implements View.OnClickListener{

    public ImageButton reddit, spotify, youtube, custom;

    public SelectionFrag() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_selection, container, false);
        reddit = (ImageButton) v.findViewById(R.id.reddit);
        spotify = (ImageButton) v.findViewById(R.id.spotify);
        youtube = (ImageButton) v.findViewById(R.id.youtube);
        custom = (ImageButton) v.findViewById(R.id.custom);
        reddit.setOnClickListener(this);
        spotify.setOnClickListener(this);
        youtube.setOnClickListener(this);
        custom.setOnClickListener(this);
        return v;
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.reddit) {
            String url = "https://www.reddit.com/r/calmdown/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
        else if (view.getId() == R.id.spotify) {
            final Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("spotify:album:4Hv8Vy1ZYqyxiku73K6skq"));
            startActivity(intent);
        }
        else if (view.getId() == R.id.youtube) {
            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com/watch?v=P3gIi9su2Rw")));
        }
        else if (view.getId() == R.id.custom) {
            MainActivity main = (MainActivity)getActivity();
            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.hide(fragmentManager.findFragmentByTag("select")).commit();
            main.link.setVisibility(View.VISIBLE);
            main.play.setVisibility(View.VISIBLE);
            main.next.setVisibility(View.VISIBLE);
            main.prev.setVisibility(View.VISIBLE);
        }
    }
}
