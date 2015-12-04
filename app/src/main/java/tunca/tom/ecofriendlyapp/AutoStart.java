package tunca.tom.ecofriendlyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class AutoStart extends BroadcastReceiver{

    public void onReceive(Context context, Intent arg0){
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = myPreferences.edit();
        if(myPreferences.getBoolean("start_on_boot", false)) {
            editor.putBoolean("Service enabled", true);
            Intent mIntent = new Intent(context, TripMonitor.class);
            context.startService(mIntent);
        }else{
            editor.putBoolean("Service enabled", true);
        }
        editor.commit();
    }

}
