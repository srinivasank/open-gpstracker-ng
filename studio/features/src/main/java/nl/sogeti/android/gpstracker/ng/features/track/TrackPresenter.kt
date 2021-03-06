/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng.features.track

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.features.util.AbstractSelectedTrackPresenter
import javax.inject.Inject

class TrackPresenter @Inject constructor(
        val trackTypeDescriptions: TrackTypeDescriptions,
        trackSelection: TrackSelection,
        contentController: ContentController)
    : AbstractSelectedTrackPresenter(trackSelection, contentController) {

    var navigation: TrackNavigator? = null
    val viewModel = TrackViewModel()

    //region Presenter context

    override fun onTrackUpdate(trackUri: Uri?, name: String) {
        if (trackUri != null) {
            viewModel.trackUri.set(trackUri)
            viewModel.name.set(name)
            viewModel.trackIcon.set(trackTypeDescriptions.loadTrackType(trackUri).drawableId)
        }
    }

    //endregion

    //region View callbacks

    fun onListOptionSelected() {
        navigation?.showTrackSelection()
    }

    fun onAboutOptionSelected() {
        navigation?.showAboutDialog()
    }

    fun onEditOptionSelected() {
        val trackUri = viewModel.trackUri.get()
        trackUri?.let { navigation?.showTrackEditDialog(it) }
    }

    fun onGraphsOptionSelected() {
        navigation?.showGraphs()
    }

    //endregion

    @Suppress("UNCHECKED_CAST")
    companion object {

        fun newFactory() =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val presenter = FeatureConfiguration.featureComponent.trackPresenter()
                        return presenter as T
                    }
                }
    }
}
