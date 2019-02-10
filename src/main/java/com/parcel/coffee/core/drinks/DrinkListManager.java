package com.parcel.coffee.core.drinks;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DrinkListManager {

    public static final String FILE_PATH = "drinks.bin";

    public DrinkListManager() {
        if(!drinkFileExists()) {
            createDefaultDrinkList();
        }
    }

    private boolean drinkFileExists() {
        File f = new File(FILE_PATH);
        return f.exists() && !f.isDirectory();
    }

    // Сохранение цен и названий
    public void createDefaultDrinkList() {

        List<Drink> list = new ArrayList<Drink>();
        list.add(0, new Drink("Напиток 1", 50));
        list.add(1, new Drink("Напиток 2", 150));
        list.add(2, new Drink("Напиток 3", 250));
        list.add(3, new Drink("Напиток 4", 350));
        list.add(4, new Drink("Напиток 5", 450));
        list.add(5, new Drink("Напиток 6", 550));

        savePricesAndTitles(list);
    }

    public void savePricesAndTitles(List<Drink> drinkList) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(drinkList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Загрузка текущих цен и названий
    public List<Drink> loadCurrentPricesAndTitles() {

        List<Drink> list = null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            list = (List<Drink>) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }
}

