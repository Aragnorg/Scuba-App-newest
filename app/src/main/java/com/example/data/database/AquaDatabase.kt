package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.DivingDao
import com.example.data.model.CourseModuleEntity
import com.example.data.model.DiverDocumentEntity
import com.example.data.model.DiveLogEntity
import com.example.data.model.LearningVideoEntity
import com.example.data.model.PresentationDeckEntity
import com.example.data.model.QuizEntity
import com.example.data.model.StudentRosterEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProgressEntity

import com.example.data.model.OrganizationEntity
import com.example.data.model.OrganizationMemberEntity
import com.example.data.model.DiveEventEntity
import com.example.data.model.DiveEventParticipantEntity

@Database(
    entities = [
        CourseModuleEntity::class,
        PresentationDeckEntity::class,
        LearningVideoEntity::class,
        QuizEntity::class,
        UserProgressEntity::class,
        UserAccountEntity::class,
        StudentRosterEntity::class,
        DiveLogEntity::class,
        DiverDocumentEntity::class,
        OrganizationEntity::class,
        OrganizationMemberEntity::class,
        DiveEventEntity::class,
        DiveEventParticipantEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AquaDatabase : RoomDatabase() {
    abstract fun divingDao(): DivingDao

    companion object {
        @Volatile
        private var INSTANCE: AquaDatabase? = null

        fun getDatabase(context: Context): AquaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AquaDatabase::class.java,
                    "aqua_learn_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
