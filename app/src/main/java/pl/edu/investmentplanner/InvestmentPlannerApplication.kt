package pl.edu.investmentplanner

import android.app.Application
import androidx.room.Room
import pl.edu.investmentplanner.data.InvestmentDatabase
import pl.edu.investmentplanner.data.InvestmentRepository

class InvestmentPlannerApplication : Application() {
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            InvestmentDatabase::class.java,
            "investment_planner.db"
        )
            .addMigrations(
                InvestmentDatabase.MIGRATION_2_3,
                InvestmentDatabase.MIGRATION_3_4,
                InvestmentDatabase.MIGRATION_4_5,
                InvestmentDatabase.MIGRATION_5_6,
                InvestmentDatabase.MIGRATION_6_7
            )
            // W projekcie studenckim upraszcza zmianę schematu między wersjami aplikacji.
            .fallbackToDestructiveMigration(true)
            .build()
    }

    val repository by lazy { InvestmentRepository(database.investmentDao()) }
}
