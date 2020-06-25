package app

interface IGameLogic {

    @Throws(Exception::class)
    fun init()
    fun update(interval: Float)
    fun render()
    fun cleanup()

}
