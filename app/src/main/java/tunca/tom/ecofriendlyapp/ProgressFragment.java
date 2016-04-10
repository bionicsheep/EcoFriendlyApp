package tunca.tom.ecofriendlyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ProgressFragment extends Fragment {

    public ProgressFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_progress, container, false);

        return rootView;
    }

    @Override
    public void onResume(){
        ((MainActivity)getActivity()).getNavigationView().getMenu().getItem(0).setChecked(true);
        super.onResume();
    }

}