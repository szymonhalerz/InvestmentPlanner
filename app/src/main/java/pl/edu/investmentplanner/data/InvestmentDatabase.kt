package pl.edu.investmentplanner.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        InvestmentGoalEntity::class,
        PaymentEntity::class,
        FinancialCushionEntity::class,
        InvestmentPlanEntity::class,
        UserProfileEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class InvestmentDatabase : RoomDatabase() {
    abstract fun investmentDao(): InvestmentDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE investment_goals " +
                        "ADD COLUMN generatedByPlan INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE investment_goals " +
                        "ADD COLUMN horizonMonths INTEGER NOT NULL DEFAULT 12"
                )
                database.execSQL(
                    "ALTER TABLE investment_goals " +
                        "ADD COLUMN plannedMonthlyPayment REAL NOT NULL DEFAULT 0.0"
                )
                database.execSQL(
                    "UPDATE investment_goals " +
                        "SET plannedMonthlyPayment = targetAmount / 12.0 " +
                        "WHERE targetAmount > 0"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    UPDATE investment_goals
                    SET horizonMonths = (
                            SELECT horizonYears * 12 FROM investment_plan WHERE id = 1
                        ),
                        plannedMonthlyPayment = targetAmount / (
                            SELECT horizonYears * 12.0 FROM investment_plan WHERE id = 1
                        )
                    WHERE generatedByPlan = 1
                      AND EXISTS (
                          SELECT 1 FROM investment_plan
                          WHERE id = 1 AND horizonYears > 0
                      )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE investment_goals " +
                        "ADD COLUMN priority TEXT NOT NULL DEFAULT 'Średni'"
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_profile (
                        id INTEGER NOT NULL,
                        monthlyIncome REAL NOT NULL,
                        monthlyExpenses REAL NOT NULL,
                        monthlyInvestableAmount REAL NOT NULL,
                        livingSituation TEXT NOT NULL,
                        riskProfile TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE investment_goals " +
                        "ADD COLUMN goalType TEXT NOT NULL DEFAULT 'Inwestycyjny'"
                )
                database.execSQL(
                    """
                    UPDATE investment_goals
                    SET goalType = CASE
                        WHEN category = 'Poduszka finansowa' THEN 'Poduszka finansowa'
                        WHEN category IN ('Zakup samochodu', 'Zakup mieszkania', 'Inne')
                            THEN 'Zakupowy / dowolny'
                        ELSE 'Inwestycyjny'
                    END
                    """.trimIndent()
                )
            }
        }
    }
}
