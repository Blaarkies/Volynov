import frontend.DirectDrawDemo;

public class Main {

    private static void doTheThing(State state) {
        
    }

    public static void main(String[] args) {
        DirectDrawDemo frontendWindow = new DirectDrawDemo(1000, 1000);
        frontendWindow.makeWindow();

        State state = new State();
        doTheThing(state);

    }
}

