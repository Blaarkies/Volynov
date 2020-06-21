package app

import dI

class AppLogic : IGameLogic {

    @Throws(Exception::class)
    override fun init() {
        dI.renderer.init()
        dI.textures.init()
        dI.gamePhaseHandler.init()
    }

    override fun update(interval: Float) {
        dI.gamePhaseHandler.update()
    }

    override fun render() {
        dI.renderer.clear()
        dI.gamePhaseHandler.render()
    }

    override fun cleanup() {
        dI.renderer.dispose()
        dI.inputHandler.dispose()
    }
}
