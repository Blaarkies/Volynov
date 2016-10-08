package backend;

import backend.motion.Motion;

public class Vehicle extends FreeBody {
// this will split up into "Player" later on

    public String name;
    public int hitpoints;
    public int money;
    public int kills;
    public int deaths;

    public Vehicle() {
        this.name = "Vehicle 1";
        this.hitpoints = 100;
        this.money = 800;
        this.kills = 0;
        this.deaths = 0;

        this.motion.position.setPosition(250, 250);
    }

    public Vehicle(String name, int hitpoints, int money, int kills, int deaths, FreeBody freeBody) {
        this();
        this.name = name;
        this.hitpoints = hitpoints;
        this.money = money;
        this.kills = kills;
        this.deaths = deaths;

        this.motion.position.setPosition(freeBody.motion.position.x, freeBody.motion.position.y);
        this.radius = freeBody.radius;

        this.mass = 1;
        this.angularMass = getAngularMass(mass,radius);
        this.temperature = 315;
        this.motion = new Motion(freeBody.motion.position.x,
                freeBody.motion.position.y,
                freeBody.motion.position.h,
                freeBody.motion.velocity.dx,
                freeBody.motion.velocity.dy,
                freeBody.motion.velocity.dh,
                freeBody.motion.trailersPopulation);

        this.id = freeBody.id;
    }
}
