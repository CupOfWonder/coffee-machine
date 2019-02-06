package DrinksAndPrices;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

 class ModelSaveLoad {

// Сохранение цен и названий
    public void preservationOfPricesAndNames() {

        List<Drink> list = new ArrayList<Drink>();
        list.add(0, new Drink("Coca-cola", 50));
        list.add(1, new Drink("Fanta", 150));
        list.add(2, new Drink("Pepsi", 250));
        list.add(3, new Drink("Sprite", 350));
        list.add(4, new Drink("BonAqua", 450));
        list.add(5, new Drink("Dr Pepper", 550));

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("D:/JavaP/model/test.bin"))) {
            oos.writeObject(list);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//Загрузка текущих цен и названий
    public List<Drink> loadingCurrentPricesAndTitles() {

        List<Drink> list = null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("D:/JavaP/model/test.bin"))) {
            try {
                 list = (List<Drink>) ois.readObject();

                Iterator<Drink> iter = list.iterator();
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
        return list;
    }
}

