package app

import display.graphic.Window

interface IGameLogic {

    @Throws(Exception::class)
    fun init()
    fun input(window: Window)
    fun update(interval: Float)
    fun render(window: Window)
    fun cleanup()

}
