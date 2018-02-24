/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.ng.features.trackdelete

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.service.util.readName
import javax.inject.Inject

class TrackDeletePresenter(val model: TrackDeleteModel, val view: TrackDeleteModel.View) : ContextedPresenter() {

    @Inject
    lateinit var summaryManager: SummaryManager

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun didStart() {
        val trackUri = model.trackUri.get()
        trackUri?.let {
            loadTrackName(context, trackUri)
        }
    }

    override fun willStop() {
    }

    fun ok() {
        val trackUri = model.trackUri.get()
        trackUri?.let {
            deleteTrack(context, trackUri)
            summaryManager.removeFromCache(trackUri)
            view.dismiss()
        }
    }

    fun cancel() {
        view.dismiss()
    }

    private fun loadTrackName(context: Context, trackUri: Uri) {
        val trackName = trackUri.readName(context)
        model.name.set(trackName)
    }

    private fun deleteTrack(context: Context, trackUri: Uri) {
        context.contentResolver.delete(trackUri, null, null)
    }
}
