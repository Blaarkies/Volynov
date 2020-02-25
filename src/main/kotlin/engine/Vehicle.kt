package engine

class Vehicle(
    id: String,
    x: Double,
    y: Double,
    h: Double,
    dx: Double,
    dy: Double,
    dh: Double,
    mass: Double = 1.0,
    temperature: Double = 325.0,
    radius: Double = 13.0,
    restitution: Double = .5
) : FreeBody(id, mass, temperature, radius, x, y, h, dx, dy, dh, restitution)
