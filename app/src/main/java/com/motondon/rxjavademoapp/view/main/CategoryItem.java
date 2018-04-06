package com.motondon.rxjavademoapp.view.main;

import android.app.Activity;

import java.io.Serializable;

/**
 * Pair consisting of the name of an example and the activity corresponding to the example.
 *
 */
public class CategoryItem implements Serializable {

    public final Class<? extends Activity> mExampleActivityClass;
    public final String mExampleName;
    public final String mExampleDetails;

    public CategoryItem(
            Class<? extends Activity> exampleActivityClass, String exampleName, String exampleDetails) {
        mExampleActivityClass = exampleActivityClass;
        mExampleName = exampleName;
        mExampleDetails = exampleDetails;
    }
}

