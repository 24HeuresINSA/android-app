package com.insalyon.les24heures.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.insalyon.les24heures.MainActivity;
import com.insalyon.les24heures.R;
import com.insalyon.les24heures.fragments.DetailFragment;
import com.insalyon.les24heures.model.Resource;
import com.insalyon.les24heures.service.impl.ResourceServiceImpl;
import com.insalyon.les24heures.utils.DetailSlidingUpPanelLayoutNullActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by remi on 04/02/15.
 */
//TODO faire une jolie interface du SlidingUpPanelLayout avec fragment inside et gestion du parallax header
public class DetailSlidingUpPanelLayout extends SlidingUpPanelLayout{
    private static final String TAG = DetailSlidingUpPanelLayout.class.getCanonicalName();

    private static ResourceServiceImpl resourceService = ResourceServiceImpl.getInstance();


    private DetailScrollView detailScrollView;
    private TextView nextSchedule;
    private ImageButton favoriteImageButton;
    private TextView detailSlidingTitle;
    private TextView detailSlidingDescription;

    private View parallaxHeader;
    private DrawerArrowDrawable drawerArrowDrawable;


    private Integer wideHeight;
    private MainActivity activity;
    private boolean isSetup = false;


    private float anchored;
    private int scrollingHeaderHeight;
    private DetailSlidingUpPanelLayout self;

    private PanelSlideListener panelSlideListener = new PanelSlideListener() {
        @Override
        public void onPanelSlide(View panel, float slideOffset) {
            Log.i(TAG, "onPanelSlide, offset " + slideOffset);

            float newParallaxHeaderPos;
            float parallaxContentFrame = -self.getCurrentParalaxOffset();

            if (slideOffset <= 0) {//from visible to hidden and vice versa
                //parallax
                newParallaxHeaderPos = (wideHeight - scrollingHeaderHeight) * (1 - slideOffset / (anchored));
                newParallaxHeaderPos = newParallaxHeaderPos + parallaxContentFrame;
                parallaxHeader.setTranslationY(newParallaxHeaderPos);
                //TODO on peut juste se contenter de cacher la main pic a cette etape au lieu de la bouger

            } else if (slideOffset < anchored) { //from visible to anchored and vice versa
                //parallax
                newParallaxHeaderPos = (wideHeight - scrollingHeaderHeight) * (1 - slideOffset / (anchored));
                newParallaxHeaderPos = newParallaxHeaderPos + parallaxContentFrame;
                parallaxHeader.setTranslationY(newParallaxHeaderPos);

                //arrowDrawable
                float param = 1 / anchored * slideOffset;
                if (param < 0) param = 0;
                else if (param > 1) param = 1;

                drawerArrowDrawable.setParameter(param);

//                    activity.getActionBar().setTitle("");
            } else {      //from anchored to expand and vice versa
                drawerArrowDrawable.setParameter(1);
//                // TODO ou pas ?
//                    newParallaxHeaderPos = parallaxHeight*slideOffset/(1-anchored);
//                  newParallaxHeaderPos = (height * (1 - slideOffset));
//                    newParallaxHeaderPos = newParallaxHeaderPos + parallaxContentFrame;
//                    parallaxHeader.setTranslationY(newParallaxHeaderPos);
            }

            if (slideOffset == 0) {
                detailScrollView.setIsScrollEnable(true);
            }

        }

        @Override
        public void onPanelExpanded(View panel) {
            Log.i(TAG, "onPanelExpanded");
            detailScrollView.setIsScrollEnable(true);
            nextSchedule.setVisibility(View.GONE);
            favoriteImageButton.setVisibility(View.VISIBLE);
//                this.setDragView(slidingDetailHeader);

            activity.invalidateOptionsMenu();
//                activity.getActionBar().setTitle(resource.getTitle());   => hide title in detail

        }

        @Override
        public void onPanelCollapsed(View panel) {
            Log.i(TAG, "onPanelCollapsed");
            nextSchedule.setVisibility(View.VISIBLE);
            favoriteImageButton.setVisibility(View.GONE);
//                this.setDragView(wholeSlidingLayout);
            detailScrollView.setIsScrollEnable(false);
            detailScrollView.fullScroll(ScrollView.FOCUS_UP);

            activity.invalidateOptionsMenu();
            activity.restoreTitle();
        }

        @Override
        public void onPanelAnchored(View panel) {
            Log.i(TAG, "onPanelAnchored");
            nextSchedule.setVisibility(View.GONE);
            favoriteImageButton.setVisibility(View.VISIBLE);
            detailScrollView.setIsScrollEnable(false);

            activity.invalidateOptionsMenu();
        }

        @Override
        public void onPanelHidden(View panel) {
            Log.i(TAG, "onPanelHidden");
        }
    };
    private DetailFragment detailFragment;


