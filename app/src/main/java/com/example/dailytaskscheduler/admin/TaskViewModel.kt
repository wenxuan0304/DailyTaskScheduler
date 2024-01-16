package com.example.dailytaskscheduler.admin
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dailytaskscheduler.util.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(application: Application): AndroidViewModel(application) {
    private val taskList = MutableLiveData<List<Task>>()
    val allTasks : LiveData<List<Task>> = taskList

    init {

        allTasks.observeForever{
            Log.d("TaskViewModel", "All tasks changed: $it")
        }

    }
}

class TaskViewModelFactory(private val application: Application): ViewModelProvider.AndroidViewModelFactory(application){
    override fun <T: ViewModel> create(modeClass: Class<T>): T{
        if(modeClass.isAssignableFrom(TaskViewModel::class.java)){
            return TaskViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}