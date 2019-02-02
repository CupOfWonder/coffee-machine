package DrinksAndPrices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.omg.CORBA.Any;
import org.omg.CORBA.DataOutputStream;
import org.omg.CORBA.Object;
import org.omg.CORBA.TypeCode;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileClass {

    public static void main(String[] args) {

        List<Drink> list = new ArrayList<Drink>();
        list.add(0, new Drink("Coca-cola", 50));
        list.add(1, new Drink("Fanta", 150));
        list.add(2, new Drink("Pepsi", 250));
        list.add(3, new Drink("Sprite", 350));
        list.add(4, new Drink("BonAqua", 450));
        list.add(5, new Drink("Dr Pepper", 550));

        RecordFile(list);

        ReadingFile(list);
    }

    public static void RecordFile(List<Drink> l) {

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("D:/JavaP/model/test.bin"))) {
            oos.writeObject(l);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ReadingFile(List<Drink> l_1) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("D:/JavaP/model/test.bin"))) {
            try {
                List<Drink> l = (List<Drink>) ois.readObject();

                Iterator<Drink> iter = l.iterator();
                while (iter.hasNext()) {
                    System.out.println(iter.next());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}