package src.main.java;

public enum Difficulty {
    EASY(9, 9, 10),
    INTERMEDIATE(16, 16, 40),
    EXPERT(16, 30, 99);

    private final int rowsAmount;
    private final int columnsAmount;
    private final int bombsAmount;

    Difficulty(int rowAmount, int columnsAmount, int bombsAmount) {
        this.rowsAmount = rowAmount;
        this.columnsAmount = columnsAmount;
        this.bombsAmount = bombsAmount;
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

    @Override
    public String toString(){
        String name = name();
        return String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1).toLowerCase();
    }
}
