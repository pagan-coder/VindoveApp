package cz.httpvindove.vindove.Borrowed;

/*
 * Copyright 2013-2014 Ludwig M Brinckmann
 * Copyright 2014, 2015 devemux86
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * Utility functions that can be used across different mapsforge based
 * activities.
 */
public final class Utils {
    public static String ellipsize(String input, int maxLength) {
        maxLength++;
        String ellip = "…";
        if (input == null || input.length() <= maxLength
                || input.length() < ellip.length()) {
            return input;
        }
        return input.substring(0, maxLength - ellip.length()).concat(ellip);
    }

    /**
     * Compatibility method.
     *
     * @param a
     *            the current activity
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void enableHome(Activity a) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            a.getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Compatibility method.
     *
     * @param view
     *            the view to set the background on
     * @param background
     *            the background
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }

    private Utils() {
        throw new IllegalStateException();
    }

}