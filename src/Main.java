import frontend.AnimatedBoat;

public class Main {

    public static void main(String[] args) {
        State state = new State();
        AnimatedBoat img = new AnimatedBoat();

        img.animationPane.drawNoise(0,0,100,100);

//        DirectDrawDemo frontendWindow = new DirectDrawDemo(1000, 1000);

        state.addPlayer();
        System.out.println(state.players.get(0).name);

//        frontendWindow.startup();
//        frontendWindow.makeWindow();
//        frontendWindow.drawNoise(250,250,500,500);

    }
}

