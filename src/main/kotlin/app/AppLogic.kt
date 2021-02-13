package app

import dI

class AppLogic : IGameLogic {

    override fun init() {
//        dI.renderer.init()
        dI.newRenderer.init()
        dI.textures.init()
        dI.models.init()
        dI.gamePhaseHandler.init()
    }

    override fun update(interval: Float) {
        dI.gamePhaseHandler.update()
    }

    override fun render() {
//        dI.renderer.clear()
        dI.newRenderer.clear()
        dI.gamePhaseHandler.render()
    }

    override fun cleanup() {
//        dI.renderer.dispose()
        dI.newRenderer.dispose()
        dI.inputHandler.dispose()
        dI.gamePhaseHandler.dispose()
    }
}
