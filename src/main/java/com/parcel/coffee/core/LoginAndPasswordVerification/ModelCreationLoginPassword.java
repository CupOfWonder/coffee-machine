package LoginAndPasswordVerification;

public class ModelCreationLoginPassword {

    private static String login;
    private static String password;

    public ModelCreationLoginPassword(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
    public void setPassword(String password) {
        this.password = password;

    }

    public String getPassword() {
        return password;
    }
}

