package android.technion.fitracker.models.exercise

open class ExerciseBaseModel(
    val type: String? = null
)  {
    fun downcastToWeight(): WeightExerciseModel?{
        if(type ?: type == "Weight"){
            return this as WeightExerciseModel
        }
        return null
    }
    fun downcastToAerobic(): AerobicExerciseModel?{
        if(type ?: type == "Aerobic"){
            return this as AerobicExerciseModel
        }
        return null
    }
}