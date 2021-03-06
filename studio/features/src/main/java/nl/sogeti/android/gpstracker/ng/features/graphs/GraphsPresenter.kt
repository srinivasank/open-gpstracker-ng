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
package nl.sogeti.android.gpstracker.ng.features.graphs

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.model.Preferences
import nl.sogeti.android.gpstracker.ng.features.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.features.model.not
import nl.sogeti.android.gpstracker.ng.features.summary.Summary
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.util.AbstractSelectedTrackPresenter
import nl.sogeti.android.gpstracker.utils.ofMainThread
import nl.sogeti.android.gpstracker.utils.postMainThread
import javax.inject.Inject

class GraphsPresenter @Inject constructor(
        private val summaryManager: SummaryManager,
        private val preferences: Preferences,
        trackSelection: TrackSelection,
        contentController: ContentController)
    : AbstractSelectedTrackPresenter(trackSelection, contentController) {

    internal val viewModel = GraphsViewModel()

    private var graphDataProvider: GraphDataProvider
    private var trackSummary: Summary? = null
    private var runningSelection = false
    private val inverseSpeedPreferenceObserver = Observer<Boolean> {
        viewModel.inverseSpeed.set(it ?: false)
    }

    init {
        preferences.inverseSpeed.observeForever(inverseSpeedPreferenceObserver)
        resetTrack()
        graphDataProvider = GraphSpeedOverTimeDataProvider()
        viewModel.durationSelected.set(true)
    }

    override fun onStart() {
        super.onStart()
        summaryManager.start()
    }

    override fun onStop() {
        summaryManager.stop()
        super.onStop()
    }

    override fun onCleared() {
        preferences.inverseSpeed.removeObserver(inverseSpeedPreferenceObserver)
        super.onCleared()
    }

    //region View callbacks

    fun didSelectDistance() {
        if (runningSelection || viewModel.distanceSelected.get())
            return
        runningSelection = true
        viewModel.distanceSelected.set(true)
        viewModel.durationSelected.set(false)
        ofMainThread {
            graphDataProvider = GraphSpeedOVerDistanceDataProvider()
            trackSummary?.let { fillGraphWithSummary(it) }
            postMainThread {
                runningSelection = false
            }
        }
    }

    fun didSelectTime() {
        if (runningSelection || viewModel.durationSelected.get())
            return
        runningSelection = true
        viewModel.distanceSelected.set(false)
        viewModel.durationSelected.set(true)
        ofMainThread {
            graphDataProvider = GraphSpeedOverTimeDataProvider()
            trackSummary?.let { fillGraphWithSummary(it) }
            postMainThread {
                runningSelection = false
            }
        }
    }

    fun onInverseSpeedSelected() {
        preferences.inverseSpeed.not()
    }

    //endregion

    //region update

    private fun resetTrack() {
        viewModel.distance.set(0F)
        viewModel.timeSpan.set(0L)
        viewModel.speed.set(0F)
        viewModel.waypoints.set("-")
        viewModel.startTime.set(0L)
        viewModel.duration.set(0L)
        viewModel.paused.set(0L)
    }

    override fun onTrackUpdate(trackUri: Uri?, name: String) {
        viewModel.trackUri.set(trackUri)
        if (trackUri != null) {
            summaryManager.collectSummaryInfo(trackUri) {
                trackSummary = it
                fillSummaryNumbers(it)
                fillGraphWithSummary(it)
            }
        } else {
            resetTrack()
        }
    }

    private fun fillSummaryNumbers(summary: Summary) {
        viewModel.waypoints.set(summary.count.toString())
        viewModel.startTime.set(summary.startTimestamp)
        val total = summary.stopTimestamp - summary.startTimestamp
        val pausedTime = total - summary.trackedPeriod
        viewModel.paused.set(pausedTime)
        viewModel.distance.set(summary.distance)
        viewModel.duration.set(summary.trackedPeriod)
        viewModel.timeSpan.set(total)

        val seconds = summary.trackedPeriod / 1000F
        val speed = if (seconds > 0) summary.distance / seconds else 0F
        viewModel.speed.set(speed)
    }

    private fun fillGraphWithSummary(it: Summary) {
        viewModel.graphData.set(graphDataProvider.calculateGraphPoints(it))
        viewModel.xLabel.set(graphDataProvider.xLabel)
        viewModel.yLabel.set(graphDataProvider.yLabel)
        viewModel.graphLabels.set(graphDataProvider.valueDescriptor)
    }

    @Suppress("UNCHECKED_CAST")
    companion object {

        fun newFactory() =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val presenter = FeatureConfiguration.featureComponent.graphsPresenter()
                        return presenter as T
                    }
                }
    }
}

