package le.smartlamp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import le.smartlamp.utils.MessageCodes;

import java.util.List;

public class SettingsActivity extends PreferenceActivity{
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		PreferenceCategory fakeHeader;
		
		// Add 'auto change' preferences.
		addPreferencesFromResource(R.xml.pref_autochange);
		
		// Add 'timer' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_timer);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_timer);
		
		// Add 'about' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_about);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_about);

		Preference aboutPref = (Preference) findPreference("about");
		aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				AboutDialog about = new AboutDialog(SettingsActivity.this);
				about.show();
				return true;
			}
		});
		
		Preference firmwarePref = (Preference) findPreference("firmware");
		firmwarePref.setSummary(ColorPickerActivity.firmwareVersion);
		
		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("timer"));
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
				>= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener
		sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();
			
			if (preference instanceof ListPreference) {
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set timer flag on after select time to fade off
				if (!listPreference.getValue().equals(stringValue)) {
					if (ColorPickerActivity.bleController != null && ColorPickerActivity.bleController.getConnectingState()) {
						ColorPickerActivity.bleController.makeChange("*255|255|255|" + MessageCodes.TIMER_REQ.toString()
								+ "|" + stringValue + "#");
						ColorPickerActivity.isSentTimer = true;
					}
					else
						ColorPickerActivity.isSentTimer = false;
				}
				
				// Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

			} else {
				preference.setSummary(stringValue);
			}
			
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 *
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(), ""));
	}

	public static class AutochangePreference extends PreferenceFragment implements OnSharedPreferenceChangeListener{
		
		//public static final String KEY_COLOR_VARY = "color_vary_checkbox";
		public static final String KEY_COLOR_FADE = "color_fade_checkbox";
		//static CheckBoxPreference colorVaryCheckbox;
		static CheckBoxPreference colorFadeCheckbox;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_autochange);
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			/*colorVaryCheckbox = (CheckBoxPreference) findPreference(KEY_COLOR_VARY);
			colorFadeCheckbox = (CheckBoxPreference) findPreference(KEY_COLOR_FADE);
			
			if (key.equals(KEY_COLOR_VARY)) {
				//colorFadeCheckbox.setChecked(false);
				if (!colorVaryCheckbox.isChecked()) colorVaryCheckbox.setChecked(true);
				if (colorFadeCheckbox.isChecked()) colorFadeCheckbox.setChecked(false);
				//System.out.println("vary -> " + colorVaryCheckbox.isChecked());
	        }
			else {
				//colorVaryCheckbox.setChecked(false);
				if (colorVaryCheckbox.isChecked()) colorVaryCheckbox.setChecked(false);
				if (!colorFadeCheckbox.isChecked()) colorFadeCheckbox.setChecked(true);
				//System.out.println("fade -> " + colorFadeCheckbox.isChecked());
			}*/
		}
		
		@Override
		public void onResume() {
		    super.onResume();
		    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
		    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		    super.onPause();
		}
	}

	public static class TimerPreference extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_timer);

			bindPreferenceSummaryToValue(findPreference("timer"));
		}
	}
	
	public static class InformationPreference extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_information);

			Preference pref = null;
			PackageInfo pi = null;
			try {
				pi = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			
	        IconPreference test = (IconPreference) findPreference("application_name");
			Resources res = getResources();
			@SuppressWarnings("deprecation")
			Drawable icon = res.getDrawable(R.drawable.ic_launcher);
			test.setIcon(icon);
			
			pref = findPreference("version_name");
	        pref.setSummary(pi.versionName);

			pref = findPreference("firmware_name");
			pref.setSummary(ColorPickerActivity.firmwareVersion);
		}
	}
}
