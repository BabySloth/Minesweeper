package src.main.java;

public enum Difficulty {
    EASY("Easy", 9, 9, 10),
    INTERMEDIATE("Intermediate", 16, 16, 40),
    EXPERT("Expert", 16, 30, 99);

    // Variables aren't final since custom requires user input
    public final String stringName;
    private final int rowsAmount;
    private final int columnsAmount;
    private final int bombsAmount;

    Difficulty(String stringName, int rowAmount, int columnsAmount, int bombsAmount) {
        this.stringName = stringName;
        this.rowsAmount = rowAmount;
        this.columnsAmount = columnsAmount;
        this.bombsAmount = bombsAmount;
    }

    public String getStringName() {
        return stringName;
    }

    public int getRowsAmount() {
        return rowsAmount;
    }

    public int getColumnsAmount() {
        return columnsAmount;
    }

    public int getBombsAmount() {
        return bombsAmount;
    }
}
