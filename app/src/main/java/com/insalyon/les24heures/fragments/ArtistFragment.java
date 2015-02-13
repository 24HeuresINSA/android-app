package com.insalyon.les24heures.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.insalyon.les24heures.R;
import com.insalyon.les24heures.adapter.NightResourceAdapter;
import com.insalyon.les24heures.eventbus.CategoriesSelectedEvent;
import com.insalyon.les24heures.eventbus.ManageDetailSlidingUpDrawer;
import com.insalyon.les24heures.eventbus.ResourcesUpdatedEvent;
import com.insalyon.les24heures.eventbus.SearchEvent;
import com.insalyon.les24heures.model.NightResource;
import com.insalyon.les24heures.utils.SlidingUpPannelState;
import com.insalyon.les24heures.view.AutoExpandGridView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by remi on 11/02/15.
 */
public class ArtistFragment extends ContentFrameFragment<NightResource>  {
    private static final String TAG = OutputMapsFragment.class.getCanonicalName();
    View view;

    @InjectView(R.id.artiste_fragment_grid_layout)
    AutoExpandGridView artistGridView;
    private NightResourceAdapter nightResourceAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayName = getActivity().getResources().getString(R.string.artist_fragment_name);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        view = inflater.inflate(R.layout.artists_fragment,container,false);
        ButterKnife.inject(this, view);

        //create an ArrayAdaptar from the String Array
        nightResourceAdapter = new NightResourceAdapter(this.getActivity().getApplicationContext(),
                R.layout.artist_grid_item, new ArrayList<>(resourcesList)); //no need of a pointer, ResourceAdapter takes care of its data via event and filter

        //get filters than are managed by ContentFrameFragment
        searchFilter = nightResourceAdapter.getFilter();
        categoryFilter = nightResourceAdapter.getCategoryFilter();

        artistGridView.setAdapter(nightResourceAdapter);
        artistGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NightResource resource = (NightResource) parent.getItemAtPosition(position);

                ManageDetailSlidingUpDrawer manageDetailSlidingUpDrawer = new ManageDetailSlidingUpDrawer(SlidingUpPannelState.ANCHORED,
                        resource);
                eventBus.post(manageDetailSlidingUpDrawer);
            }
        });


        return view;
    }

    /**
     * Fragment is alive      *
     */

    /**
     * Fragment is alive       *
     */
    public void onEvent(CategoriesSelectedEvent event) {
        super.onEvent(event);
    }

    public void onEvent(ResourcesUpdatedEvent event) {
        super.onEvent(event);
        Log.d("onEvent(ResourcesUpdatedEvent)", event.getNightResourceList().toString());
        resourcesList.clear();
        resourcesList.addAll(event.getNightResourceList());
    }

    public void onEvent(SearchEvent event) {
        super.onEvent(event);
        Log.d("onEvent(SearchEvent)", event.getQuery().toString());
    }






}