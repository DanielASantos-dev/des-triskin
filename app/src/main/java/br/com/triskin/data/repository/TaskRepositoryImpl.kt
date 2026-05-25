package br.com.triskin.data.repository

import br.com.triskin.data.local.dao.TaskDao
import br.com.triskin.data.mapper.toDomain
import br.com.triskin.data.mapper.toEntity
import br.com.triskin.domain.model.Task
import br.com.triskin.domain.model.TaskPriority
import br.com.triskin.domain.model.TaskStatus
import br.com.triskin.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> =
        taskDao.getAllTasks().mapToDomain()

    override fun getTasksDueOn(date: LocalDate): Flow<List<Task>> =
        getTasksDueBetween(date, date.plusDays(1))

    override fun getTasksDueBetween(from: LocalDate, to: LocalDate): Flow<List<Task>> =
        taskDao.getTasksDueBetween(
            from = from.atStartOfDay().format(isoFormatter),
            to = to.atStartOfDay().format(isoFormatter),
        ).mapToDomain()

    override fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> =
        taskDao.getTasksByStatus(status).mapToDomain()

    override fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>> =
        taskDao.getTasksByPriority(priority).mapToDomain()

    override fun getTaskById(id: String): Flow<Task?> =
        taskDao.getTaskById(id).map { it?.toDomain() }

    override suspend fun createTask(task: Task) = taskDao.insertTask(task.toEntity())

    override suspend fun updateTask(task: Task) = taskDao.updateTask(task.toEntity())

    override suspend fun deleteTask(id: String) = taskDao.deleteTask(id)

    private fun Flow<List<br.com.triskin.data.local.entity.TaskEntity>>.mapToDomain() =
        map { list -> list.map { it.toDomain() } }

    private companion object {
        val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }
}
