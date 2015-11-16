package tunca.tom.ecofriendlyapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CheckBox;


public class SettingsActivity extends FragmentActivity {

    @Override
    protected void onCreate (Bundle savedInsteanceState){
        super.onCreate(savedInsteanceState);
        setContentView(R.layout.settings_activity);
    }



    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch(view.getId()) {
            case R.id.checkbox_Demo_Mode:
                if (checked) {
                    // action if this is checked
                }
                else {

                }
                break;
            case R.id.checkbox_Service_Enable:
                if (checked) {
                    // action if this is checked
                }
                else {

                }
                break;

        }
    }



}
