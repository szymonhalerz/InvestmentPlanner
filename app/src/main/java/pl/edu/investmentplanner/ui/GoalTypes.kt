package pl.edu.investmentplanner.ui

const val GOAL_TYPE_INVESTMENT = "Inwestycyjny"
const val GOAL_TYPE_CUSHION = "Poduszka finansowa"
const val GOAL_TYPE_CUSTOM = "Zakupowy / dowolny"

val goalTypes = listOf(GOAL_TYPE_INVESTMENT, GOAL_TYPE_CUSHION, GOAL_TYPE_CUSTOM)

fun categoriesForGoalType(type: String): List<InvestmentCategory> = when (type) {
    GOAL_TYPE_CUSHION -> investmentCategories.filter { it.name == "Poduszka finansowa" }
    GOAL_TYPE_CUSTOM -> investmentCategories.filter {
        it.name in listOf(
            "Wkład własny na mieszkanie",
            "Zakup mieszkania",
            "Zakup samochodu",
            "Remont",
            "Wakacje",
            "Sprzęt",
            "Kurs / szkolenie",
            "Inne"
        )
    }
    else -> investmentCategories.filter {
        it.name in listOf(
            "ETF-y",
            "Akcje",
            "Obligacje skarbowe",
            "Metale szlachetne",
            "Kryptowaluty",
            "Gotówka / konto oszczędnościowe"
        )
    }
}
