package com.bridou_n.beaconscanner.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bridou_n.beaconscanner.models.BeaconSaved

@Database(
        entities = [
            BeaconSaved::class
        ],
        version = 1,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun beaconsDao() : BeaconsDao
}