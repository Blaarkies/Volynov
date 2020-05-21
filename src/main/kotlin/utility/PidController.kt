package utility

class PidController(private val kp: Float = .3f,
                    private val ki: Float = .001f,
                    private val kd: Float = 2f) {

    private var proportional = 0f
    private var integral = 0f
    private var derivative = 0f

    fun getReaction(sensor: Float, target: Float): Float {
        val lastProportional = proportional

        proportional = sensor - target
        integral += proportional
        derivative = proportional - lastProportional


        return proportional * kp + integral * ki + derivative * kd
    }

}
