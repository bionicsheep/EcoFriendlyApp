package tunca.tom.ecofriendlyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AutoStart extends BroadcastReceiver{

    public void onReceive(Context context, Intent arg0){
        Intent mIntent = new Intent(context, TripMonitor.class);
        context.startService(mIntent);
    }

}
