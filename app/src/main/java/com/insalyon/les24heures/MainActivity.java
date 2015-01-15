package com.insalyon.les24heures;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.insalyon.les24heures.eventbus.CategoriesSelectedEvent;
import com.insalyon.les24heures.eventbus.CategoriesUpdatedEvent;
import com.insalyon.les24heures.eventbus.ResourcesUpdatedEvent;
import com.insalyon.les24heures.fragments.OutputListFragment;
import com.insalyon.les24heures.fragments.OutputMapsFragment;
import com.insalyon.les24heures.model.Category;
import com.insalyon.les24heures.model.Resource;
import com.insalyon.les24heures.service.CategoryService;
import com.insalyon.les24heures.service.ResourceRetrofitService;
import com.insalyon.les24heures.service.ResourceService;
import com.insalyon.les24heures.service.impl.CategoryServiceImpl;
import com.insalyon.les24heures.service.impl.ResourceServiceImpl;
import com.insalyon.les24heures.utils.FilterAction;
import com.insalyon.les24heures.utils.OutputType;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();

    FragmentManager fragmentManager;
    EventBus eventBus;
    RestAdapter restAdapter;
    ResourceRetrofitService resourceRetrofitService;

    private ActionBarDrawerToggle actionBarDrawerToggle;

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.left_drawer)
    View drawerView;
    @InjectView(R.id.left_drawer_categories_list)
    ListView categoriesList;
