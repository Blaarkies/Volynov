package display

import dI
import display.event.KeyboardEvent
import display.event.MouseButtonEvent
import display.event.MouseScrollEvent
import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.Callback
import org.lwjgl.system.MemoryUtil
import utility.Common.makeVec2

class Window(private val title: String,
             var width: Int,
             var height: Int,
             private var vSync: Boolean) {

    private var windowHandle: Long = 0
    private var callbacks: MutableList<Callback> = mutableListOf()

    val keyboardEvent = PublishSubject.create<KeyboardEvent>()
    val mouseButtonEvent = PublishSubject.create<MouseButtonEvent>()
    val cursorPositionEvent = PublishSubject.create<MouseButtonEvent>()
    val mouseScrollEvent = PublishSubject.create<MouseScrollEvent>()
    val textInputEvent = PublishSubject.create<String>()

    fun init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))
            ?.let { callbacks.add(it) }

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL_FALSE) // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL_TRUE) // the window will be resizable
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4) // anti-aliasing

        when (dI.isDebugMode) {
            false -> windowHandle =
                GLFW.glfwCreateWindow(width, height, title, GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL)
            true -> {
                windowHandle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
                if (windowHandle == MemoryUtil.NULL) {
                    throw RuntimeException("Failed to create the GLFW window")
                }
                GLFW.glfwSetFramebufferSizeCallback(windowHandle) { _, width, height ->
                    this.width = width
                    this.height = height
                }

                val videoRes = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!
                GLFW.glfwSetWindowPos(windowHandle, (videoRes.width() - width) / 2, (videoRes.height() - height) / 2)
            }

        }

        GLFW.glfwMakeContextCurrent(windowHandle) // Make the OpenGL context current
        if (isVSync()) { // Enable v-sync
            GLFW.glfwSwapInterval(1)
        }
        GLFW.glfwShowWindow(windowHandle)
        GL.createCapabilities()
        glClearColor(0f, 0f, 0f, 0f)

        setupInputCallbacks()
    }

    private fun setupInputCallbacks() {
        GLFW.glfwSetKeyCallback(windowHandle) { _, key, scancode, action, mods ->
            if (!handleDebugKeys(key, scancode, action, mods)) {
                keyboardEvent.onNext(KeyboardEvent(key, scancode, action, mods))
            }
        }?.let { callbacks.add(it) }

        GLFW.glfwSetMouseButtonCallback(windowHandle) { _, button, action, mods ->
            mouseButtonEvent.onNext(MouseButtonEvent(button, action, mods, getCursorPosition()))
        }?.let { callbacks.add(it) }

        GLFW.glfwSetCursorPosCallback(windowHandle) { _, _, _ ->
            cursorPositionEvent.onNext(MouseButtonEvent(-1, -1, -1, getCursorPosition()))
        }?.let { callbacks.add(it) }

        GLFW.glfwSetScrollCallback(windowHandle) { _, xOffset, yOffset ->
            mouseScrollEvent.onNext(MouseScrollEvent(makeVec2(xOffset, yOffset), getCursorPosition()))
        }?.let { callbacks.add(it) }

        GLFW.glfwSetCharCallback(windowHandle) { _, codepoint ->
            textInputEvent.onNext(Character.toChars(codepoint)[0].toString())
        }?.let { callbacks.add(it) }
        //        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    private fun handleDebugKeys(key: Int, scancode: Int, action: Int, mods: Int): Boolean {
        if (key == GLFW.GLFW_KEY_F12) {
            when (glGetInteger(GL_POLYGON_MODE)) {
                GL_LINE -> glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
                GL_POINT -> glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                else -> glPolygonMode(GL_FRONT_AND_BACK, GL_POINT)
            }
            return true
        }
        return false
    }

    fun windowShouldClose(): Boolean = GLFW.glfwWindowShouldClose(windowHandle)

    fun isVSync(): Boolean = vSync

    fun setVSync(vSync: Boolean) {
        this.vSync = vSync
    }

    fun update() {
        GLFW.glfwSwapBuffers(windowHandle)
        GLFW.glfwPollEvents()
    }

    fun getCursorPosition(): Vec2 {
        val x = BufferUtils.createDoubleBuffer(1)
        val y = BufferUtils.createDoubleBuffer(1)
        GLFW.glfwGetCursorPos(windowHandle, x, y)

        return makeVec2(x, y)
    }

    fun exit() {
        callbacks.forEach { it.free() }
        GLFW.glfwSetWindowShouldClose(windowHandle, true)
    }

}

