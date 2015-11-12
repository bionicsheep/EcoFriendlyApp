package tunca.tom.ecofriendlyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by aless_000 on 11/12/2015.
 */
public class ProgressFragment extends Fragment {

    public ProgressFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_progress, container, false);

        return rootView;
    }

}