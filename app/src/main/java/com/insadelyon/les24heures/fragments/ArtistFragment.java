package com.insadelyon.les24heures.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.insadelyon.les24heures.R;
import com.insadelyon.les24heures.adapter.NightResourceAdapter;
import com.insadelyon.les24heures.eventbus.ManageDetailSlidingUpDrawer;
import com.insadelyon.les24heures.eventbus.ResourcesUpdatedEvent;
import com.insadelyon.les24heures.eventbus.SearchEvent;
import com.insadelyon.les24heures.model.NightResource;
import com.insadelyon.les24heures.service.impl.ResourceServiceImpl;
import com.insadelyon.les24heures.utils.Day;
import com.insadelyon.les24heures.utils.SlidingUpPannelState;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by remi on 11/02/15.
 */
public class ArtistFragment extends ContentFrameFragment<NightResource> {
    private static final String TAG = DayMapsFragment.class.getCanonicalName();
    View view;

    @InjectView(R.id.artiste_fragment_grid_layout)
    GridView artistGridView;
    @InjectView(R.id.progress_wheel)
    View progressBar;
    Day day;

    private NightResourceAdapter nightResourceAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayName = getActivity().getResources().getString(R.string.artist_fragment_appname);

        if(getArguments() != null){
            day = (Day) getArguments().getSerializable("day");
        }
    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.artists_fragment, container, false);
        ButterKnife.inject(this, view);

        //create an ArrayAdaptar from the String Array
        nightResourceAdapter = new NightResourceAdapter(this.getActivity().getApplicationContext(),
                R.layout.artist_grid_item, new ArrayList<>(resourcesList),day); //no need of a pointer, ResourceAdapter takes care of its data via event and filter

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

    @Override
    public void onResume() {
        super.onResume();
        //setCategoryFilter();
    }

    /**
     * Fragment is alive      *
     */

    /**
     * Fragment is alive       *
     */


    public void onEvent(ResourcesUpdatedEvent event) {
        super.onEvent(event);
        resourcesList.clear();
        resourcesList.addAll(ResourceServiceImpl.getInstance().filterByDay((ArrayList<NightResource>) event.getNightResourceList(),day));
//        nightResourceAdapter.getFilter().filter("");
        //nightResourceAdapter.notifyDataSetChanged();

    }

    public void onEvent(SearchEvent event) {
        super.onEvent(event);
    }





    protected void displayProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }


}
