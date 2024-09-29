package com.example.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameController implements Initializable {

    @FXML
    public GridPane rootGridPlane;

    public Pane insertedDiscsPane;

    private static final int rows = 6;
    private static final int columns = 7;

    private static final float diameter = 85;

    private static String PLAYER_ONE = "Player One";
    private static String PLAYER_TWO = "Player Two";
    private static final String discColor1 = "#24303E";
    private static final String discColor2 = "#4CAA08";
    private boolean isPlayerOneTurn = true;

    public TextField playerOneTextField, playerTwoTextField;

    @FXML
    public Button setNamesButton;

    @FXML
    public Label playerNameLabel;

    private boolean isAllowedToInsert = true;
    private Disc[][] insertedDiscsArray = new Disc[rows][columns]; //for developers
    public void createPlayGroundNow() {
        Shape withHoles = createPlayGround();
        rootGridPlane.add(withHoles,0,1);
        List<Rectangle> rectangle = createClickColumns();
        for(Rectangle rectangle1 : rectangle){
            rootGridPlane.add(rectangle1,0,1);
        }
        setNamesButton.setOnAction(event -> {
            PLAYER_ONE = playerOneTextField.getText();
            PLAYER_TWO = playerTwoTextField.getText();
            playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);
        });
    }
    public Shape createPlayGround(){
    Shape rectangleWithHoles = new Rectangle((columns+1)*diameter,(rows+1)*diameter);

        for(int i = 0;i < rows;i++){
            for(int j = 0; j < columns;j++){
                Circle circle = new Circle();
                circle.setRadius(diameter/2);
                circle.setCenterX(diameter/2);
                circle.setCenterY(diameter/2);
                circle.setSmooth(true);

                circle.setTranslateX(j * (diameter+5)+diameter/4);
                circle.setTranslateY(i * (diameter+5)+diameter/4);

                rectangleWithHoles = Shape.subtract(rectangleWithHoles,circle);
            }
        }
        rectangleWithHoles.setFill(Color.WHITE);
        return rectangleWithHoles;
    }
    public List<Rectangle> createClickColumns() {
        List<Rectangle> rectangleList = new ArrayList<>();
        Rectangle rectangle = null;
        for (int col = 0; col < columns; col++) {
            rectangle = new Rectangle(diameter, (rows + 1) * diameter);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(col*(diameter+5)+diameter/4);
            Rectangle finalRectangle = rectangle;
            rectangle.setOnMouseEntered(event -> finalRectangle.setFill(Color.valueOf("#eeeeee26")));
            rectangle.setOnMouseExited(event ->finalRectangle.setFill(Color.TRANSPARENT));

            final int column = col;
            rectangle.setOnMouseClicked(event -> {
                if(isAllowedToInsert) {
                    isAllowedToInsert = false;
                    insertDisc(new Disc(isPlayerOneTurn), column);
                }
            });
            rectangleList.add(rectangle);
        }
        return rectangleList;
    }

    private void insertDisc(Disc disc, int columns){

        int row = rows-1;
        while(row >= 0){
            if(insertedDiscsArray[row][columns] == null){
                break;
            }
            row--;
        }
        if(row < 0)
            return;

        insertedDiscsArray[row][columns] = disc;// for developers
        insertedDiscsPane.getChildren().add(disc);

        disc.setTranslateX(columns * (diameter+5) + diameter/4);
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);
        translateTransition.setByY(row*(diameter+5) + diameter/4);
        int currentRow = row;
        translateTransition.setOnFinished(event -> {
            isAllowedToInsert = true;
            if(gameEnded(currentRow,columns)){
                gameOver();
                return;
            }
            isPlayerOneTurn = !isPlayerOneTurn;
            playerNameLabel.setText(isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO);
        });
        translateTransition.play();
    }

    private boolean gameEnded(int row, int column) {

        List<Point2D> verticalPoints = IntStream.rangeClosed(row-3,row+3)
                .mapToObj(r-> new Point2D(r,column))
                .collect(Collectors.toList());
        List<Point2D> HorizontalPoints = IntStream.rangeClosed(column-3,column+3)
                .mapToObj(col-> new Point2D(row,col))
                .collect(Collectors.toList());
        Point2D startPoint1 = new Point2D(row-3,column+3);
        Point2D startPoint2 = new Point2D(row-3,column-3);
        List<Point2D> diagonalPoints1 = IntStream.rangeClosed(0,6)
                .mapToObj(i -> startPoint1.add(i,-i))
                .collect(Collectors.toList());
        List<Point2D> diagonalPoints2 = IntStream.rangeClosed(0,6)
                .mapToObj(i -> startPoint2.add(i,i))
                .collect(Collectors.toList());

        boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(HorizontalPoints) || checkCombinations(diagonalPoints1)|| checkCombinations(diagonalPoints2);
        return isEnded;
    }

    private boolean checkCombinations(List<Point2D> verticalPoints) {
        int chain = 0;
        for(Point2D piont:verticalPoints){

            int rowIndexForArray = (int) piont.getX();
            int columnIndexForArray = (int)piont.getY();

            Disc disc = getDiscIfPresent(rowIndexForArray,columnIndexForArray);
            if(disc != null && disc.isPlayerOneMove == isPlayerOneTurn) {
                chain++;
                if (chain == 4) {
                    return true;
                }
            }
                else{
                    chain = 0;
                }
        }
        return false;
    }
    private Disc getDiscIfPresent(int row,int column){
        if(row >= rows || row < 0 || column >= columns || column < 0){
            return null;
        }
        return insertedDiscsArray[row][column];
    }
    private void gameOver(){
        String winner = isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The winner is " + winner);
        alert.setContentText("want to play again ");

        ButtonType yesBtn = new ButtonType("Yes");
        ButtonType noBtn = new ButtonType("No, Exit");
        alert.getButtonTypes().setAll(yesBtn,noBtn);

        Platform.runLater( ()-> {
            Optional<ButtonType> btnClicked = alert.showAndWait();
            if(btnClicked.isPresent() && btnClicked.get() == yesBtn){
            resetGame();
            }
            else{
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void resetGame() {
        insertedDiscsPane.getChildren().clear();
        for(int row = 0;row < rows;row++){
            for(int col = 0;col < columns;col++){
                insertedDiscsArray[row][col] = null;
            }
        }
        isPlayerOneTurn = true;
        playerNameLabel.setText(PLAYER_ONE);
        createPlayGroundNow();
    }

    private static  class Disc extends Circle {
        private final boolean isPlayerOneMove;

        public Disc(boolean isPlayerOneMove){
            this.isPlayerOneMove = isPlayerOneMove;
            setFill(isPlayerOneMove ? Color.valueOf(discColor1) : Color.valueOf(discColor2));
            setRadius(diameter/2);
            setCenterX(diameter/2);
            setCenterY(diameter/2);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}