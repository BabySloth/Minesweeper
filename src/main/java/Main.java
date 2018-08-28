package src.main.java;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

/**
 * Credit is to http://minesweeperonline.com/ for the game values (amount of columns, rows, and bombs along with
 * each difficulty name (easy, intermediate, expert, and custom)
 */
public class Main extends Application {
    private Difficulty difficulty = Difficulty.EASY;
    private boolean isPregame = true;
    private boolean gameEnded = false;

    private Label difficultyDisplay;
    private Label timeDisplay;
    private Label bombsLeftDisplay;

    private VBox mainHolder;
    private ScrollPane gameBoardHolder;
    private GridPane gameBoard;
    private VBox extraHolder = new VBox();  // For changing difficulty and help
    private Tile startingTile;
    private Tile[][] gameMatrix;

    private int bombAmount;

    @Override
    public void start(Stage primaryStage){
        mainHolder = new VBox(generateInformationSection());
        mainHolder.setAlignment(Pos.TOP_CENTER);
        mainHolder.setSpacing(10);
        mainHolder.getChildren().remove(new HBox());
        newGame(difficulty);

        Scene scene = new Scene(mainHolder, Measurements.windowWidth.value(), Measurements.windowHeight.value());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox generateInformationSection(){
        SplitPane topHolder = getInformationSplitPane();
        difficultyDisplay = getInformationLabel(difficulty.getStringName());
        timeDisplay = getInformationLabel("00:00");
        bombsLeftDisplay = getInformationLabel("Bombs left: 0");
        topHolder.getItems().addAll(difficultyDisplay, timeDisplay, bombsLeftDisplay);

        SplitPane bottomHolder = getInformationSplitPane();
        Button changeDifficulty = getInformationButton("Change difficulty");
        changeDifficulty.setOnAction(event -> changeDifficulty());
        Button pause = getInformationButton("Pause");
        Button newGame = getInformationButton("New");
        newGame.setOnAction(event -> newGame(difficulty));
        // Pause and newGame must be located in the middle
        HBox middle = new HBox(pause, newGame);
        middle.setMaxWidth(Measurements.windowWidth.value() / 3);
        Button help = getInformationButton("Help");
        help.setOnAction(event -> help());
        bottomHolder.getItems().addAll(changeDifficulty, middle, help); //pause and newGame together

        return new VBox(topHolder, bottomHolder);
    }
    /**
     * Specially designed SplitPane for information view
     * @return SplitPane holding three information nodes
     */
    private SplitPane getInformationSplitPane(){
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.3f, 0.6f, 0.9f);
        splitPane.setPrefSize(Measurements.windowWidth.value(), Measurements.displayHeight.value());
        return splitPane;
    }

    private Label getInformationLabel(String text){
        Label label = new Label(text);
        double width = Measurements.windowWidth.value() / 3;
        double height = Measurements.displayHeight.value();
        label.setPrefSize(width, height);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private Button getInformationButton(String text){
        Button button = new Button(text);
        double height = Measurements.displayHeight.value();
        button.setPrefHeight(height);
        button.setPrefWidth(Measurements.windowWidth.value() / 3);
        button.setAlignment(Pos.CENTER);
        return button;
    }

    /**
     * Help on the program
     */
    private void help(){
        String WARNING = "The current game will be deleted if you want help";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, WARNING, ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()){
            if(result.get().equals(ButtonType.OK)){
                // Makes space
                removeCurrentGame();
                extraHolder.getChildren().clear();
                mainHolder.getChildren().remove(extraHolder);

                // Text has leading space so the text isn't touching the edge of the window
                Text part1 = new Text("  This game was based off of: ");
                Hyperlink linkToSource = new Hyperlink("http://minesweeperonline.com/");
                Text part2 = new Text("\n  Instructions on how to play can be found here: ");
                Hyperlink linkToInstructions =
                        new Hyperlink("http://www.instructables.com/id/How-to-beat-Minesweeper/");

                extraHolder.getChildren().add(new TextFlow(part1, linkToSource, part2, linkToInstructions));
                mainHolder.getChildren().add(extraHolder);
            }
        }
    }

