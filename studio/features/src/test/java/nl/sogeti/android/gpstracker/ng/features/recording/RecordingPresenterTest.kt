package nl.sogeti.android.gpstracker.ng.features.recording

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import nl.renedegroot.android.test.utils.any
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.excellent
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.high
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.low
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.medium
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingViewModel.signalQualityLevel.none
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.util.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants
import nl.sogeti.android.gpstracker.service.integration.ServiceManager
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit

class RecordingPresenterTest {

    lateinit var sut: RecordingPresenter
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @Mock
    lateinit var uri: Uri
    @Mock
    lateinit var contentController: ContentController
    @Mock
    lateinit var gpsStatusController: GpsStatusController
    @Mock
    lateinit var serviceManager: ServiceManager
    @Mock
    lateinit var trackUri: Uri
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var gpsStatusControllerFactory: GpsStatusControllerFactory
    @Mock
    lateinit var navigation: RecordingNavigation
    @Mock
    lateinit var packageManager: PackageManager
    @Mock
    lateinit var statisticsFormatter: StatisticsFormatter
    @Mock
    lateinit var summaryManager: SummaryManager

    @Before
    fun setUp() {
        sut = RecordingPresenter(navigation, contentController, gpsStatusControllerFactory, packageManager, statisticsFormatter, summaryManager)
        sut.serviceManager = serviceManager
        `when`(gpsStatusControllerFactory.createGpsStatusController(any(), any())).thenReturn(gpsStatusController)
    }

    @Test
    fun testStop() {
        // Arrange
        sut.start(context)
        sut.didConnectToService(context, trackUri, "mockTrack", ServiceConstants.STATE_LOGGING)
        // Act
        sut.willStop()
        // Assert
        verify(contentController).unregisterObserver()
        verify(gpsStatusController).stopUpdates()
    }

    @Test
    fun testConnectToLoggingService() {
        // Arrange
        sut.start(context)
        // Act
        sut.didConnectToService(context, uri, "mockTrack", ServiceConstants.STATE_LOGGING)
        // Assert
        Assert.assertThat(sut.viewModel.isRecording.get(), `is`(true))
        Assert.assertThat(sut.viewModel.name.get(), `is`("mockTrack"))
        Assert.assertThat(sut.viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testConnectToPauseService() {
        // Arrange
        sut.start(context)
        // Act
        sut.didConnectToService(context, uri, "paused", ServiceConstants.STATE_PAUSED)
        // Assert
        Assert.assertThat(sut.viewModel.isRecording.get(), `is`(true))
        Assert.assertThat(sut.viewModel.name.get(), `is`("paused"))
        Assert.assertThat(sut.viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testConnectToStoppedService() {
        // Arrange
        sut.start(context)
        // Act
        sut.didConnectToService(context, uri, "stopped", ServiceConstants.STATE_STOPPED)
        // Assert
        Assert.assertThat(sut.viewModel.isRecording.get(), `is`(false))
        Assert.assertThat(sut.viewModel.name.get(), `is`("stopped"))
        Assert.assertThat(sut.viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testChangeToLoggingService() {
        // Arrange
        sut.start(context)
        // Act
        sut.didChangeLoggingState(context, uri, "mockTrack", ServiceConstants.STATE_STOPPED)
        // Assert
        Assert.assertThat(sut.viewModel.isRecording.get(), `is`(false))
        Assert.assertThat(sut.viewModel.name.get(), `is`("mockTrack"))
        Assert.assertThat(sut.viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testGpsStart() {
        // Act
        sut.onStart()
        // Assert
        assertThat(sut.viewModel.isScanning.get(), `is`(true))
        assertThat(sut.viewModel.hasFix.get(), `is`(false))
    }

    @Test
    fun testGpsStop() {
        // Act
        sut.onStop()
        // Assert
        assertThat(sut.viewModel.isScanning.get(), `is`(false))
        assertThat(sut.viewModel.hasFix.get(), `is`(false))
        assertThat(sut.viewModel.maxSatellites.get(), `is`(0))
        assertThat(sut.viewModel.currentSatellites.get(), `is`(0))
    }

    @Test
    fun onFirstFix() {
        // Act
        sut.onFirstFix()
        // Assert
        assertThat(sut.viewModel.hasFix.get(), `is`(true))
        assertThat(sut.viewModel.signalQuality.get(), `is`(4))
    }

    @Test
    fun onNoSignal() {
        // Act
        sut.onChange(0, 0)
        // Assert
        assertThat(sut.viewModel.maxSatellites.get(), `is`(0))
        assertThat(sut.viewModel.currentSatellites.get(), `is`(0))
        assertThat(sut.viewModel.signalQuality.get(), `is`(none))
    }

    @Test
    fun onLowSignal() {
        // Act
        sut.onChange(4, 4)
        // Assert
        assertThat(sut.viewModel.maxSatellites.get(), `is`(4))
        assertThat(sut.viewModel.currentSatellites.get(), `is`(4))
        assertThat(sut.viewModel.signalQuality.get(), `is`(low))
    }

    @Test
    fun onMediumSignal() {
        // Act
        sut.onChange(6, 20)
        // Assert
        assertThat(sut.viewModel.maxSatellites.get(), `is`(20))
        assertThat(sut.viewModel.currentSatellites.get(), `is`(6))
        assertThat(sut.viewModel.signalQuality.get(), `is`(medium))
    }

    @Test
    fun onHighSignal() {
        // Act
        sut.onChange(8, 20)
        // Assert
        assertThat(sut.viewModel.signalQuality.get(), `is`(high))
    }

    @Test
    fun onExcellentSignal() {
        // Act
        sut.onChange(10, 20)
        // Assert
        assertThat(sut.viewModel.signalQuality.get(), `is`(excellent))
    }
}
