package nl.sogeti.android.gpstracker.ng.common.controllers.content

import android.content.Context


class ContentControllerProvider {

    fun createContentControllerProvider(context: Context, listener: ContentController.Listener): ContentController {
        return ContentController(context, listener);
    }
}