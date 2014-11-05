/**
 * Preference
 */
package jp.sato.kiyo.android.sahrekowaviewer;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author sk
 *
 */
public class SharekowaPreferenceActivity extends PreferenceActivity {

@Override
public void onCreate(Bundle savedInstanceState){
	super.onCreate(savedInstanceState);
	this.addPreferencesFromResource(R.xml.preference);
}

}
