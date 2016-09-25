import frontend.DirectDrawDemo;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");

        DirectDrawDemo frontendWindow = new DirectDrawDemo(1000, 1000);

        frontendWindow.makeWindow();
    }
}

