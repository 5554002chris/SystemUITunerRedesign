package com.zacharee1.systemuituner.activites.instructions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zacharee1.systemuituner.R
import com.zacharee1.systemuituner.util.checkPermissions
import kotlinx.android.synthetic.main.command_box.view.*
import kotlinx.android.synthetic.main.permissions_fragment.view.*

class SetupActivity : AppIntro2() {
    companion object {
        const val PERMISSION_NEEDED = "permission_needed"

        val NOT_REQUIRED = arrayListOf(
                Manifest.permission.DUMP,
                Manifest.permission.PACKAGE_USAGE_STATS
        )

        fun make(context: Context, permissions: ArrayList<String>) {
            val intent = Intent(context, SetupActivity::class.java)
            intent.putStringArrayListExtra(PERMISSION_NEEDED, permissions)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private var permissionsNeeded: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setButtonState(backButton, false)
        showSkipButton(true)
        showPagerIndicator(false)

        supportActionBar?.hide()

        val intent = intent

        if (intent != null) {
            permissionsNeeded = ArrayList(intent.getStringArrayListExtra(PERMISSION_NEEDED)
                    ?.filterNot { checkCallingOrSelfPermission(it) == PackageManager.PERMISSION_GRANTED } ?: return)

            val frag = PermsFragment.newInstance(
                    resources.getString(R.string.permissions),
                    resources.getString(R.string.adb_setup),
                    permissionsNeeded,
                    resources.getColor(R.color.intro_1, null)
            )

            addSlide(frag)
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        if (permissionsNeeded != null) {
            val missing = checkPermissions(permissionsNeeded!!)
            val requiredMissing = ArrayList(missing).apply { removeAll(NOT_REQUIRED) }
            val notRequiredMissing = ArrayList(missing).apply { removeAll(requiredMissing) }

            if (requiredMissing.isNotEmpty()) {
                MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.missing_perms)
                        .setMessage(requiredMissing.toString())
                        .setPositiveButton(R.string.ok, null)
                        .show()
            } else {
                if (notRequiredMissing.isNotEmpty()) {
                    MaterialAlertDialogBuilder(this)
                            .setTitle(R.string.missing_perms)
                            .setMessage(R.string.missing_not_required_perms_desc)
                            .setPositiveButton(R.string.yes) { _, _ ->
                                finish()
                            }
                            .setNegativeButton(R.string.no, null)
                            .show()
                } else {
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        finish()
    }

    class PermsFragment : Fragment() {
        @SuppressLint("SetTextI18n")
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val args = arguments

            val view = inflater.inflate(R.layout.permissions_fragment, container, false)

            view.setBackgroundColor(args!!.getInt("color"))
            view.findViewById<View>(R.id.adb_instructions).backgroundTintList = ColorStateList.valueOf(args.getInt("color"))

            val title = view.findViewById<TextView>(R.id.title)
            title.text = args.getString("title", "")

            val desc = view.findViewById<TextView>(R.id.description)
            desc.text = args.getString("description", "")

            val text = view.findViewById<LinearLayout>(R.id.perms_layout)
            val perms = args.getStringArrayList("permissions")

            if (perms != null) {
                for (perm in perms) {
                    val commandBox = layoutInflater.inflate(R.layout.command_box, text, false)

                    commandBox.command.text = "adb shell pm grant ${view.context.packageName} $perm"
                    text.addView(commandBox)
                }

                view.adb_instructions.setOnClickListener {
                    val intent = Intent(activity, InstructionsActivity::class.java)
                    intent.putStringArrayListExtra(InstructionsActivity.ARG_COMMANDS, perms)
                    startActivity(intent)
                }
            }

            return view
        }

        companion object {
            fun newInstance(title: String, description: String, permissions: ArrayList<String>?, color: Int): PermsFragment {
                val fragment = PermsFragment()
                val args = Bundle()
                args.putString("title", title)
                args.putString("description", description)
                args.putInt("color", color)
                args.putStringArrayList("permissions", permissions)
                fragment.arguments = args
                return fragment
            }
        }
    }
}