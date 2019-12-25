package android.technion.fitracker.models.nutrition

data class NutritionFireStoreModel(val meals: ArrayList<HashMap<String,String>>? = null, var name: String? = null)