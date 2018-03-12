/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
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
package nl.sogeti.android.gpstracker.ng.features.control

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.trackedit.NameGenerator
import nl.sogeti.android.gpstracker.ng.features.util.ConnectedServicePresenter
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.service.util.*
import nl.sogeti.android.gpstracker.utils.contentprovider.runQuery
import nl.sogeti.android.opengpstrack.ng.features.R
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

class ControlPresenter(private val viewModel: ControlViewModel) : ConnectedServicePresenter() {
    @Inject
    lateinit var nameGenerator: NameGenerator
    @Inject
    @field:Named("SystemBackgroundExecutor")
    lateinit var asyncExecutor: Executor

    val handler = Handler(Looper.getMainLooper())
    private val enableRunnable = { enableButtons() }

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    //region Service connection

    override fun didConnectToService(context: Context, trackUri: Uri?, name: String?, loggingState: Int) {
        viewModel.state.set(loggingState)
        enableButtons()
    }

    override fun didChangeLoggingState(context: Context, trackUri: Uri?, name: String?, loggingState: Int) {
        viewModel.state.set(loggingState)
        enableButtons()

        if (trackUri != null && loggingState == STATE_LOGGING) {
            asyncExecutor.execute {
                checkForInitialName(context, trackUri)
            }
        }
    }

    //endregion

    //region View callback

    fun onClickLeft() {
        disableUntilChange(200)
        if (viewModel.state.get() == STATE_LOGGING) {
            stopLogging(context)
        } else if (viewModel.state.get() == STATE_PAUSED) {
            stopLogging(context)
        }
    }

    fun onClickRight() {
        disableUntilChange(200)
        if (viewModel.state.get() == STATE_STOPPED) {
            startLogging(context)
        } else if (viewModel.state.get() == STATE_LOGGING) {
            pauseLogging(context)
        } else if (viewModel.state.get() == STATE_PAUSED) {
            resumeLogging(context)
        }
    }

    //endregion

    private fun disableUntilChange(timeout: Long) {
        viewModel.enabled.set(false)
        handler.postDelayed(enableRunnable, timeout)
    }

    private fun enableButtons() {
        handler.removeCallbacks { enableRunnable }
        viewModel.enabled.set(true)
    }

    private fun checkForInitialName(context: Context, trackUri: Uri) {
        val name = trackUri.readName()
        if (name == context.getString(R.string.initial_track_name)) {
            val generatedName = nameGenerator.generateName(context, Calendar.getInstance())
            trackUri.updateName(generatedName)
        }
    }

    fun startLogging(context: Context) {
        serviceManager.startGPSLogging(context, context.getString(R.string.initial_track_name))
    }

    fun stopLogging(context: Context) {
        serviceManager.stopGPSLogging(context)
        deleteEmptyTrack(context.contentResolver, serviceManager.trackId)
    }

    fun pauseLogging(context: Context) {
        serviceManager.pauseGPSLogging(context)
    }

    fun resumeLogging(context: Context) {
        serviceManager.resumeGPSLogging(context)
    }

    private fun deleteEmptyTrack(contentResolver: ContentResolver, trackId: Long) {
        if (trackId <= 0) {
            return
        }

        val waypointsUri = waypointsUri(trackId)
        val firstWaypointId = waypointsUri.runQuery(contentResolver) { it.getLong(0) } ?: -1L
        if (firstWaypointId == -1L) {
            contentResolver.delete(trackUri(trackId), null, null)
        }
    }
}
