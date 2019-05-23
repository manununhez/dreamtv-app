package com.dream.dreamtv.db.dao;

import com.dream.dreamtv.db.entity.TaskEntity;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao // Required annotation for Dao to be recognized by Room
public interface TaskDao {
    /**
     * task entry by id
     *
     * @return {@link TaskEntity} task entry
     */
    @Query("SELECT * FROM task_table WHERE category = :category")
    LiveData<TaskEntity[]> requestTaskByCategory(String category);


    /**
     * Insert a single {@link TaskEntity} into the database
     *
     * @param TaskEntity
     */
    @Insert
    void insert(TaskEntity TaskEntity);


    /**
     * Inserts a list of {@link TaskEntity} into the task table. If there is a conflicting id
     * or date the task entry uses the {@link OnConflictStrategy} of replacing the task
     * forecast. The required uniqueness of these values is defined in the {@link TaskEntity}.
     *
     * @param taskEntities A list of task forecasts to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(TaskEntity... taskEntities);
//
//
//    /**
//     * Deletes any task data older than some date
//     *
//     * @param date The date to delete all prior task from (exclusive)
//     */
//    @Query("DELETE FROM task_table WHERE dt < :date")
//    void deleteOldtask(long date);
//
    /**
     * Delete a single {@link TaskEntity} from the database
     */
    @Delete
    void delete(TaskEntity TaskEntity);


    @Query("DELETE from task_table")
    void deleteAll();

    /**
     * Last update. We get the oldest dt, that's why we order dt by ASC. This is key to know if new
     * data should be fetched from network.
     *
     * @return {@link long} the oldest datetime entry in the database
     */
    @Query("SELECT sync_time FROM task_table ORDER by sync_time ASC LIMIT 1")
    long getLastUpdateTime();


    @Query("SELECT * FROM task_table WHERE task_id = :task_id AND category = :category LIMIT 1")
    LiveData<TaskEntity> verifyTask(int task_id, String category);

    @Query("DELETE from task_table WHERE task_id=:task_id AND category=:category")
    void deleteByTaskIdAndCategory(int task_id, String category);
}
