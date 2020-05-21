package utility

import org.jbox2d.common.Vec2

class PidControllerVec2(private val kp: Float = .3f,
                        private val ki: Float = .001f,
                        private val kd: Float = 2f) {

    private var proportional = Vec2()
    private var integral = Vec2()

    fun getReaction(sensor: Vec2, target: Vec2): Vec2 {
        val lastProportional = proportional.clone()

        proportional = sensor.sub(target)
        integral.addLocal(proportional)
        val derivative = proportional.sub(lastProportional)


        return proportional.mul(kp).add(integral.mul(ki)).add(derivative.mul(kd))
    }

    fun reset() {
        proportional.setZero()
        integral.setZero()
    }

}
