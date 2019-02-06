package DrinksAndPrices;

public class TestApp {

    public static void main(String[] args) {

        ModelSaveLoad m = new ModelSaveLoad();

        m.preservationOfPricesAndNames();

        m.loadingCurrentPricesAndTitles();
    }
}