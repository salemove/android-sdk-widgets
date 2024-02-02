package com.glia.widgets.helper

import android.app.Activity
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.filepreview.ui.FilePreviewActivity
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class GliaActivityManagerTest {

    @MockK(relaxUnitFun = true)
    private lateinit var chatActivity: ChatActivity

    @MockK(relaxUnitFun = true)
    private lateinit var callActivity: CallActivity

    @MockK(relaxUnitFun = true)
    private lateinit var filePreviewActivity: FilePreviewActivity

    @MockK(relaxUnitFun = true)
    private lateinit var anotherActivity: Activity

    private lateinit var managerTest: GliaActivityManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        managerTest = GliaActivityManagerImpl()
    }

    @Test
    fun `onActivityCreated() adds activity to the list`() {
        managerTest.onActivityCreated(chatActivity)
        managerTest.onActivityCreated(callActivity)
        managerTest.finishActivities()

        verify { chatActivity.finish() }
        verify { callActivity.finish() }

        confirmVerified(chatActivity, callActivity)
    }

    @Test
    fun `onActivityCreated() does not add activity to the list when it is not glia activity`() {
        managerTest.onActivityCreated(anotherActivity)
        managerTest.finishActivities()

        verify(exactly = 0) { anotherActivity.finish() }

        confirmVerified(anotherActivity)
    }

    @Test
    fun `onActivityDestroyed() removes activity from the list`() {
        managerTest.onActivityCreated(chatActivity)
        managerTest.onActivityCreated(callActivity)
        managerTest.onActivityCreated(filePreviewActivity)

        managerTest.onActivityDestroyed(filePreviewActivity)

        managerTest.finishActivities()

        verify { chatActivity.finish() }
        verify { callActivity.finish() }
        verify(exactly = 0) { filePreviewActivity.finish() }

        confirmVerified(chatActivity, callActivity, filePreviewActivity)
    }

    @Test
    fun `finishActivity finishes activity when it is present in list`() {
        managerTest.onActivityCreated(chatActivity)
        managerTest.onActivityCreated(callActivity)
        managerTest.onActivityCreated(filePreviewActivity)
        managerTest.onActivityCreated(anotherActivity)

        managerTest.finishActivity(FilePreviewActivity::class)

        verify(exactly = 0) { chatActivity.finish() }
        verify(exactly = 0) { callActivity.finish() }
        verify(exactly = 0) { anotherActivity.finish() }
        verify { filePreviewActivity.finish() }

        confirmVerified(chatActivity, callActivity, filePreviewActivity, anotherActivity)
    }

}
