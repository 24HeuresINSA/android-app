package com.insalyon.les24heures.eventbus;

import android.widget.Filter;

import com.insalyon.les24heures.utils.OutputType;

/**
 * Created by remi on 18/04/15.
 */
public class FilterUpdateEnded {
    Filter filter;

    public FilterUpdateEnded(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

}
