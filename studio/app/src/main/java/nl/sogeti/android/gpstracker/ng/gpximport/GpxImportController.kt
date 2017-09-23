/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
-
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
package nl.sogeti.android.gpstracker.ng.gpximport

import android.content.ContentResolver
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import javax.inject.Inject

class GpxImportController(val callback: Callback) {

    @Inject
    lateinit var gpxParserFactory: GpxParserFactory

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    fun import(contentResolver: ContentResolver, uri: Uri, length: Int? = null) {
        callback.didStartImport()
        val parser = gpxParserFactory.createParse()
        parser.parse(contentResolver.openInputStream(uri))
        callback.didCompleteImport()
    }

    interface Callback {
        fun didStartImport()
        fun onProgress(progress: Int, goal: Int)
        fun didCompleteImport()
    }
}

