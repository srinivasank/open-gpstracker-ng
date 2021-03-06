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
package nl.sogeti.android.gpstracker.ng.features.gpximport

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.JobIntentService
import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.VALUE_TYPE_DEFAULT
import nl.sogeti.android.opengpstrack.ng.features.R
import timber.log.Timber
import javax.inject.Inject

class ImportService : JobIntentService() {

    @Inject
    lateinit var importController: GpxImportController
    @Inject
    lateinit var context: Context

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    companion object {

        private const val EXTRA_FILE = "GPX_FILE_URI"
        private const val EXTRA_TYPE = "EXTRA_TRACK_TYPE"
        private const val EXTRA_DIRECTORY = "GPX_DIRECTORY_URI"
        private val JOB_ID = R.menu.import_export

        fun importFile(uri: Uri, trackType: String = VALUE_TYPE_DEFAULT) {
            val work = Intent()
            work.putExtra(EXTRA_FILE, uri)
            work.putExtra(EXTRA_TYPE, trackType)
            enqueueWork(BaseConfiguration.appComponent.applicationContext(), ImportService::class.java, JOB_ID, work)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun importDirectory(uri: Uri, trackType: String = VALUE_TYPE_DEFAULT) {
            val work = Intent()
            work.putExtra(EXTRA_DIRECTORY, uri)
            work.putExtra(EXTRA_TYPE, trackType)
            enqueueWork(BaseConfiguration.appComponent.applicationContext(), ImportService::class.java, JOB_ID, work)
        }
    }

    @SuppressLint("NewApi")
    override fun onHandleWork(intent: Intent) {
        val trackType = intent.getStringExtra(EXTRA_TYPE)
        when {
            intent.hasExtra(EXTRA_FILE) -> importController.import(intent.getParcelableExtra(EXTRA_FILE), trackType)
            intent.hasExtra(EXTRA_DIRECTORY) -> importController.importDirectory(intent.getParcelableExtra(EXTRA_DIRECTORY), trackType)
            else -> Timber.e("Failed to handle import work $intent")
        }
    }
}
