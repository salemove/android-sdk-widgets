package com.glia.widgets.dialog

import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.R
import com.glia.widgets.SnapshotTest
import com.glia.widgets.StringProvider
import com.glia.widgets.UiTheme
import com.glia.widgets.core.configuration.GliaSdkConfigurationManager
import com.glia.widgets.di.ControllerFactory
import com.glia.widgets.di.Dependencies
import com.glia.widgets.helper.ResourceProvider
import com.glia.widgets.snapshotutils.SnapshotStringProvider
import com.glia.widgets.view.VisitorCodeView
import com.glia.widgets.view.unifiedui.theme.UnifiedTheme
import com.google.gson.JsonObject
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

class VisitorCodeDialogTest : SnapshotTest(
    renderingMode = fullWidthRenderMode
) {

    override fun tearDown() {
        super.tearDown()

        Dependencies.getGliaThemeManager().theme = null
    }

    // MARK: Show visitor code

    @Test
    fun visitorCode() {
        snapshot(
            setupView().apply {
                startLoading()
                showVisitorCode(code())
            },
            offsetMillis = 200
        )
    }

    @Test
    fun visitorCodeWithUiTheme() {
        snapshot(
            setupView(
                uiTheme = uiTheme()
            ).apply {
                startLoading()
                showVisitorCode(code())
            },
            offsetMillis = 200
        )
    }

    @Test
    fun visitorCodeWithGlobalColors() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).apply {
                startLoading()
                showVisitorCode(code())
            },
            offsetMillis = 200
        )
    }

    @Test
    fun visitorCodeWithUnifiedTheme() {
        snapshot(
            setupView(
                unifiedTheme = unifiedTheme()
            ).apply {
                startLoading()
                showVisitorCode(code())
            },
            offsetMillis = 200
        )
    }

    @Test
    fun visitorCodeWithUnifiedThemeWithoutVisitorCode() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithoutVisitorCode()
            ).apply {
                startLoading()
                showVisitorCode(code())
            },
            offsetMillis = 200
        )
    }

    // MARK: Error

    @Test
    fun error() {
        snapshot(
            setupView().apply {
                setClosable(true)
                startLoading()
                showError(Throwable())
            },
            offsetMillis = 200
        )
    }

    @Test
    fun errorWithUiTheme() {
        snapshot(
            setupView(
                uiTheme = uiTheme()
            ).apply {
                setClosable(true)
                startLoading()
                showError(Throwable())
            },
            offsetMillis = 200
        )
    }

    @Test
    fun errorWithGlobalColors() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).apply {
                setClosable(true)
                startLoading()
                showError(Throwable())
            },
            offsetMillis = 200
        )
    }

    @Test
    fun errorWithUnifiedTheme() {
        snapshot(
            setupView(
                unifiedTheme = unifiedTheme()
            ).apply {
                setClosable(true)
                startLoading()
                showError(Throwable())
            },
            offsetMillis = 200
        )
    }

    @Test
    fun errorWithUnifiedThemeWithoutVisitorCode() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithoutVisitorCode()
            ).apply {
                setClosable(true)
                startLoading()
                showError(Throwable())
            },
            offsetMillis = 200
        )
    }

    // MARK: Loading

    @Test
    fun loading() {
        snapshot(
            setupView().apply {
                setClosable(true)
                startLoading()
            }
        )
    }

    @Test
    fun loadingWithUiTheme() {
        snapshot(
            setupView(
                uiTheme = uiTheme()
            ).apply {
                setClosable(true)
                startLoading()
            }
        )
    }

    @Test
    fun loadingWithGlobalColors() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithGlobalColors()
            ).apply {
                setClosable(true)
                startLoading()
            }
        )
    }

    @Test
    fun loadingWithUnifiedTheme() {
        snapshot(
            setupView(
                unifiedTheme = unifiedTheme()
            ).apply {
                setClosable(true)
                startLoading()
            }
        )
    }

    @Test
    fun loadingWithUnifiedThemeWithoutVisitorCode() {
        snapshot(
            setupView(
                unifiedTheme = unifiedThemeWithoutVisitorCode()
            ).apply {
                setClosable(true)
                startLoading()
            }
        )
    }

    private fun setupView(
        uiTheme: UiTheme = UiTheme(),
        unifiedTheme: UnifiedTheme? = null,
        executor: Executor? = Executor(Runnable::run)
    ): VisitorCodeView {
        unifiedTheme?.let { Dependencies.getGliaThemeManager().theme = it }
        val configurationManager = GliaSdkConfigurationManager().also {
            it.uiTheme = uiTheme
        }
        val controllerFactoryMock: ControllerFactory = mock<ControllerFactory>().also {
            whenever(it.visitorCodeController).thenReturn(mock())
        }
        val rp = ResourceProvider(context)
        val sp: StringProvider = SnapshotStringProvider(context)
        Dependencies.setSdkConfigurationManager(configurationManager)
        Dependencies.setControllerFactory(controllerFactoryMock)
        Dependencies.setResourceProvider(rp)
        Dependencies.setStringProvider(sp)

        return VisitorCodeView(context, executor).apply {
            notifySetupComplete()
        }
    }

    private fun code(
        code: String = "12345",
        duration: Long = 10000
    ): VisitorCode = mock<VisitorCode>().also {
        whenever(it.code).thenReturn(code)
        whenever(it.duration).thenReturn(duration)
    }

    private fun unifiedThemeWithoutVisitorCode(): UnifiedTheme = unifiedTheme(R.raw.test_unified_config) { unifiedTheme ->
        unifiedTheme.add(
            "callVisualizer",
            (unifiedTheme.remove("callVisualizer") as JsonObject).also {
                it.remove("visitorCode")
            }
        )
    }
}
