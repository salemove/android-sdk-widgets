package com.glia.widgets.internal.callvisualizer.domain

import com.glia.androidsdk.GliaException
import com.glia.androidsdk.RequestCallback
import com.glia.androidsdk.omnibrowse.Omnibrowse
import com.glia.androidsdk.omnibrowse.VisitorCode
import com.glia.widgets.di.GliaCore
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class VisitorCodeRepositoryTest {

    private val gliaCore: GliaCore = mock()
    private val repository = VisitorCodeRepository(gliaCore)
    private val callVisualizer: Omnibrowse = mock()
    private val vc: VisitorCode = mock()
    private val CODE = "11121"

    @Test
    fun `getVisitorCode glia not initialized`() {
        whenever(gliaCore.isInitialized).thenReturn(false)
        val testObservable = repository.getVisitorCode().test()
        testObservable.assertError {
            it is GliaException && (it as GliaException).cause == GliaException.Cause.INVALID_INPUT
        }
    }

    @Test
    fun `getVisitorCode success`() {
        whenever(gliaCore.isInitialized).thenReturn(true)
        whenever(gliaCore.callVisualizer).thenReturn(callVisualizer)
        whenever(callVisualizer.getVisitorCode(any())).thenAnswer { invocation ->
            invocation?.getArgument<RequestCallback<VisitorCode>>(0)?.onResult(vc, null)
            vc
        }
        whenever(vc.code).thenReturn(CODE)
        val testObservable = repository.getVisitorCode().test()
        verify(callVisualizer).getVisitorCode(any())
        testObservable.assertValue(vc)
        testObservable.onComplete()
    }

    @Test
    fun `getVisitorCode failure visitor code is null`() {
        whenever(gliaCore.isInitialized).thenReturn(true)
        whenever(gliaCore.callVisualizer).thenReturn(callVisualizer)
        whenever(callVisualizer.getVisitorCode(any())).thenAnswer { invocation ->
            invocation?.getArgument<RequestCallback<VisitorCode>>(0)?.onResult(null, null)
            null
        }
        val testObservable = repository.getVisitorCode().test()
        verify(callVisualizer).getVisitorCode(any())
        testObservable.assertError {
            it is IllegalArgumentException
        }
        testObservable.onComplete()
    }

    @Test
    fun `getVisitorCode failure`() {
        val error = GliaException("test", GliaException.Cause.INTERNAL_ERROR)
        whenever(gliaCore.isInitialized).thenReturn(true)
        whenever(gliaCore.callVisualizer).thenReturn(callVisualizer)
        whenever(callVisualizer.getVisitorCode(any())).thenAnswer { invocation ->
            invocation?.getArgument<RequestCallback<VisitorCode>>(0)?.onResult(null, error)
            null
        }
        val testObservable = repository.getVisitorCode().test()
        verify(callVisualizer).getVisitorCode(any())
        testObservable.assertError(error)
        testObservable.onComplete()
    }
}
