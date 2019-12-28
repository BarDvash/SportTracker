package android.technion.fitracker.models.exercise

data class ExerciseLogModel(
    val name: String? = null,
    var time_done: String? = null
)

open class ExerciseBaseModel(
    val type: String? = null,
    var time_done: String? = null,
    var done: Boolean = false
) {
    fun downcastToWeight(): WeightExerciseModel? {
        if (type ?: type == "Weight") {
            return this as WeightExerciseModel
        }
        return null
    }

    fun downcastToAerobic(): AerobicExerciseModel? {
        if (type ?: type == "Aerobic") {
            return this as AerobicExerciseModel
        }
        return null
    }

    fun extractLogModel(): ExerciseLogModel {
        return if (type ?: type == "Aerobic") {
            ExerciseLogModel((this as AerobicExerciseModel).name, this.time_done)
        } else {
            ExerciseLogModel((this as WeightExerciseModel).name, this.time_done)
        }
    }
}