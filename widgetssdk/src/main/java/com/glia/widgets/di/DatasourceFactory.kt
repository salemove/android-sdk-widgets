package com.glia.widgets.di

import com.glia.widgets.engagement.EngagementDataSource
import com.glia.widgets.engagement.EngagementDataSourceImpl
import com.glia.widgets.engagement.SurveyDataSource
import com.glia.widgets.engagement.SurveyDataSourceImpl
import com.glia.widgets.engagement.end.EngagementEndReasonDataSource
import com.glia.widgets.engagement.end.EngagementEndReasonDataSourceImpl

internal object DatasourceFactory {
    private val gliaCore: GliaCore get() = Dependencies.glia()

    @JvmStatic
    val engagementDataSource: EngagementDataSource get() = EngagementDataSourceImpl(gliaCore)

    @JvmStatic
    val surveyDataSource: SurveyDataSource get() = SurveyDataSourceImpl()

    //Singletons
    @JvmStatic
    val engagementEndReasonDataSource: EngagementEndReasonDataSource by lazy {
        EngagementEndReasonDataSourceImpl()
    }
}
