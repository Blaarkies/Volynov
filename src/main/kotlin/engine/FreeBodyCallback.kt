package engine

import engine.freeBody.FreeBody
import org.jbox2d.dynamics.Body

class FreeBodyCallback(val freeBody: FreeBody, val callback: (FreeBody, Body) -> Unit)
