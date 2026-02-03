package com.glia.widgets.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.glia.widgets.survey.SurveyViewModel

/**
 * Factory for creating ViewModels with dependencies from the SDK's DI system.
 */
internal class ViewModelFactory(
    private val useCaseFactory: UseCaseFactory,
    private val repositoryFactory: RepositoryFactory
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            SurveyViewModel::class.java -> SurveyViewModel(
                useCaseFactory.surveyAnswerUseCase
            ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}