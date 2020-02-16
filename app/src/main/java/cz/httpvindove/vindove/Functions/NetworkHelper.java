package cz.httpvindove.vindove.Functions;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {
    /**
     * Checks if the network is available
     * @return boolean
     */
    public static boolean isNetworkAvailable(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        // check if it is !connected!
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
