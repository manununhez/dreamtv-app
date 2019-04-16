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
    LiveData<TaskEntity[]> getTasksByCategory(String category);


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

}
