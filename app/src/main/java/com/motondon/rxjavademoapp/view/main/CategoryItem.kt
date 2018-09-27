package com.motondon.rxjavademoapp.view.main

import android.app.Activity

import java.io.Serializable

/**
 * Pair consisting of the name of an example and the activity corresponding to the example.
 *
 */
class CategoryItem(
        val mExampleActivityClass: Class<out Activity>, val mExampleName: String, val mExampleDetails: String) : Serializable

