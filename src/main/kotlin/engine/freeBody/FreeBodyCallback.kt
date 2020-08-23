package engine.freeBody

import org.jbox2d.dynamics.Body

class FreeBodyCallback(val freeBody: FreeBody,
                       val callback: (self: FreeBody, impacted: Body?) -> Unit,
                       var isHandled: Boolean = false)
