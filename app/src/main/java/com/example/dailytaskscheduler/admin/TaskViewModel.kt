package com.example.dailytaskscheduler.admin
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dailytaskscheduler.util.Task
import com.example.dailytaskscheduler.util.TaskDatabase
import com.example.dailytaskscheduler.util.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(application: Application): AndroidViewModel(application) {
    private val repository: TaskRepository
    val allTasks : LiveData<List<Task>>

    init {
        val dao = TaskDatabase.getDatabase(application).taskDao
        repository = TaskRepository(dao)
        allTasks = repository.allTasks

        allTasks.observeForever{
            Log.d("TaskViewModel", "All tasks changed: $it")
        }
    }

    fun insertTask(task: Task) = viewModelScope.launch(Dispatchers.IO){
        repository.insert(task)
    }

    fun updateTodo(task: Task) = viewModelScope.launch(Dispatchers.IO){
        repository.update(task)
    }

    fun deleteTodo(task: Task) = viewModelScope.launch(Dispatchers.IO){
        repository.delete(task)
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