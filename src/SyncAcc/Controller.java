package SyncAcc;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller {

    @FXML public Pane pane;
    @FXML public Label firstLabel;
    @FXML public Label secondLabel;
    @FXML public Button cmdButton;
    public ScrollPane scroll;

    private final Dot dot1 = new Dot();
    private final Dot dot2 = new Dot();

    private AccelerateTask dot1Acc = new AccelerateTask(20);
    private AccelerateTask dot2Acc = new AccelerateTask(20);

    private boolean racing = false;
    private static class Dot extends Circle {

        private Dot(){
            super();
            Random random = new Random();
            double r = random.nextDouble();
            double g = random.nextDouble();
            double b = random.nextDouble();

            Color color = Color.color(r, g ,b);
            this.setFill(color);

            this.setStroke(Color.BLACK);
            this.setStrokeWidth(2);

            this.setRadius(20);
        }

    }

    @FXML
    private void initialize(){
        DecimalFormat format = new DecimalFormat("000.00");

        dot1.centerXProperty().addListener((observable, oldValue, newValue) -> {
            String dis = format.format(newValue) + " m";
            String time = format.format(dot1Acc.time) + " sec";

            firstLabel.setText("Dot 1: "+ dis + " Time: " + time);
        });

        dot2.centerXProperty().addListener((observable, oldValue, newValue) -> {
            String dis = format.format(newValue) + " m";
            String vel = format.format(dot2Acc.velocity) + " m/s";
            String time = format.format(dot2Acc.time) + " sec";

            secondLabel.setText("Dot 2: "+ dis + " Velocity: " + vel +" Time: " + time);
        });

        dot1.centerXProperty().bind(dot1Acc.valueProperty());
        dot2.centerXProperty().bind(dot2Acc.valueProperty());

        pane.getChildren().addAll(dot1, dot2);
        Platform.runLater(() -> {
            double height = pane.getHeight();

            double dot1Y = 40;
            double dot2Y = (height) - dot1Y;

//            dot1.setCenterX(dot1.getRadius());
//            dot2.setCenterX(dot2.getRadius());

            dot1.setCenterY(dot1Y);
            dot2.setCenterY(dot2Y);
        });

        cmdButton.setOnAction(event -> {
            startRacing();
        });

        pane.widthProperty().addListener((observable, oldValue, newValue) -> {
            scroll.setHvalue(1);
        });
    }

    private static class AccelerateTask extends Task<Double>{

        double acceleration;
        double velocity = 0;
        double time = 0;

        private AccelerateTask(double acceleration){
            this.acceleration = acceleration;

        }

        @Override
        protected Double call() throws Exception {
            while (true) {
                if (isCancelled())
                    break;

                Thread.sleep(50);
                time += 0.05;

                // if there's acceleration
                // v = at;
                if (acceleration != 0)
                    velocity = acceleration * time;

                updateValue(calcValue());
            }
            return calcValue();
        }

        private double calcValue(){
            // v^2 = 2as
            // s = v^2 / 2a
            double s;
            if (acceleration != 0) {
                s = Math.pow(velocity, 2) / (2 * acceleration);

//                // if decelerating
//                if (acceleration < 0)
//                    s = valueProperty().get() - Math.pow(velocity, 2) / (2 * acceleration);
            }
            else {
                System.out.println("using s = ut "+ velocity);
                s = velocity * time;


            }
            return (double)Math.round(s * 100000d) / 100000d;
        }
    }

    private ExecutorService service = Executors.newFixedThreadPool(2);

    private void startRacing(){
        if (! racing) {
            cmdButton.setText("Pause");
            service.submit(dot1Acc);
            service.submit(dot2Acc);
            racing = true;
        }
        else{
            cmdButton.setText("Start");
            service.shutdownNow();
            racing = false;
        }

    }

}
