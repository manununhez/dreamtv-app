package com.dream.dreamtv.db.dao;

import com.dream.dreamtv.db.entity.UserEntity;

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

    /**
     * user entry by id
     *
     * @return {@link UserEntity} user entry
     */
    @Query("SELECT * FROM user_table WHERE email = :email and password = :password")
    LiveData<UserEntity> getUser(String email, String password);


    /**
     * Insert a single {@link UserEntity} into the database
     *
     * @param userEntity
     */
    @Insert
    void insert(UserEntity userEntity);


    /**
     * Inserts a list of {@link UserEntity} into the user table. If there is a conflicting id
     * or date the user entry uses the {@link OnConflictStrategy} of replacing the user
     * forecast. The required uniqueness of these values is defined in the {@link UserEntity}.
     *
     * @param userEntities A list of users to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(UserEntity... userEntities);
//
//
//    /**
//     * Deletes any user data older than some date
//     *
//     * @param date The date to delete all prior user from (exclusive)
//     */
//    @Query("DELETE FROM user_table WHERE dt < :date")
//    void deleteOlduser(long date);
//
    /**
     * Delete a single {@link UserEntity} from the database
     */
    @Delete
    void delete(UserEntity userEntity);


    @Query("DELETE from user_table")
    void deleteAll();


}
