package display.graphic

enum class CameraType {

    UNIVERSE, // normal lighting
    UNIVERSE_SPECTRAL, // only ambient lighting
    UNIVERSE_LAMP, // no lighting, used to display light sources
    GUI, // 100 ambient lighting
    NONE

}
