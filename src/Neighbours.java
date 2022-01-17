import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.Arrays;
import java.util.Random;
import static java.lang.Math.sqrt;
import static java.lang.System.exit;
import static java.lang.System.out;

/*
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
 *
 * NOTE:
 * - JavaFX first calls method init() and then method start() far below.
 * - To test methods uncomment call to test() first in init() method!
 *
 */
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    final static Random rand = new Random();

    class Actor {
        final Color color;        // Color an existing JavaFX class
        boolean isSatisfied;      // false by default

        Actor(Color color) {      // Constructor to initialize
            this.color = color;
        }  // Constructor, used to initialize
    }

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
    // This variable may *only* be used directly in methods init() and updateWorld()
    Actor[][] world;              // The world is a square matrix of Actors


    // This is the method called by the timer to update the world
    // (i.e move unsatisfied) approx each 1/60 sec.
     void updateWorld() {
           // % of surrounding neighbours that are like me
            double threshold = 0.7;
         // del 1.
            for (int i = 0; i < world[0].length; i++) {
                for (int j = 0; j < world[0].length; j++) {
                    int sum;
                    int tot_nei;
                    if (world[i][j] != null) {
                        sum = getSum(world, i, j)[0];
                        tot_nei = getSum(world, i, j)[1];
                        world[i][j].isSatisfied = (sum/(double)tot_nei) >= threshold;
                    }
                }
            }
            int[] nulls = getNullLocations(world); //2.
            shuffle(nulls); // 3.
            int[] unsatisfieds = getUnsatisfieds(world);
            for (int i = 0; i < unsatisfieds.length; i++) { // 4. loopa igenom våra actors och hitta de som inte är satisfied
                    world[nulls[i] / world[0].length][nulls[i] % world[0].length] = world[unsatisfieds[i] / world[0].length][unsatisfieds[i] % world[0].length]; // liten specialare eftersom våra index kommer i listform
                    world[unsatisfieds[i] / world[0].length][unsatisfieds[i] % world[0].length] = null;

                }
            }



    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime
    // That's why we must have "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        // test();    // <---------------- Uncomment to TEST, see below!

        // %-distribution of RED, BLUE and NONE
        double[] dist = {0.25, 0.25, 0.50};
        // Number of locations (places) in world (must be a square)
        int nLocations = 90000;   // Should also try 90 000

        // method that creates random world by calling generateDist and toMatrix
        Actor[][] world = createWorld(dist, nLocations);


        // Should be last
        fixScreenSize(nLocations);
    }


    Actor[][]  toMatrix(Actor arr[]){ // ska konvertera lista  av actors till matris
        Actor matrix[][] = new Actor[(int)sqrt(arr.length)][(int)sqrt(arr.length)];
        for (int i = 0; i < sqrt(arr.length); i++) {
            for (int j = 0; j < sqrt(arr.length); j++) {
                matrix[i][j] = arr[(int)sqrt(arr.length)*i+j];
            }
        }
        return matrix;
    }


    // method that creates random world by calling generateDist and toMatrix
    Actor[][] createWorld(double dist[],int locations) {
        Actor arr[];
        arr = generateDistribution(locations, dist[0], dist[1]);
        world = toMatrix(arr);
        return world;
    }


    Actor[] generateDistribution(int a, double b, double c) {
        int ca = (int) Math.round(b * a);
        int ba = (int) Math.round(c * a);
        int abc= (int) Math.round((a*(1 - (b + c))));

        int[] red = new int[(int) ca];
        int[] blue = new int[(int) ba];
        int[] zeros = new int[(int) abc];
        int[] arr = new int[a];

        Arrays.fill(red, 1);
        Arrays.fill(blue, -1);
        Arrays.fill(zeros, 0);

        System.arraycopy(red, 0, arr, 0, ca);
        System.arraycopy(blue, 0, arr, ca, ba);
        System.arraycopy(zeros, 0, arr, abc, abc);
        shuffle(arr);
        Actor[] act_arr = new Actor[arr.length];
        for (int i = 0; i < act_arr.length; i++){ // konverterar random siffror till en array av actors
            if (arr[i] == 1) {
                act_arr[i] = new Actor(Color.RED);
            } else if (arr[i] == -1){
                act_arr[i] = new Actor(Color.BLUE);
            } else {
                act_arr[i] = null;
            }
        }
        return act_arr;
    }


    int[] getSum(Actor[][] world, int row, int col) { // index 0 = rad, index 1 = kolumn
        int size = world[0].length;
        int[] sum = new int[2];
        sum[0] = -1; // börjar på minus ett för att den räknar med sig själv en gång, samma färg
        sum[1] = -1; // Tot neighbours, börjar också på -1
        int row_lbound = -1;
        int row_rbound = +1;
        int col_lbound = -1;
        int col_rbound = +1;

        if (!isValidLocation(size, row - 1, col)){ // adjust boundaries if out of bounds(invalid location)
            row_lbound = 0;
        } if (!isValidLocation(size, row + 1, col)) {
            row_rbound = 0;
        } if (!isValidLocation(size, row, col - 1)) {
            col_lbound = 0;
        } if (!isValidLocation(size, row, col + 1)) {
            col_rbound = 0;
        }

        for (int i = row + row_lbound; i <= row + row_rbound; i++) {
            for (int j = col + col_lbound; j <= col + col_rbound; j++) {
                if ( world[i][j] == null ) {
                } else if ( world[i][j].color == world[row][col].color ) { // if same color add, if null do nothing
                    sum[0] = sum[0]+1;
                    sum[1] = sum[1]+1;
                } else {
                    sum[1] = sum[1]+1;
                }
            }
        }
        return sum;
    }



    // Check if inside world
    boolean isValidLocation(int size, int row, int col) {
        return 0 <= row && row < size && 0 <= col && col < size;
    }

    // ----------- Utility methods -----------------

    void shuffle(int[] arr) {
        for (int i = arr.length; i > 1; i--) {
            int j = rand.nextInt(i);
            int tmp = arr[j];
            arr[j] = arr[i - 1];
            arr[i - 1] = tmp;
        }
    }


    int[] getNullLocations(Actor matrix[][]) { // will return locations of nulls in matrix, but in array form. Observe !
        int[] nulls = new int[matrix.length*matrix[0].length];

        int size = 0;
        for(int row = 0 ; row < matrix.length ; row++){
            for( int col = 0 ; col < matrix[row].length; col++){
                if (matrix[row][col] == null) {
                    nulls[size] = row*matrix.length + col;
                    size++;
                }
            }
        }
        nulls = Arrays.copyOf(nulls, size);
        return nulls;
    }

    int[] getUnsatisfieds(Actor matrix[][]) {
        int[] unsatisfieds = new int[matrix.length * matrix[0].length];
        int size = 0;

        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                if (matrix[row][col] != null) {
                    if (!world[row][col].isSatisfied) {
                        unsatisfieds[size] = row * matrix.length + col;
                        size++;
                    }
                }
            }
        }
        unsatisfieds = Arrays.copyOf(unsatisfieds, size);
        shuffle(unsatisfieds);
        return unsatisfieds;
    }



    // ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
    // to see that they really work. Important!!!!
    void test() {
        // A small hard coded world for testing
        Actor[][] testWorld = new Actor[][]{
                {new Actor(Color.RED), new Actor(Color.RED), null},
                {null, new Actor(Color.BLUE), null},
                {new Actor(Color.RED), null, new Actor(Color.BLUE)}
        };
        double th = 0.5;   // Simple threshold used for testing

        int size = testWorld.length;
        out.println(isValidLocation(size, 0, 0));   // This is a single test
        out.println(!isValidLocation(size, -1, 0));
        out.println(!isValidLocation(size, 0, 3));

        // TODO  More tests here. Implement and test one method at the time
        // TODO Always keep all tests! Easy to rerun if something happens



        exit(0);
    }

    // ******************** NOTHING to do below this row, it's JavaFX stuff  **************

    double width = 500;   // Size for window
    double height = 500;
    final double margin = 50;
    double dotSize;

    void fixScreenSize(int nLocations) {
        // Adjust screen window
        dotSize = (double) 9000 / nLocations;
        if (dotSize < 1) {
            dotSize = 2;
        }
        width = sqrt(nLocations) * dotSize + 2 * margin;
        height = width;
    }

    long lastUpdateTime;
    final long INTERVAL = 450_000_000;


    @Override
    public void start(Stage primaryStage) throws Exception {

        // Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long now) {
                long elapsedNanos = now - lastUpdateTime;
                if (elapsedNanos > INTERVAL) {
                    updateWorld();
                    renderWorld(gc);
                    lastUpdateTime = now;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation");
        primaryStage.show();

        timer.start();  // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int x = (int) (dotSize * col + margin);
                int y = (int) (dotSize * row + margin);
                if (world[row][col] != null) {
                    g.setFill(world[row][col].color);
                    g.fillOval(x, y, dotSize, dotSize);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
