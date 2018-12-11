package com.bridou_n.beaconscanner.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.bridou_n.beaconscanner.models.BeaconSaved
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface BeaconsDao {

    companion object {
        const val TABLE_NAME = "beacons"
    }

    @Query("SELECT * FROM $TABLE_NAME WHERE is_blocked = :blocked")
    fun getBeacons(blocked: Boolean = false) : Flowable<List<BeaconSaved>>

    @Query("SELECT * FROM $TABLE_NAME WHERE hashcode = :hashcode")
    fun getBeaconById(hashcode: Int) : Single<BeaconSaved>

    @Query("SELECT * FROM $TABLE_NAME where is_blocked = :blocked AND last_seen >= :lastSeen")
    fun getBeaconsSeenAfter(lastSeen: Long, blocked: Boolean = false) : Single<List<BeaconSaved>>

    @Insert(onConflict = REPLACE)
    fun insertBeacon(beacon: BeaconSaved)

    @Delete
    fun deleteBeacon(beacon: BeaconSaved)

    @Query("DELETE FROM $TABLE_NAME WHERE hashcode = :hashcode")
    fun deleteBeaconById(hashcode: Int)

    @Query("DELETE FROM $TABLE_NAME")
    fun clearBeacons()
}