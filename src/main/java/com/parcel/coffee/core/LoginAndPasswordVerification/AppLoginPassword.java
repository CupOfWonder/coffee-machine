package LoginAndPasswordVerification;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.mindrot.jbcrypt.BCrypt;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class AppLoginPassword {

    public static void main(String[] args) {

        ModelCreationLoginPassword d = new ModelCreationLoginPassword("Person", "Person_1995");
        Map<String, String> mp = new HashMap<String, String>();
        mp.put("Login", d.getLogin());
        mp.put("Password", BCrypt.hashpw(d.getPassword(), BCrypt.gensalt()));

        FileWriter f = null;
        Gson g = null;
        String json = null;
        Scanner sc = new Scanner(System.in);

        try {
            f = new FileWriter("D:/JavaP/model/test_0.json", false);
            g = new GsonBuilder().setPrettyPrinting().create();
            json = g.toJson(mp);
            f.write(json);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//  Create login and password
        System.out.println("Enter login: "); //Person
        String test_login = sc.next();

        System.out.println("Enter password: "); //Person_1995
        String test_password = sc.next();

        CheckLoginPassword(mp, json, test_password, test_login);
    }

    public static void CheckLoginPassword(Map<String, String> m, String json, String test_pw, String test_lg) {
        Gson g = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> myMap = g.fromJson(json, type);

//Create LoginPassword

        if (test_lg.equals(myMap.get("Login")))
            System.out.println("Login is correct");
        else
            System.out.println("Wrong Login");

        if (BCrypt.checkpw(test_pw, myMap.get("Password")))
            System.out.println("Password is correct");
        else
            System.out.println("Wrong Password");
    }
}