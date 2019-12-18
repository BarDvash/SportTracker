package android.technion.fitracker.models.nutrition

data class NutritionFireStoreModel(val meals: List<Map<String,String>>? = null, var name: String? = null)