    public DetailSlidingUpPanelLayout(Context context) {
        super(context);
    }

    public DetailSlidingUpPanelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetailSlidingUpPanelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }



    /**
     * if return false it's because the height of the view isn't yet ready, the setup will be done once the view is rendered and
     * the view height is processed
     * @return
     */
    public Boolean setup() throws DetailSlidingUpPanelLayoutNullActivity {
        if(wideHeight == null)
            return false;
        if(activity == null){
            throw new DetailSlidingUpPanelLayoutNullActivity();
        }

        findDetailView();

        drawerArrowDrawable = activity.getDrawerArrowDrawable();

        //get params
        anchored = Float.parseFloat(getResources().getString(R.string.detail_anchored));
        scrollingHeaderHeight = (int) getResources().getDimension(R.dimen.detail_header_height);
        final int parallaxHeight = (int) ((wideHeight - scrollingHeaderHeight) * (1-anchored));//407; //paralax height

        //set parallaxHeader
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) parallaxHeader.getLayoutParams();
        params.height = parallaxHeight;
        parallaxHeader.setLayoutParams(params);
        parallaxHeader.setTranslationY(wideHeight);

        self = this;

        //setup
        this.setAnchorPoint(anchored);
        this.hidePanel();  //2.0.4 sera remplacé par mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN); à la prochaine release
//        this.setDragView(wholeSlidingLayout); //default but to be clear
        detailScrollView.setIsScrollEnable(false);

        this.setPanelSlideListener(panelSlideListener);

        isSetup = true;
        return true;
    }


    public void setDetailFragment(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    public void setParallaxHeader(View parallaxHeader) {
        this.parallaxHeader = parallaxHeader;
    }

    private void findDetailView() {
        detailScrollView = (DetailScrollView) detailFragment.getView().findViewById(R.id.detail_scrollView);
        nextSchedule = (TextView) detailFragment.getView().findViewById(R.id.detail_next_schedule);
        favoriteImageButton = (ImageButton) detailFragment.getView().findViewById(R.id.detail_favorites);
        detailSlidingTitle = (TextView) detailFragment.getView().findViewById(R.id.detail_sliding_title);
        detailSlidingDescription = (TextView) detailFragment.getView().findViewById(R.id.detail_desciption_text);
    }



    public void showDetailPanel(Resource res){
        detailFragment.notifyDataChanged(res);
        this.showPanel();
    }

    public Boolean hideDetailPanel(){
        if(!this.isPanelHidden()) {
            this.hidePanel();
            return true;
        }else{
            return false;
        }

    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
        //ce sont les dimensions en pixels sans la action bar
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        wideHeight = MeasureSpec.getSize(heightMeasureSpec);

        if(!isSetup)
            try {
                setup();
            } catch (DetailSlidingUpPanelLayoutNullActivity detailSlidingUpPanelLayoutNullActivity) {
                detailSlidingUpPanelLayoutNullActivity.printStackTrace();
            }
    }


    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }


    public Boolean isAnchoredOrExpanded(){
        return this.isPanelExpanded() || this.isPanelAnchored();
    }


}