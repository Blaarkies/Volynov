package app

import dI

class AppLogic : IGameLogic {

    override fun init() {
        dI.oldRenderer.init()
        dI.newRenderer.init()
        dI.textures.init()
        dI.models.init()
        dI.gamePhaseHandler.init()
    }

    override fun update(interval: Float) {
        dI.gamePhaseHandler.update()
    }

    override fun render() {
        dI.oldRenderer.clear()
        dI.newRenderer.clear()
        dI.gamePhaseHandler.render()
    }

    override fun cleanup() {
        dI.oldRenderer.dispose()
        dI.newRenderer.dispose()
        dI.inputHandler.dispose()
        dI.gamePhaseHandler.dispose()
    }
}
