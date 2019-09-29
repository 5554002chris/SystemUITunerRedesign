package com.zacharee1.systemuituner

import android.Manifest
import android.content.Intent
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.topjohnwu.superuser.Shell
import com.zacharee1.systemuituner.activites.BaseAnimActivity
import com.zacharee1.systemuituner.activites.info.IntroActivity
import com.zacharee1.systemuituner.activites.instructions.SetupActivity
import com.zacharee1.systemuituner.util.checkPermissions
import com.zacharee1.systemuituner.util.prefs
import com.zacharee1.systemuituner.util.startUp
import com.zacharee1.systemuituner.util.sudo
import io.fabric.sdk.android.Fabric

class LauncherActivity : BaseAnimActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics())

        if (prefs.showIntro) {
            startActivity(Intent(this, IntroActivity::class.java))
        } else {
            val perms = arrayListOf(Manifest.permission.WRITE_SECURE_SETTINGS,
                    Manifest.permission.DUMP, Manifest.permission.PACKAGE_USAGE_STATS)

            val ret = checkPermissions(perms)
            ret.removeAll(SetupActivity.NOT_REQUIRED)

            if (ret.isNotEmpty()) {
                if (Shell.rootAccess()) {
                    sudo("pm grant $packageName ${Manifest.permission.WRITE_SECURE_SETTINGS}",
                            "pm grant $packageName ${Manifest.permission.DUMP}",
                            "pm grant $packageName ${Manifest.permission.PACKAGE_USAGE_STATS}")
                    startUp()
                    finish()
                } else {
                    SetupActivity.make(this, ret)
                    finish()
                }
            } else {
                startUp()
                finish()
            }
        }

        finish()
    }
}
