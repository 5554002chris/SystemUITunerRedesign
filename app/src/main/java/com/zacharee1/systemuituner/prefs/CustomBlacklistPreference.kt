package com.zacharee1.systemuituner.prefs

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zacharee1.systemuituner.R
import com.zacharee1.systemuituner.misc.CustomBlacklistInfo
import com.zacharee1.systemuituner.util.prefs

class CustomBlacklistPreference : SwitchPreference {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    init {
        isIconSpaceReserved = true
        isEnabled = true
        isSelectable = true
        layoutResource = R.layout.clickable_icon_pref
        icon = ContextCompat.getDrawable(context, R.drawable.ic_remove_circle_outline_black_24dp)
    }

    override fun setKey(key: String?) {
        super.setKey(key)

        summary = key
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.isDividerAllowedBelow = false
        holder.isDividerAllowedAbove = false

        holder.itemView.findViewById<LinearLayout>(R.id.icon_frame).apply {
            setOnClickListener {
                MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(context.resources
                                .getString(R.string.remove_custom_blacklist_item_desc, title))
                        .setPositiveButton(R.string.yes) { _, _ ->
                            context.prefs.removeCustomBlacklistItem(
                                    CustomBlacklistInfo(key, title.toString()))
                        }
                        .setNegativeButton(R.string.no, null)
                        .show()
            }
        }

        holder.itemView.findViewById<ImageView>(android.R.id.icon).apply {
            setColorFilter(Color.RED)
        }
    }
}