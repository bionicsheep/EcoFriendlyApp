package tunca.tom.ecofriendlyapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by aless_000 on 11/12/2015.
 */
public class TripMonitor extends Service{

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }



}
