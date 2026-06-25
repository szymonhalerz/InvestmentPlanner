package pl.edu.investmentplanner.ui

import androidx.compose.ui.graphics.Color

data class InvestmentCategory(
    val name: String,
    val icon: String,
    val color: Color
)

val investmentCategories = listOf(
    InvestmentCategory("ETF-y", "📊", Color(0xFF4F7CAC)),
    InvestmentCategory("Akcje", "📈", Color(0xFF6C63A8)),
    InvestmentCategory("Obligacje skarbowe", "🏛️", Color(0xFF467A6B)),
    InvestmentCategory("Metale szlachetne", "🪙", Color(0xFFB8860B)),
    InvestmentCategory("Kryptowaluty", "₿", Color(0xFFDA7B27)),
    InvestmentCategory("Gotówka / konto oszczędnościowe", "💰", Color(0xFF388E7D)),
    InvestmentCategory("Wkład własny na mieszkanie", "🔑", Color(0xFF8A6A4A)),
    InvestmentCategory("Zakup samochodu", "🚗", Color(0xFF546E7A)),
    InvestmentCategory("Zakup mieszkania", "🏠", Color(0xFF9A6B4F)),
    InvestmentCategory("Remont", "🔨", Color(0xFFA1684C)),
    InvestmentCategory("Wakacje", "✈️", Color(0xFF3F7F93)),
    InvestmentCategory("Sprzęt", "💻", Color(0xFF596A8A)),
    InvestmentCategory("Kurs / szkolenie", "🎓", Color(0xFF7166A3)),
    InvestmentCategory("Poduszka finansowa", "🛟", Color(0xFF2F7D69)),
    InvestmentCategory("Inne", "✨", Color(0xFF7E6B8F))
)

fun categoryDetails(name: String): InvestmentCategory =
    investmentCategories.firstOrNull { it.name == name }
        ?: InvestmentCategory(name, "✨", Color(0xFF7E6B8F))
