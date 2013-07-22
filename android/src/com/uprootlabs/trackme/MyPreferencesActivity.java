package com.uprootlabs.trackme;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class MyPreferencesActivity extends PreferenceActivity {
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    final Preference.OnPreferenceChangeListener intervalChangeListener = new Preference.OnPreferenceChangeListener() {

      @Override
      public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        if (newValue.equals("") || Integer.parseInt(newValue.toString()) <= 0) {
          final Context context = getApplicationContext();
          final String text_message = "Invalid input, interval cannot be empty, 0 or negative";
          final int duration = Toast.LENGTH_SHORT;

          // TODO Make it a alert box instead of a toast
          final Toast toast = Toast.makeText(context, text_message, duration);
          toast.show();
          return false;
        }
        return true;
      }

    };

    final SummarizedEditTextPreference captureFrequencyPreference = (SummarizedEditTextPreference) getPreferenceScreen().findPreference(
        this.getResources().getString(R.string.key_capture_interval));

    captureFrequencyPreference.setOnPreferenceChangeListener(intervalChangeListener);

    final SummarizedEditTextPreference updateFrequencyPreference = (SummarizedEditTextPreference) getPreferenceScreen().findPreference(
        this.getResources().getString(R.string.key_update_interval));

    updateFrequencyPreference.setOnPreferenceChangeListener(intervalChangeListener);

  }
}
