package org.fossify.contacts.helpers

import android.content.Context
import org.fossify.commons.helpers.BaseConfig
import org.fossify.commons.helpers.SHOW_TABS

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var showTabs: Int
        get() = prefs.getInt(SHOW_TABS, ALL_TABS_MASK)
        set(showTabs) = prefs.edit().putInt(SHOW_TABS, showTabs).apply()

    var autoBackupContactSources: Set<String>
        get() = prefs.getStringSet(AUTO_BACKUP_CONTACT_SOURCES, setOf())!!
        set(autoBackupContactSources) = prefs.edit().remove(AUTO_BACKUP_CONTACT_SOURCES).putStringSet(AUTO_BACKUP_CONTACT_SOURCES, autoBackupContactSources)
            .apply()

    // Degrees, 0-360. Rotates every color in the gel avatar/group-icon palette
    // together (via HSV hue rotation, saturation/value untouched) rather than
    // needing 8 separate color pickers - one slider retints the whole palette
    // while preserving the relative hue spacing between the 8 base colors.
    var gelAvatarHueShift: Int
        get() = prefs.getInt(GEL_AVATAR_HUE_SHIFT, 0)
        set(gelAvatarHueShift) = prefs.edit().putInt(GEL_AVATAR_HUE_SHIFT, gelAvatarHueShift).apply()
}
