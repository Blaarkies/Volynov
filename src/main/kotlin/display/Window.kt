package display

import io.reactivex.subjects.PublishSubject
import org.jbox2d.common.Vec2
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.Callback
import org.lwjgl.system.MemoryUtil

class Window(private val title: String, var width: Int, var height: Int, private var vSync: Boolean) {

    private var windowHandle: Long = 0

    private var callbacks: MutableList<Callback> = mutableListOf()

    val keyboardEvent = PublishSubject.create<KeyboardEvent>()
    val mouseButtonEvent = PublishSubject.create<MouseButtonEvent>()
    val cursorPositionEvent = PublishSubject.create<Vec2>()
    val mouseScrollEvent = PublishSubject.create<Vec2>()

    fun init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFW.glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err))
            ?.let { callbacks.add(it) }

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE) // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE) // the window will be resizable
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE)
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4) // anti-aliasing
        // Create the window
        windowHandle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowHandle == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }
        // Setup resize callback
        GLFW.glfwSetFramebufferSizeCallback(windowHandle) { _, width, height ->
            this.width = width
            this.height = height

            println("FramebufferSizeCallback $width, $height")
        }

        // Get the resolution of the primary monitor
        val videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!
        // Center our window
        GLFW.glfwSetWindowPos(windowHandle, (videoMode.width() - width) / 2, (videoMode.height() - height) / 2)
        GLFW.glfwMakeContextCurrent(windowHandle) // Make the OpenGL context current
        if (isVSync()) { // Enable v-sync
            GLFW.glfwSwapInterval(1)
        }
        GLFW.glfwShowWindow(windowHandle)
        GL.createCapabilities()
        GL11.glClearColor(0f, 0f, 0f, 0f)

        setupInputCallbacks()
    }

    private fun setupInputCallbacks() {
        GLFW.glfwSetKeyCallback(windowHandle) { window, key, scancode, action, mods ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                callbacks.forEach { it.free() }
                GLFW.glfwSetWindowShouldClose(window, true)
            } else {
                keyboardEvent.onNext(KeyboardEvent(key, scancode, action, mods))
            }
        }?.let { callbacks.add(it) }

        GLFW.glfwSetMouseButtonCallback(windowHandle) { window, button, action, mods ->
            mouseButtonEvent.onNext(MouseButtonEvent(button, action, mods))
        }?.let { callbacks.add(it) }

        GLFW.glfwSetCursorPosCallback(windowHandle) { window, xPos, yPos ->
            cursorPositionEvent.onNext(Vec2(xPos.toFloat(), yPos.toFloat()))
        }?.let { callbacks.add(it) }

        GLFW.glfwSetScrollCallback(windowHandle) { window, xOffset, yOffset ->
            mouseScrollEvent.onNext(Vec2(xOffset.toFloat(), yOffset.toFloat()))
        }?.let { callbacks.add(it) }

//        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
//        glfwSetCharCallback(window, character_callback);
    }

    fun setClearColor(r: Float, g: Float, b: Float, alpha: Float) {
        GL11.glClearColor(r, g, b, alpha)
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

        return Vec2(x.get().toFloat(), y.get().toFloat())
    }

}

