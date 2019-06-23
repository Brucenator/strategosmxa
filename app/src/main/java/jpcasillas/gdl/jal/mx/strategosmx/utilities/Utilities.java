package jpcasillas.gdl.jal.mx.strategosmx.utilities;

import android.content.Context;
import android.net.ConnectivityManager;

public class Utilities {

    public static final boolean CheckInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            return true;

        } else {
            return false;
        }
    }
}
