package com.zacharee1.systemuituner.activites.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreference
import com.zacharee1.systemuituner.R
import com.zacharee1.systemuituner.activites.BaseAnimActivity
import com.zacharee1.systemuituner.fragments.AnimFragment
import com.zacharee1.systemuituner.services.SafeModeService
import com.zacharee1.systemuituner.util.PrefManager
import com.zacharee1.systemuituner.util.checkSamsung
import com.zacharee1.systemuituner.util.forEachPreference
import com.zacharee1.systemuituner.util.getAnimTransaction

class SettingsActivity : BaseAnimActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        supportFragmentManager
                .getAnimTransaction()
                .replace(R.id.content_main, GeneralPreferenceFragment())
                .commit()
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class GeneralPreferenceFragment : AnimFragment() {
        override val prefsRes = R.xml.settings_general

        override fun onSetTitle() = resources.getString(R.string.settings)

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            super.onCreatePreferences(savedInstanceState, rootKey)

            setHasOptionsMenu(true)
            setUpQSStuff()
            setSwitchListeners()
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                activity?.finish()
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        private fun setUpQSStuff() {
            val category = findPreference<PreferenceCategory>("quick_settings")!!

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                category.isEnabled = false
                category.forEachPreference {
                    it.summary = resources.getText(R.string.requires_nougat)
                }
            }
        }

        private fun setSwitchListeners() {
            val safeMode = findPreference<SwitchPreference>("safe_mode")!!
            val safeNotif = findPreference<SwitchPreference>("show_safe_mode_notif")!!

            safeMode.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toBoolean()) {
                    activity?.stopService(Intent(activity, SafeModeService::class.java))
                    ContextCompat.startForegroundService(activity!!, Intent(activity, SafeModeService::class.java))
                } else {
                    activity?.stopService(Intent(activity, SafeModeService::class.java))
                }

                true
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                safeNotif.isEnabled = false
                safeNotif.summary = resources.getText(R.string.safe_mode_notif_desc_not_supported)
            }

            if (context!!.checkSamsung() && Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                findPreference<CheckBoxPreference>(PrefManager.SAFE_MODE_ROW_COL)!!.apply {
                    isEnabled = false
                    isChecked = false
                    setSummary(R.string.setting_not_on_touchwiz_pie)
                }

                findPreference<CheckBoxPreference>(PrefManager.SAFE_MODE_HEADER_COUNT)!!.apply {
                    isEnabled = false
                    isChecked = false
                    setSummary(R.string.setting_not_on_touchwiz_pie)
                }
            }

            findPreference<Preference>(PrefManager.SAFE_MODE_HIGH_BRIGHTNESS_WARNING)
                    ?.isVisible = context!!.checkSamsung()

            findPreference<Preference>(PrefManager.SAFE_MODE_SNOOZE_OPTIONS)
                    ?.isVisible = Build.VERSION.SDK_INT > Build.VERSION_CODES.O
        }
    }
}