    /**
     * Warns player that they are changing difficulty and gives a box to select items (taking up the location
     * of where the game board was)
     */
    private void changeDifficulty(){
        // Warns player that the current game will be deleted
        String WARNING = "The current game will be deleted if you change difficulty.";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, WARNING, ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()){
            if(result.get().equals(ButtonType.OK)){
                // Makes space
                removeCurrentGame();
                extraHolder.getChildren().clear();
                mainHolder.getChildren().remove(extraHolder);

                ObservableList<String> choices =
                        FXCollections.observableArrayList("Easy", "Intermediate", "Expert");
                ChoiceBox choicesHolder = new ChoiceBox(choices);
                // Reminds player what mode they are playing on (even though it is on top)
                choicesHolder.getSelectionModel().select(difficulty.stringName);

                // Creates a new game based on the choice
                Button confirm = new Button("Create game");
                confirm.setOnAction(event -> {
                    String selectedDifficulty = (String) choicesHolder.getSelectionModel().getSelectedItem();
                    selectedDifficulty = selectedDifficulty.toUpperCase();  //Enums are CAPS
                    newGame(Difficulty.valueOf(selectedDifficulty));
                });

                extraHolder = new VBox(choicesHolder, confirm);
                extraHolder.setSpacing(20);
                mainHolder.getChildren().add(extraHolder);
            }
        }
    }

    /**
     * Begins a new game by recreating the gameBoard and updating the information on top to fit a new game.
     */
    private void newGame(Difficulty newDifficulty){
        // Removes things blocking where the new game goes
        removeCurrentGame();
        mainHolder.getChildren().remove(extraHolder);

        // Generates new game
        difficulty = newDifficulty;
        generateGameBoard();
        mainHolder.getChildren().add(gameBoardHolder);
        // Update global variable
        isPregame = true;
        gameEnded = false;
    }

    private void removeCurrentGame(){
        // Update information section
        // Don't need to set bombAmount to 0 since user can't see this number and will be set to correct
        // value once the user clicks on something
        bombsLeftDisplay.setText("0");

        // Removes previous game and adds a new one
        mainHolder.getChildren().remove(gameBoardHolder);
    }

    /**
     * Creates a gameBoard that holds all the tiles. The gameBoard will be put in a StackPane to allow scrolling
     * when the gameBoard is too large to fit the screen. The StackPane will be put into the mainHolder in
     * {@link #newGame(Difficulty)}.
     */
    private void generateGameBoard(){
        gameBoard = new GridPane();

        int rowAmount = difficulty.getRowsAmount();
        int columnAmount = difficulty.getColumnsAmount();

        // Generate rows and columns
        for(int rowIndex = 0; rowIndex < rowAmount; rowIndex++){
            RowConstraints row = new RowConstraints(Measurements.tileSide.value());
            gameBoard.getRowConstraints().add(row);
        }
        for(int columnIndex = 0; columnIndex < columnAmount; columnIndex++){
            ColumnConstraints column = new ColumnConstraints(Measurements.tileSide.value());
            gameBoard.getColumnConstraints().add(column);
        }

        createTiles();

        for(int rowIndex = 0; rowIndex < rowAmount; rowIndex++){
            for(int columnIndex = 0; columnIndex < columnAmount; columnIndex++){
                // Don't need values in this case since it is pregame
                gameBoard.add(gameMatrix[columnIndex][rowIndex], columnIndex, rowIndex);
            }
        }

        gameBoardHolder = new ScrollPane(gameBoard);
        gameBoardHolder.setPrefSize(Measurements.windowWidth.value(), Measurements.windowHeight.value());
    }

    private void createTiles(){
        int columnsAmount = difficulty.getColumnsAmount();
        int rowsAmount = difficulty.getRowsAmount();

        // Creates matrix to hold all the bombs, if you want to take the code this line gives the gameplay part
        gameMatrix = new Tile[columnsAmount][rowsAmount];
        for(int y = 0; y < rowsAmount; y++) {
            for(int x = 0; x < columnsAmount; x++) {
                gameMatrix[x][y] = new Tile(x, y);
            }
        }
    }

    private void setBombs(){
        Random random = new Random();
        int bombsCreated = 0;
        int bombsNeeded = difficulty.getBombsAmount();

        while(bombsCreated != bombsNeeded){
            int x = random.nextInt(difficulty.getColumnsAmount());
            int y = random.nextInt(difficulty.getRowsAmount());

            Tile tile = gameMatrix[x][y];

            if(tile.isBomb() || isStartLocation(x, y)){
                continue;
            }else{
                tile.setBomb();
            }

            bombsCreated++;
        }
    }

    private boolean isStartLocation(int x, int y){
        // Bombs cannot be placed within one tile of these start location
        int startX = GridPane.getColumnIndex(startingTile);
        int startY = GridPane.getRowIndex(startingTile);

        return (Math.abs(x - startX) <= 1 || Math.abs(y - startY) <= 1);
    }

    private void lost(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "You lost");
        alert.showAndWait();
        for(Tile[] rows : gameMatrix){
            for(Tile tile : rows){
                if(tile.isBomb()){
                    tile.removeCover();
                }
            }
        }

        gameEnded = true;
    }

    private void win(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "You won!");
        alert.showAndWait();

        gameEnded = true;
    }

    /**
     * The player wins when all tiles are shown and all marked tiles are bombs and all bombs are marked.
     * Calls {@link #win()} if these conditions are met.
     */
    private void checkWin(){
        // Checks if all tiles are shown or are marked correctly, returns if any are false
        for(Tile[] row : gameMatrix){
            for(Tile tile : row){
                // All tiles must be shown or marked
                if(!tile.isShown() && !tile.isMarked()) return;

                if(tile.isMarked()){
                    if(!tile.isBomb()){
                        return;
                    }
                }
            }
        }

        win();
    }

    private class Tile extends StackPane {
        private boolean isBomb = false;
        private boolean isShown = false;
        private boolean isMarked = false;

        // Position in the gameBoard and gameMatrix
        private final int x;
        private final int y;

        // How big each node should be
        private double cellSideLength;

        private Rectangle cover;
        private Label markLabel;

        Tile(int x, int y){
            // Position
            this.x = x;
            this.y = y;
            
            // Make the StackPane a square shaped. Each node inside should be square shaped as well.
            this.setPrefSize(Measurements.tileSide.value(), Measurements.tileSide.value());
            this.cellSideLength = Measurements.tileSide.value() - 2;

            generateCover();

            this.setOnMouseClicked(this::clickedOn);
        }

        /**
         * Generates a tile that will cover the bomb node behind it or the emptiness behind it (from no bombs nearby,
         * hence, there are no numbers). If there should be a number behind, it will be removed and a label will be
         * created {@link #showNumberLabel()}
         */
        private void generateCover(){
            cover = new Rectangle(cellSideLength, cellSideLength);
            cover.setFill(Color.web("d9d9d9", 1));
            this.getChildren().add(cover);
        }

        /**
         * When it is pregame, the bomb tiles will be chosen and clears the tile that was clicked on (the first
         * tile and the surrounding 8 tiles will never be a bomb.
         *
         * If it isn't pregame:
         * Right click means to mark or unmark a tile. A marked tile cannot be left clicked on.
         * If it is a left click, it will uncover the cover (rectangle on top of the StackPane) and either
         * show a bomb the user lost or show a label with the amount of nearby bombs. It will also
         * show any empty tiles (tiles with no bombs nearby) if the one pressed on is an empty tile.
         * @param event Used to determine right or left click
         */
        private void clickedOn(MouseEvent event){
            // Don't want user clicking things when the game is over
            if(gameEnded) return;

            if(isPregame){
                startGame();
            }
            if((event.getButton().equals(MouseButton.SECONDARY) || event.isShortcutDown()) && !isShown){
                if(isMarked){
                    unmark();
                }else{
                    mark();
                }
            }else if(!isMarked){
                if(isBomb){
                    lost();
                    this.setStyle("-fx-background-color: #ff4;");
                }else{
                    showNearbyTiles(this);
                }
            }

            checkWin();
        }

        /**
         * Calls {@link #setBomb()} and updates bombsLeftDisplay to show total bombs created
         */
        private void startGame(){
            startingTile = this;
            isPregame = false;
            setBombs();

            bombAmount = difficulty.getBombsAmount();
            bombsLeftDisplay.setText(String.valueOf(bombAmount));
        }

        /**
         * Adds a "!" ontop of the cover to represent the player thinks there is a bomb.
         * Will update the information displayed to user as well.
         * If all tiles are marked and are correct, the user wins
         */
        private void mark(){
            // For the tile
            isMarked = true;
            markLabel = new Label("!");
            markLabel.setTextFill(Color.RED);
            this.getChildren().add(markLabel);

            // For information section
            if(bombAmount != 0) bombAmount--;
            bombsLeftDisplay.setText(String.valueOf(bombAmount));
        }

        /**
         * Removes the "!" ontop of the cover.
         * Will update the information displayed to the user as well.
         */
        private void unmark(){
            // For the tile
            isMarked = false;
            this.getChildren().remove(markLabel);
            markLabel = null;  // So tile can be click and shown again, uncovering looks at if markLabel == null

            // For information section
            bombAmount++;
            bombsLeftDisplay.setText(String.valueOf(bombAmount));
        }

        /**
         * Removes the cover node and adds a label showing the number from to give an illusion that there is a label
         * underneath {@link #createNumberLabel(int)}.
         */
        private void showNumberLabel(){
            // Fixes bug of user first action being marked rather than being a left click and having a tile
            // marked and no way to fix it
            if(markLabel == null){
                removeCover();
                this.getChildren().add(createNumberLabel(numberBombsNearby()));
                isShown = true;
            }
        }

        /**
         * Gets the amount of bombs surrounding the tile
         * @return int between 0 and 8
         */
        private int numberBombsNearby(){
            int bombsNearby = 0;
            for(Tile tile : getSurroundingTiles(this)){
                if(tile.isBomb()){
                    bombsNearby++;
                }
            }
            return bombsNearby;
        }

        /**
         * Recursively shows tiles if they are empty or tile is adjacent to empty one
         * @param tile  Tile that was clicked on
         */
        private void showNearbyTiles(Tile tile){
            if(tile.isShown){
                return;
            }
            tile.showNumberLabel();

            for(Tile adjacent : getSurroundingTiles(tile)){
                // Empty tiles will show all surrounding 8 tiles
                // If there are any empty tiles next the one clicked, the empty tiles should be shown as well
                // bomb tiles are technically have no bombs nearby, so we need to check that too
                if(tile.numberBombsNearby() == 0 || (adjacent.numberBombsNearby() == 0 && !adjacent.isBomb)){
                    showNearbyTiles(adjacent);
                }
            }
        }

        /**
         * Returns a list of the eight tiles surrounding the middle tile (input)
         * @param tile  The middle tile
         * @return  An array which size can be anywhere between 3 to 8
         */
        private Tile[] getSurroundingTiles(Tile tile){
            ArrayList<Tile> surround = new ArrayList<>();
            int x = tile.x;
            int y = tile.y;

            boolean hasUp = y != 0;
            boolean hasDown = y != difficulty.getRowsAmount() - 1;
            boolean hasLeft = x != 0;
            boolean hasRight = x != difficulty.getColumnsAmount() - 1;

            // Bottom 3 tiles
            if(hasDown){
                // Down left
                if(hasLeft){
                    surround.add(gameMatrix[x - 1][y + 1]);
                }
                // Direct down
                surround.add(gameMatrix[x][y + 1]);
                // Down right
                if(hasRight){
                    surround.add(gameMatrix[x + 1][y + 1]);
                }
            }

            // Left and right
            if(hasRight){
                surround.add(gameMatrix[x + 1][y]);
            }
            if(hasLeft){
                surround.add(gameMatrix[x - 1][y]);
            }

            // Top 3 tiles
            if(hasUp){
                // Top left
                if(hasLeft){
                    surround.add(gameMatrix[x - 1][y - 1]);
                }
                // Direct up
                surround.add(gameMatrix[x][y - 1]);
                // Top right
                if(hasRight){
                    surround.add(gameMatrix[x + 1][y - 1]);
                }
            }
            return surround.toArray(new Tile[0]);
        }

        /**
         * Removes the rectangle covering what is below
         */
        private void removeCover(){
            this.getChildren().remove(cover);
        }

        /**
         * Returns a label with colored text
         * @param nearbyBombs   Amount of bombs nearby a middle tile. Only numbers zero to eight
         * @return  Label with a colored number or nothing
         */
        private Label createNumberLabel(int nearbyBombs){
            Label label = new Label();
            label.setAlignment(Pos.CENTER);

            switch(nearbyBombs){
                case 1:
                    label.setText("1");
                    label.setTextFill(Color.BLUE);
                    break;
                case 2:
                    label.setText("2");
                    label.setTextFill(Color.GREEN);
                    break;
                case 3:
                    label.setText("3");
                    label.setTextFill(Color.RED);
                    break;
                case 4:
                    label.setText("4");
                    label.setTextFill(Color.DARKBLUE);
                    break;
                case 5:
                    label.setText("5");
                    label.setTextFill(Color.DARKRED);
                    break;
                case 6:
                    label.setText("6");
                    label.setTextFill(Color.TURQUOISE);
                    break;
                case 7:
                    label.setText("7"); //Color black
                    break;
                case 8:
                    label.setText("8");
                    label.setTextFill(Color.GRAY);
                    break;
            }
            return label;
        }

        boolean isBomb() {
            return isBomb;
        }

        /**
         * Sets the tile to be a bomb and adds the bomb decal and adds a red rectangle underneath the cover (gray
         * rectangle) to represent a bomb.
         */
        void setBomb() {
            isBomb = true;

            // Adds a bomb underneath the cover
            int coverIndex = this.getChildren().indexOf(cover);
            this.getChildren().add(coverIndex, new Rectangle(cellSideLength, cellSideLength, Color.RED));
        }

        boolean isMarked() {
            return isMarked;
        }

        boolean isShown(){
            return isShown;
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}