//    @InjectView(R.id.outputtype_maps)
//    View outputTypeMaps;
//    @InjectView(R.id.outputtype_list)
//    View outputTypeList;



    private String[] navigationDrawerCategories; //viendra du backend, a supprimer du manifest
    private ArrayList<Category> categories;
    private ArrayList<Category> categoriesSelected;
    private ArrayList<Resource> resourcesList;

    //depency injection ?
    private ResourceService resourceService;
    private CategoryService categoryService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*** init services ***/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this, this);
        eventBus = EventBus.getDefault();
        restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://cetaitmieuxavant.24heures.org")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        resourceRetrofitService = restAdapter.create(ResourceRetrofitService.class);
        fragmentManager = getFragmentManager();
        //dependency injection instead ?
        resourceService = ResourceServiceImpl.getInstance();
        categoryService = CategoryServiceImpl.getInstance();


        /*** recover data either from (by priority)
         *           savedInstanceState (rotate, restore from background)
         *           getIntent (start from another activity, another apps) TODO
         *           sharedPreferences (start,rotate, restore from bg) TODO
         *           localStorage (start) TODO
         *           backend (if needed TODO)
         */
        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelableArrayList("categories") != null) {
                categories = savedInstanceState.getParcelableArrayList("categories");
            }
            if (savedInstanceState.getParcelableArrayList("categoriesSelected") != null) {
                categoriesSelected = savedInstanceState.getParcelableArrayList("categoriesSelected");
            }
            if (savedInstanceState.getParcelableArrayList("resourcesList") != null) {
                resourcesList = savedInstanceState.getParcelableArrayList("resourcesList");
            }
        }

        navigationDrawerCategories = getResources().getStringArray(R.array.navigation_drawer_categories); //TODO que veut en parametre ArrayAdapter ?
        if (categories == null) {
            //viendra du backend
            categories = new ArrayList<>();
            for (String navigationDrawerCategory : navigationDrawerCategories) {
                categories.add(new Category(navigationDrawerCategory));
            }
            eventBus.post(new CategoriesUpdatedEvent(categories));
            //TODO comprendre pourquoi est ce que je dois faire ca, meme si au final la réponse ne servira pas pour ce cas precis
            categoryService.setCategories(categories);
        }

        if (resourcesList == null) {
            resourcesList = new ArrayList<>();
           resourceService.getResourcesAsyncFromBackend(resourceRetrofitService);
//            resourceService.getResourcesAsyncMock();
        }

        if(categoriesSelected == null){
            categoriesSelected = new ArrayList<>();
        }


        /*** setup the navigation drawer ***/
        setSupportActionBar(toolbar);

        // set a custom shadow that overlays the main content when the drawer opens
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // set up the drawer's list view with items and click listener
        categoriesList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item_category, navigationDrawerCategories));
        categoriesList.setOnItemClickListener(new DrawerItemClickListener());

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        actionBarDrawerToggle = new ActionBarDrawerToogle24Heures(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
//        actionBarDrawerToggle.onDrawerOpened();
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        /*** start the right ouptut : Maps or List ***/
        if (savedInstanceState == null) {
            //default start : get from manifest
            try {
                ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = ai.metaData;
                String defaultOutput = bundle.getString("outputType");
                if (defaultOutput.toLowerCase().equals(OutputType.MAPS.toString().toLowerCase())) {
                    selectMaps();
                } else if (defaultOutput.toLowerCase().equals(OutputType.LIST.toString().toLowerCase())) {
                    selectList();
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
                selectMaps();
            } catch (NullPointerException e) {
                Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
                selectMaps();
            }

        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }


    /**         Activity is alive       **/

    public void onEvent(ResourcesUpdatedEvent event) {
        // super.onEvent(event);
        Log.d(TAG + "onEvent(ResourcesUpdatedEvent)", event.getResourceList().toString());
        resourcesList.clear();
        resourcesList.addAll(event.getResourceList());
    }

//    @OnClick(R.id.outputtype_maps)
    public void selectMaps() {
//        if (outputTypeMaps.isSelected()) return;

//        outputTypeMaps.setSelected(true);
//        outputTypeList.setSelected(false);
//        drawerLayout.closeDrawer(drawerView);

        Fragment mapsFragment = new OutputMapsFragment();
        replaceContentFragment(mapsFragment);
    }

//    @OnClick(R.id.outputtype_list)
    public void selectList() {
//        if (outputTypeList.isSelected()) return;

//        outputTypeList.setSelected(true);
//        outputTypeMaps.setSelected(false);
//        drawerLayout.closeDrawer(drawerView);

        Fragment listFragment = new OutputListFragment();
        replaceContentFragment(listFragment);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**         Activity is no more alive       **/

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Output state
//        if (outputTypeMaps.isSelected()) {
        if (fragmentManager.findFragmentById(R.id.content_frame).getClass() == OutputMapsFragment.class) {
            outState.putString("outputType", OutputType.MAPS.toString());
        } else {
            outState.putString("outputType", OutputType.LIST.toString());
        }

        //categories
        outState.putParcelableArrayList("categories", categories);
        //categories state
        ArrayList<Category> categoriesSelected = getCategoriesSelectedFromView();
        outState.putParcelableArrayList("categoriesSelected", categoriesSelected);
        //resources
        outState.putParcelableArrayList("resourcesList", resourcesList);
    }


    /**         Activity methods      **/

    private void replaceContentFragment(Fragment fragment) {
        Bundle bundleArgs = new Bundle();
        bundleArgs.putParcelableArrayList("categoriesSelected", categoriesSelected);
        bundleArgs.putParcelableArrayList("resourcesList", resourcesList);
        fragment.setArguments(bundleArgs);

        FragmentTransaction ft = fragmentManager.beginTransaction();

        if(fragment.getClass() == OutputListFragment.class)
            ft.setCustomAnimations(R.animator.slide_in_from_left, R.animator.slide_out_to_the_right);
        else
            ft.setCustomAnimations(R.animator.slide_in_from_right, R.animator.slide_out_to_the_left);

        ft.replace(R.id.content_frame, fragment).commit();
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectCategory(position);
        }
    }

    private void selectCategory(int position) {
        categoriesSelected = getCategoriesSelectedFromView();

        CategoriesSelectedEvent categoriesSelectedEvent = new CategoriesSelectedEvent(categoriesSelected);

        // update selected item and title, then close the drawer
        if (categoriesList.isItemChecked(position)) {
            Log.i(TAG + "selectCategory", "categoy added :" + navigationDrawerCategories[position]);
            categoriesSelectedEvent.setFilterAction(FilterAction.ADDED);
            eventBus.post(categoriesSelectedEvent);

        } else if (!categoriesList.isItemChecked(position)) {
            Log.i(TAG + "selectCategory", "categoy removed :" + navigationDrawerCategories[position]);
            categoriesSelectedEvent.setFilterAction(FilterAction.REMOVED);
            eventBus.post(categoriesSelectedEvent);
        }

        drawerLayout.closeDrawer(drawerView);
    }

    private ArrayList<Category> getCategoriesSelectedFromView() {
        ArrayList<Category> categoriesSelected = new ArrayList<>();

        int len = categoriesList.getCount();
        SparseBooleanArray checked = categoriesList.getCheckedItemPositions();
        for (int i = 0; i < len; i++)
            if (checked.get(i)) {
                categoriesSelected.add(categories.get(i));
            }
        return categoriesSelected;
    }

    public void displayDrawer() {
        drawerLayout.openDrawer(drawerView);
    }

}