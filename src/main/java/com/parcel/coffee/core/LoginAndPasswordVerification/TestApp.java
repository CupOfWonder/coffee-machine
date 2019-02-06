package LoginAndPasswordVerification;

public class TestApp {

    public static void main(String[] args) {

        ModelManagerPasswordLogin m = new ModelManagerPasswordLogin();

        m.verificationPasswordLogin("Person","Person_1995");

        m.changeLoginPassword("Person","Person_1","Person_1995", "Person_1996");
    }
}
