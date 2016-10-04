package backend;

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
}
