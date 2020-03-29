package game

class PlayerAim(var angle: Float = 0f, power: Float = 100f) {

    var power = power
        set(value) {
            field = value.coerceIn(0f, 100f)
        }

}
