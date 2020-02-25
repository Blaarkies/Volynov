package engine

class Planet(
    id: String,
    x: Double,
    y: Double,
    h: Double,
    dx: Double,
    dy: Double,
    dh: Double,
    mass: Double = 100.0,
    temperature: Double = 325.0,
    radius: Double = 20.0,
    restitution: Double = .1
) : FreeBody(id, mass, temperature, radius, x, y, h, dx, dy, dh, restitution)
