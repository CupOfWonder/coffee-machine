package LoginAndPasswordVerification;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

class ModelManagerPasswordLogin {
      private enum Status {
           CONFIRMATION_OK, INCORRECT_OLD_LOGIN, INCOPRRECT_OLD_PASSWORD
       }

     private String createPasswordLogin() {

         ModelCreationLoginPassword d = new ModelCreationLoginPassword("Person", "Person_1995");
         Map<String, String> mp = new HashMap<>();
         mp.put("Login", d.getLogin());
         mp.put("Password", BCrypt.hashpw(d.getPassword(), BCrypt.gensalt()));

         File myFile = new File("D:/JavaP/model/test_0.json");
         BufferedWriter bf = null;
         Gson g = null;
         String json = null;

         try {
             bf = new BufferedWriter(new FileWriter(myFile, true));
             g = new GsonBuilder().setPrettyPrinting().create();
             json = g.toJson(mp);
             bf.write(json);

         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 bf.flush();
                 bf.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return json;
     }

     public boolean verificationPasswordLogin(String test_login, String test_Password) {
         String jsonFile = createPasswordLogin();
         boolean flag = false;

         Gson g = new Gson();
         Type type = new TypeToken<Map<String, String>>(){}.getType();
         Map<String, String> myMap = g.fromJson(jsonFile, type);

         if (test_login.equals(myMap.get("Login")) && BCrypt.checkpw(test_Password, myMap.get("Password")))
             flag = true;
             else
                 flag = false;

         return flag;
     }

     public Status changeLoginPassword (String old_Login, String new_Login, String old_Password, String new_Password) {

          String jsonFile_1 = createPasswordLogin();
          Status flag = null;

          Gson g = new Gson();
          Type type = new TypeToken<Map<String, String>>(){}.getType();
          Map<String, String> myMap = g.fromJson(jsonFile_1, type);

          if(old_Login.equals(myMap.get("Login")) && BCrypt.checkpw(old_Password, myMap.get("Password"))) {

              ModelCreationLoginPassword d = new ModelCreationLoginPassword(new_Login, new_Password);
              Map<String, String> mp = new HashMap<>();
              mp.put("New_Login", d.getLogin());
              mp.put("New_Password", BCrypt.hashpw(d.getPassword(), BCrypt.gensalt()));

              File myFile = new File("D:/JavaP/model/test_0.json");
              BufferedWriter bf = null;
              Gson gs = null;
              String json = null;

              try {
                  bf = new BufferedWriter(new FileWriter(myFile, true));
                  gs = new GsonBuilder().setPrettyPrinting().create();
                  json = gs.toJson(mp);
                  bf.write(json);

              } catch (IOException e) {
                  e.printStackTrace();
              } finally {
                  try {
                      bf.flush();
                      bf.close();
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              }
              flag = Status.CONFIRMATION_OK;
          }
          else if (old_Login.equals(myMap.get("Login")) == false) {
              flag = Status.INCORRECT_OLD_LOGIN;
          }
          else if (BCrypt.checkpw(old_Password, myMap.get("Password")) == false) {
              flag = Status.INCOPRRECT_OLD_PASSWORD;
          }
          return flag;
     }
}
