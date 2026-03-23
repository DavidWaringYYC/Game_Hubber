package com.squirrelreserve.gamehubber.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        GameProgressEntity::class,
        WalletEntity::class,
        TokenTxnEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase(){
    abstract fun gameProgressDao(): GameProgressDao
    abstract fun walletDao(): WalletDao
    abstract fun tokenTxnDao(): TokenTxnDao
    companion object{
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gamehubber.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }



}