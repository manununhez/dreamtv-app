package com.dream.dreamtv.db.dao;

import com.dream.dreamtv.db.entity.UserEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Created by manuel on 21,August,2018
 */
@Dao // Required annotation for Dao to be recognized by Room
public interface UserDao {

//    /**
//     * List of the IDs of the weather entries (without pulling all of the data.)
//     *
//     * @return {@link Integer} list of ID of all weather entries
//     */
//    @Query("SELECT id FROM user_table")
//    List<Integer> getAllWeathersId();

    /**
     * Weather entry by id
     *
     * @return {@link UserEntity} weather entry
     */
    @Query("SELECT * FROM user_table WHERE token = :token")
    LiveData<UserEntity> getUserByToken(String token);

//    /**
//     * Last update. We get the oldest dt, that's why we order dt by ASC. This is key to know if new
//     * data should be fetched from network.
//     *
//     * @return {@link long} the oldest datetime entry in the database
//     */
//    @Query("SELECT dt FROM user_table ORDER by dt ASC LIMIT 1")
//    long getLastUpdateTime();
//
//    /**
//     * Selects all ids entries. This is for easily seeing
//     * what entries are in the database without pulling all of the data.
//     */
//    @Query("SELECT count(*) FROM user_table")
//    int getCountCurrentWeathers();

    /**
     * Insert a single {@link UserEntity} into the database
     *
     * @param userEntity
     */
    @Insert
    void insert(UserEntity userEntity);

//
//    /**
//     * Selects all {@link UserEntity} entries order by name. The LiveData will
//     * be kept in sync with the database, so that it will automatically notify observers when the
//     * values in the table change.
//     *
//     * @return {@link LiveData} list of all {@link UserEntity} objects order by name
//     */
//    @Query("SELECT * FROM user_table ORDER by name ASC ")
//    LiveData<List<UserEntity>> getCurrentWeather();

//    /**
//     * Inserts a list of {@link UserEntity} into the weather table. If there is a conflicting id
//     * or date the weather entry uses the {@link OnConflictStrategy} of replacing the weather
//     * forecast. The required uniqueness of these values is defined in the {@link UserEntity}.
//     *
//     * @param weather A list of weather forecasts to insert
//     */
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void bulkInsert(UserEntity... weather);
//
//
//    /**
//     * Deletes any weather data older than some date
//     *
//     * @param date The date to delete all prior weather from (exclusive)
//     */
//    @Query("DELETE FROM user_table WHERE dt < :date")
//    void deleteOldWeather(long date);
//
//    /**
//     * Delete a single {@link UserEntity} from the database
//     */
//    @Delete
//    void delete(UserEntity weatherEntity);


}
