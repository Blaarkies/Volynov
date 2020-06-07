package engine.freeBody

import org.jbox2d.dynamics.Body

class FreeBodyCallback(val freeBody: FreeBody,
                       val callback: (FreeBody, Body) -> Unit,
                       var isHandled: Boolean = false)
