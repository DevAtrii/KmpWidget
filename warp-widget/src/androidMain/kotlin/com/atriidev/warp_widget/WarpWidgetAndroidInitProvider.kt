package com.atriidev.warp_widget

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Library bootstrap: installs Glance cold-start click prepare when the process starts.
 *
 * Apps do **not** need their own ContentProvider. Register widgets from
 * [androidx.glance.appwidget.GlanceAppWidgetReceiver] `init` (or `provideGlance`);
 * on a cold tap this provider’s prepare path wakes that receiver so `register(…)` runs,
 * then re-binds [com.atriidev.warp_ui.WarpClicksRegistry].
 */
class WarpWidgetAndroidInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        WarpWidgetAndroidRegistry.installColdStartPrepare()
        context?.let(WarpWidgetAndroidReload::install)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
