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
package nl.sogeti.android.gpstracker.service.mock

import android.content.Context
import android.net.Uri
import android.os.Handler
import nl.sogeti.android.gpstracker.service.integration.ServiceCommanderInterface
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants.*
import nl.sogeti.android.gpstracker.service.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.service.util.readName

class MockServiceManager(val context: Context) : ServiceManagerInterface, ServiceCommanderInterface {

    val broadcaster = MockBroadcastSender()
    val gpsRecorder = Recorder()

    override fun startup(runnable: Runnable?) {
        started = true
        if (loggingState == STATE_UNKNOWN) {
            globalState.loggingState = STATE_STOPPED
        }
        runnable?.run()
    }

    override fun shutdown() {
        started = false
    }

    override fun getLoggingState(): Int = globalState.loggingState

    override fun getTrackId(): Long = globalState.trackId

    override fun hasForInitialName(trackUri: Uri) = trackUri.readName() == "New mock"

    override fun startGPSLogging() {
        startGPSLogging("New mock")
    }

    override fun startGPSLogging(trackName: String?) {
        globalState.loggingState = STATE_LOGGING
        gpsRecorder.startRecording(trackName)
        broadcaster.sendStartedRecording(context, trackId)
    }

    override fun stopGPSLogging() {
        globalState.loggingState = STATE_STOPPED

        broadcaster.sendStoppedRecording(context)
    }

    override fun pauseGPSLogging() {
        globalState.loggingState = STATE_PAUSED

        broadcaster.sendPausedRecording(context, trackId)
    }

    override fun resumeGPSLogging() {
        globalState.loggingState = STATE_LOGGING

        broadcaster.sendResumedRecording(context, trackId)
        gpsRecorder.resumeRecording()
    }

    override fun isPackageInstalled(): Boolean = true

    fun reset() {
        started = false
        globalState.loggingState = STATE_UNKNOWN
        globalState.trackId = -1L
    }

    companion object globalState {
        var loggingState = STATE_UNKNOWN
        var pauseWaypointGenerations = false
        private var started = false
        private var trackId = -1L
        private var segmentId = 10L
        private var waypointId = 100L
    }

    class Recorder {
        var shouldScheduleWaypoints = true

        fun startRecording(trackName: String?) {
            recordNewTrack(trackName)
            postNextWaypoint()
        }

        fun resumeRecording() {
            postNextWaypoint()
        }

        private fun postNextWaypoint() {
            if (shouldScheduleWaypoints) {
                Handler().postDelayed({
                    if (loggingState == STATE_LOGGING) {
                        if (!pauseWaypointGenerations) {
                            recordNewWaypoint()
                        }
                        postNextWaypoint()
                    }
                }, 2500)
            }
        }

        private fun recordNewTrack(trackName: String?) {
            if (trackId > 0) trackId++ else trackId = 2
            MockTracksContentProvider.addTrack(trackId, trackName)
            recordNewSegment()
        }

        private fun recordNewSegment() {
            segmentId++
            MockTracksContentProvider.addSegment(trackId, segmentId)
            recordNewWaypoint()
        }

        private fun recordNewWaypoint() {
            waypointId++
            val amplitude = 0.0001 + waypointId / 10000.0
            val angularSpeed = 5.0
            val latitude = 51.2605159 + amplitude * Math.cos(Math.toRadians(waypointId.toDouble() * angularSpeed))
            val longitude = 4.2301078 + amplitude * Math.sin(Math.toRadians(waypointId.toDouble() * angularSpeed))
            MockTracksContentProvider.addWaypoint(trackId, segmentId, waypointId, latitude, longitude)
        }
    }
}
