package com.androiddevs.runningappyt.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningappyt.db.Run
import com.androiddevs.runningappyt.other.SortType
import com.androiddevs.runningappyt.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    fun insertRun(run: Run) {
        viewModelScope.launch {
            mainRepository.insertRun(run)
        }
    }

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistanceInMeter()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByTimeMillis = mainRepository.getAllRunsSortedByTimeMillis()
    private val runsSortedByTimeAvgSpeedInKM = mainRepository.getAllRunsSortedByTimeAvgSpeedInKM()

    val run = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        run.addSource(runsSortedByDate) {
            if (sortType == SortType.DATE) {
                run.value = it
            }
        }

        run.addSource(runsSortedByDistance) {
            if (sortType == SortType.DISTANCE) {
                run.value = it
            }
        }

        run.addSource(runsSortedByCaloriesBurned) {
            if (sortType == SortType.CALORIES_BURNED) {
                run.value = it
            }
        }

        run.addSource(runsSortedByTimeAvgSpeedInKM) {
            if (sortType == SortType.AVG_SPEED) {
                run.value = it
            }
        }
        run.addSource(runsSortedByTimeMillis) {
            if (sortType == SortType.RUNNING_TIME) {
                run.value = it
            }
        }
    }

    fun sortRuns(sortType: SortType) {
        when (sortType) {
            SortType.DATE -> {
                run.value = runsSortedByDate.value
            }
            SortType.RUNNING_TIME -> {
                run.value = runsSortedByTimeMillis.value
            }
            SortType.DISTANCE -> {
                run.value = runsSortedByDistance.value
            }
            SortType.AVG_SPEED -> {
                run.value = runsSortedByTimeAvgSpeedInKM.value
            }
            SortType.CALORIES_BURNED -> {
                run.value = runsSortedByCaloriesBurned.value
            }
        }
    }
}