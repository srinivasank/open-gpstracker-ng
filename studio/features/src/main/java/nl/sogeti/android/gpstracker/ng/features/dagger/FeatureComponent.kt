package nl.sogeti.android.gpstracker.ng.features.dagger

import dagger.Component
import nl.sogeti.android.gpstracker.ng.base.dagger.AppComponent
import nl.sogeti.android.gpstracker.ng.features.about.AboutModel
import nl.sogeti.android.gpstracker.ng.features.control.ControlFragment
import nl.sogeti.android.gpstracker.ng.features.control.ControlPresenter
import nl.sogeti.android.gpstracker.ng.features.gpximport.GpxImportController
import nl.sogeti.android.gpstracker.ng.features.gpximport.ImportService
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphLabelsBindings
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphSpeedOVerDistanceDataProvider
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphSpeedOverTimeDataProvider
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphsPresenter
import nl.sogeti.android.gpstracker.ng.features.map.TrackMapPresenter
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingNavigation
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingPresenter
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.track.TrackActivity
import nl.sogeti.android.gpstracker.ng.features.track.TrackNavigator
import nl.sogeti.android.gpstracker.ng.features.track.TrackPresenter
import nl.sogeti.android.gpstracker.ng.features.trackdelete.TrackDeletePresenter
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackEditPresenter
import nl.sogeti.android.gpstracker.ng.features.tracklist.*
import nl.sogeti.android.gpstracker.ng.features.wear.LoggingReceiver
import nl.sogeti.android.gpstracker.ng.features.wear.LoggingService
import nl.sogeti.android.gpstracker.ng.features.wear.PhoneMessageListenerService
import nl.sogeti.android.gpstracker.ng.features.wear.StatisticsCollector
import nl.sogeti.android.gpstracker.service.dagger.ServiceComponent
import javax.inject.Named

@FeatureScope
@Component(modules = [FeatureModule::class, VersionInfoModule::class],
        dependencies = [AppComponent::class, ServiceComponent::class])
interface FeatureComponent {

    @Named("shareProviderAuthority")
    fun providerShareAuthority(): String

    fun inject(trackNavigator: TrackNavigator)
    fun inject(gpxImportController: GpxImportController)
    fun inject(importService: ImportService)
    fun inject(aboutModel: AboutModel)
    fun inject(controlPresenter: ControlPresenter)
    fun inject(recordingNavigation: RecordingNavigation)
    fun inject(importNotification: ImportNotification)
    fun inject(trackListNavigation: TrackListNavigation)
    fun inject(trackListViewAdapter: TrackListViewAdapter)
    fun inject(loggingReceiver: LoggingReceiver)
    fun inject(summaryCalculator: SummaryCalculator)
    fun inject(summaryManager: SummaryManager)
    fun inject(loggingService: LoggingService)
    fun inject(phoneMessageListenerService: PhoneMessageListenerService)
    fun inject(statisticsCollector: StatisticsCollector)
    fun inject(graphLabelsBindings: GraphLabelsBindings)
    fun inject(graphSpeedOverTimeProvider: GraphSpeedOverTimeDataProvider)
    fun inject(graphSpeedOVerDistanceDataProvider: GraphSpeedOVerDistanceDataProvider)
    fun inject(controlFragment: ControlFragment)
    fun inject(trackListActivity: TrackListActivity)
    fun inject(trackActivity: TrackActivity)
    fun trackDeletePresenter(): TrackDeletePresenter
    fun trackEditPresenter(): TrackEditPresenter
    fun graphsPresenter(): GraphsPresenter
    fun trackPresenter(): TrackPresenter
    fun trackMapPresenter(): TrackMapPresenter
    fun trackListPresenter(): TrackListPresenter
    fun recordingPresenter(): RecordingPresenter

}
