package com.insalyon.les24heures.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lbillon on 4/22/15.
 */
public class LiveUpdate implements Parcelable {

    private String title;
    private String message;
    private long timePublished;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(message);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimePublished() {
        return timePublished;
    }

    public void setTimePublished(long timePublished) {
        this.timePublished = timePublished;
    }

    public boolean wasPublishedAfter(long referenceTimestamp){
        return timePublished > referenceTimestamp;
    }
}
