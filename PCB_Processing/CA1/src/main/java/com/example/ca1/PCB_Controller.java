package com.example.ca1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.*;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

public class PCB_Controller {

    @FXML
    private Label totalNumber;
    @FXML
    ImageView imageView;
    @FXML
    ImageView rectImageView;
    @FXML
    private TextField noiseReductionTextField;
    @FXML
    private TextField RED;
    @FXML
    private TextField GREEN;
    @FXML
    private TextField BLUE;

    @FXML
    private void handleSetColorButtonAction() {
        setColorComponentsFromTextFields(RED, GREEN, BLUE);
    }
    Image image;
    private WritableImage dest, destBW;
    private Map<Integer, DisjointSetNode<Integer>> disjointSetMap = new HashMap<>();
    private Set<DisjointSetNode<Integer>> components = new HashSet<>();
    private int numOfComp = 0;

    @FXML
    public void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            image = new Image(selectedFile.toURI().toString());
            imageView.setImage(image);
            rectImageView.setImage(new WritableImage((int) image.getWidth(), (int) image.getHeight())); //set up 2nd imageview
        }
    }

    public void resetImage() {
        imageView.setImage(image); //reset both views
        rectImageView.setImage(new WritableImage((int) image.getWidth(), (int) image.getHeight()));
    }

    //All variables set up related to the pixel clicked by the mouse.
    //runs blackandwhite function with all the parameters set up by pixel being clicked.
    public void getColour(MouseEvent mouseEvent) {
        //Clear previous data
        disjointSetMap.clear();
        components.clear();

        Image src = imageView.getImage();
        PixelReader pr = src.getPixelReader();

        double xPos = mouseEvent.getX();
        double yPos = mouseEvent.getY();
        int xPosition = (int) (xPos / imageView.getFitWidth() * src.getWidth());
        int yPosition = (int) (yPos / imageView.getFitHeight() * src.getHeight());

        Color col = pr.getColor(xPosition, yPosition);
        double hue = col.getHue();
        double sat = col.getSaturation();
        double bri = col.getBrightness();

        double minHue = hue * 0.7;
        double maxHue = hue * 1.30;
        double minSat = sat * 0.7;
        double maxSat = sat * 1.30;
        double minBri = bri * 0.7;
        double maxBri = bri * 1.30;

        int width = (int) src.getWidth();
        int height = (int) src.getHeight();
        dest = new WritableImage(pr, width, height);
        destBW = new WritableImage(pr, width, height);
        PixelWriter pw = dest.getPixelWriter();

        blackAndWhite(pr, minHue, maxHue, minSat, maxSat, minBri, maxBri, width, height, pw);
        imageView.setImage(dest);

        //After creating the black and white image, find components
        findComponents(width, height);

    }

    private void blackAndWhite(PixelReader pr, double minHue, double maxHue, double minSat, double maxSat, double minBri, double maxBri, int width, int height, PixelWriter pw) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pr.getColor(x, y);
                if ((color.getHue() < minHue || color.getHue() > maxHue) ||
                        (color.getSaturation() < minSat || color.getSaturation() > maxSat) ||
                        (color.getBrightness() < minBri || color.getBrightness() > maxBri)) {
                    color = Color.WHITE; // Set pixels to white
                } else {
                    color = Color.BLACK; // Else set them to black
                }

                pw.setColor(x, y, color);

                if (color.equals(Color.BLACK)) {
                    // Create a new disjoint set node for each black pixel with its coordinates
                    disjointSetMap.put(y * width + x, new DisjointSetNode<>(y * width + x, x, y));
                }
            }
        }
    }

    //checks if the node is a root, else it finds parent recursively
    DisjointSetNode<Integer> find(DisjointSetNode<Integer> n) {
        if (n.parent == null) {
            return n;
        } else {
            n.parent = find((DisjointSetNode<Integer>) n.parent);
            return (DisjointSetNode<Integer>) n.parent;
        }
    }

    //finds 2 nodes along with their roots. if the roots are different, they're merged into one set
    void union(DisjointSetNode<Integer> a, DisjointSetNode<Integer> b) {
        DisjointSetNode<Integer> rootA = find(a);
        DisjointSetNode<Integer> rootB = find(b);

        if (rootA != rootB) {
            rootB.parent = rootA;
            rootA.size += rootB.size;
        }
    }

    private void findComponents(int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (disjointSetMap.containsKey(y * width + x)) {
                    DisjointSetNode<Integer> currentNode = disjointSetMap.get(y * width + x);

                    // right
                    if (x + 1 < width && disjointSetMap.containsKey(y * width + (x + 1))) {
                        union(currentNode, disjointSetMap.get(y * width + (x + 1)));
                    }

                    // down
                    if (y + 1 < height && disjointSetMap.containsKey((y + 1) * width + x)) {
                        union(currentNode, disjointSetMap.get((y + 1) * width + x));
                    }

                    // diagonal down right
                    if (x + 1 < width && y + 1 < height && disjointSetMap.containsKey((y + 1) * width + (x + 1))) {
                        union(currentNode, disjointSetMap.get((y + 1) * width + (x + 1)));
                    }

                    // diagonal down left
                    if (x - 1 >= 0 && y + 1 < height && disjointSetMap.containsKey((y + 1) * width + (x - 1))) {
                        union(currentNode, disjointSetMap.get((y + 1) * width + (x - 1)));
                    }
                }
            }
        }

        //find number of distinct components. hashset of unique roots
        Set<DisjointSetNode<Integer>> uniqueComponents = new HashSet<>();
        for (DisjointSetNode<Integer> node : disjointSetMap.values()) {
            uniqueComponents.add(find(node));
        }

        components = uniqueComponents;
        numOfComp = uniqueComponents.size();
        totalNumber.setText("Components: " + numOfComp);
    }

    private void printComponentSizes(int sizeThreshold) {
        //hashmap to store each component size in pixels
        Map<DisjointSetNode<Integer>, Integer> componentSizeMap = new HashMap<>();

        //traverse all nodes and calculate component sizes
        for (DisjointSetNode<Integer> node : disjointSetMap.values()) {
            DisjointSetNode<Integer> root = find(node);
            componentSizeMap.put(root, root.size);
        }

        //print sizes which are larger than the filter the user sets
        for (Map.Entry<DisjointSetNode<Integer>, Integer> entry : componentSizeMap.entrySet()) {
            if (entry.getValue() >= sizeThreshold) {
                System.out.println("Component Size: " + entry.getValue() + " pixels");
            }
        }
    }

    public void drawRect() {
        //check if it has been clicked yet
        if (components.isEmpty()) {
            System.out.println("No components found. Make sure to process the image first.");
            return;
        }

        //get noisereduction variable
        String noiseReductionInput = noiseReductionTextField.getText();
        int noiseReductionThreshold;

        if (noiseReductionInput == null || noiseReductionInput.trim().isEmpty()) {
            System.out.println("Noise reduction threshold is empty. Please enter a valid number.");
            return;
        }

        //make sure its an int
        try {
            noiseReductionThreshold = Integer.parseInt(noiseReductionInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for noise reduction threshold. Please enter a valid integer.");
            return;
        }

        int width = (int) imageView.getImage().getWidth();
        int height = (int) imageView.getImage().getHeight();

        //new writeable created for rectangles
        WritableImage rectImage = new WritableImage(width, height);
        PixelWriter rectPW = rectImage.getPixelWriter();
        PixelReader rectPR = destBW.getPixelReader(); // Use destBW as the base image
        int numOfRectangles = 0;
        //copy original image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rectPW.setColor(x, y, rectPR.getColor(x, y));
            }
        }

        for (DisjointSetNode<Integer> component : components) {
            if (component.size < noiseReductionThreshold) {
                continue;
            }
            numOfRectangles++;

            //bounding box calculation for each valid node. needed for rectangle
            int minX = width, maxX = 0, minY = height, maxY = 0;
            for (DisjointSetNode<Integer> node : disjointSetMap.values()) {
                if (find(node) == component) {
                    int x = node.getX();
                    int y = node.getY();
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }

            //draw the rectangles
            for (int i = minX; i <= maxX; i++) {
                rectPW.setColor(i, minY, Color.RED);
                rectPW.setColor(i, maxY, Color.RED);
            }
            for (int j = minY; j <= maxY; j++) {
                rectPW.setColor(minX, j, Color.RED);
                rectPW.setColor(maxX, j, Color.RED);
            }
        }

        //set 2nd imageview to drawn image
        rectImageView.setImage(rectImage);

        findComponents(width, height);
        totalNumber.setText("Components: " + numOfRectangles);
    }

    //similar function but for drawing numbers
    public void drawTextInRectangles() {
        if (components.isEmpty()) {
            System.out.println("No components found. Make sure to process the image first.");
            return;
        }

        String noiseReductionInput = noiseReductionTextField.getText();
        int noiseReductionThreshold;

        if (noiseReductionInput == null || noiseReductionInput.trim().isEmpty()) {
            System.out.println("Noise reduction threshold is empty. Please enter a valid number.");
            return;
        }

        try {
            noiseReductionThreshold = Integer.parseInt(noiseReductionInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for noise reduction threshold. Please enter a valid integer.");
            return;
        }

        int width = (int) imageView.getImage().getWidth();
        int height = (int) imageView.getImage().getHeight();

        WritableImage numberImage = new WritableImage(width, height);
        PixelWriter numberPW = numberImage.getPixelWriter();
        PixelReader rectPR = destBW.getPixelReader();
        int numOfRectangles = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                numberPW.setColor(x, y, rectPR.getColor(x, y));
            }
        }

        //uses canvas instead.
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.PURPLE);
        gc.setFont(new Font("Arial", 30));

        //ensure component is a list disjointset integer
        List<DisjointSetNode<Integer>> componentList = new ArrayList<>(components);

        //sort components by size (needed for largest-smallest numbering)
        componentList.sort((a, b) -> Integer.compare(b.getSize(), a.getSize()));

        int number = 1;

        //check noise reduction
        for (DisjointSetNode<Integer> component : componentList) {
            if (component.getSize() < noiseReductionThreshold) {
                continue;
            }

            numOfRectangles++;
            //bounding box calculation
            int minX = width, maxX = 0, minY = height, maxY = 0;
            for (DisjointSetNode<Integer> node : disjointSetMap.values()) {
                if (find(node) == component) {
                    int x = node.getX();
                    int y = node.getY();
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }

            //center the text inside the bounding box
            double centerX = minX + (maxX - minX) / 2.0;
            double centerY = minY + (maxY - minY) / 2.0;
            gc.fillText(String.valueOf(number), centerX, centerY);

            number++; //increment for 2nd biggest and so on
        }

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage canvasImage = canvas.snapshot(params, null);
        PixelReader canvasPR = canvasImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (canvasPR.getColor(x, y).getOpacity() > 0) { //only overwrite pixels where text was drawn
                    numberPW.setColor(x, y, canvasPR.getColor(x, y));
                }
            }
        }

        rectImageView.setImage(numberImage);
        totalNumber.setText("Components: " + numOfRectangles);
    }

    //colours disjoint sets random colours. shows grouping etc
    public void randomColorComponents() {
        Image src = imageView.getImage();
        PixelReader pr = src.getPixelReader();
        int width = (int)src.getWidth();
        int height = (int)src.getHeight();
        WritableImage newWI = new WritableImage(width, height);
        PixelWriter writer = newWI.getPixelWriter();
        Random random = new Random();

        //set up empty disjoint set
        int[] disjointSet = new int[width * height];
        Arrays.fill(disjointSet, -1);

        //populate based on disjointset map
        for (Map.Entry<Integer, DisjointSetNode<Integer>> entry : disjointSetMap.entrySet()) {
            int index = entry.getKey();
            DisjointSetNode<Integer> node = entry.getValue();
            if (node.parent != null) {
                disjointSet[index] = node.parent.data;
            } else {
                disjointSet[index] = node.data;
            }
        }

        for (DisjointSetNode<Integer> component : components) {
            //random values
            int r = random.nextInt(255);
            int g = random.nextInt(255);
            int b = random.nextInt(255);
            Color randomColor = Color.rgb(r, g, b);

            //find all pixels of a component and colour them randomly
            for (Map.Entry<Integer, DisjointSetNode<Integer>> entry : disjointSetMap.entrySet()) {
                int index = entry.getKey();
                if (find(disjointSetMap.get(index)) == component) {
                    int x = index % width;
                    int y = index / width;
                    writer.setColor(x, y, randomColor);
                }
            }
        }

        //set up imageview
        imageView.setImage(newWI);
    }

    public void setColorComponentsFromTextFields(TextField RED, TextField GREEN, TextField BLUE) {
        //get text field rgb values
        int r = parseColorComponent(RED.getText());
        int g = parseColorComponent(GREEN.getText());
        int b = parseColorComponent(BLUE.getText());

        //create color variable
        Color color = Color.rgb(r, g, b);

        //setup for imageview and pixelwriter etc
        Image src = imageView.getImage();
        PixelReader pr = src.getPixelReader();
        int width = (int) src.getWidth();
        int height = (int) src.getHeight();
        WritableImage newWI = new WritableImage(width, height);
        PixelWriter writer = newWI.getPixelWriter();

        //same as before
        int[] disjointSet = new int[width * height];
        Arrays.fill(disjointSet, -1);

        for (Map.Entry<Integer, DisjointSetNode<Integer>> entry : disjointSetMap.entrySet()) {
            int index = entry.getKey();
            DisjointSetNode<Integer> node = entry.getValue();
            if (node.parent != null) {
                disjointSet[index] = node.parent.data;
            } else {
                disjointSet[index] = node.data;
            }
        }

        //use specified color instead of random
        for (DisjointSetNode<Integer> component : components) {
            for (Map.Entry<Integer, DisjointSetNode<Integer>> entry : disjointSetMap.entrySet()) {
                int index = entry.getKey();
                if (find(disjointSetMap.get(index)) == component) {
                    int x = index % width;
                    int y = index / width;
                    writer.setColor(x, y, color);
                }
            }
        }

        imageView.setImage(newWI);
    }
    //makes sure colour values are between 255 and 0. if they aren't, they are made to be
    private int parseColorComponent(String text) {
        try {
            int value = Integer.parseInt(text);
            if (value < 0) return 0;
            if (value > 255) return 255;
            return value;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    //same as the text and rectangles functions but combined.
    public void drawRectanglesAndText() {
        if (components.isEmpty()) {
            System.out.println("No components found. Make sure to process the image first.");
            return;
        }
        String noiseReductionInput = noiseReductionTextField.getText();
        int noiseReductionThreshold;

        if (noiseReductionInput == null || noiseReductionInput.trim().isEmpty()) {
            System.out.println("Noise reduction threshold is empty. Please enter a valid number.");
            return;
        }

        try {
            noiseReductionThreshold = Integer.parseInt(noiseReductionInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for noise reduction threshold. Please enter a valid integer.");
            return;
        }

        int width = (int) imageView.getImage().getWidth();
        int height = (int) imageView.getImage().getHeight();

        WritableImage combinedImage = new WritableImage(width, height);
        PixelWriter combinedPW = combinedImage.getPixelWriter();
        PixelReader basePR = destBW.getPixelReader();
        int numOfRectangles = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                combinedPW.setColor(x, y, basePR.getColor(x, y));
            }
        }

        //canvas for text
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.PURPLE);
        gc.setFont(new Font("Arial", 30));

        List<DisjointSetNode<Integer>> componentList = new ArrayList<>(components);

        componentList.sort((a, b) -> Integer.compare(b.getSize(), a.getSize()));

        int number = 1;

        for (DisjointSetNode<Integer> component : componentList) {
            if (component.getSize() < noiseReductionThreshold) {
                continue;
            }

            numOfRectangles++;
            // Calculate the bounding box for this component
            int minX = width, maxX = 0, minY = height, maxY = 0;
            for (DisjointSetNode<Integer> node : disjointSetMap.values()) {
                if (find(node) == component) {
                    int x = node.getX();
                    int y = node.getY();
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }

            //draw rectangle
            for (int i = minX; i <= maxX; i++) {
                combinedPW.setColor(i, minY, Color.RED);
                combinedPW.setColor(i, maxY, Color.RED);
            }
            for (int j = minY; j <= maxY; j++) {
                combinedPW.setColor(minX, j, Color.RED);
                combinedPW.setColor(maxX, j, Color.RED);
            }

            //draw number
            double centerX = minX + (maxX - minX) / 2.0;
            double centerY = minY + (maxY - minY) / 2.0;
            gc.fillText(String.valueOf(number), centerX, centerY);

            number++;
        }

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage canvasImage = canvas.snapshot(params, null);
        PixelReader canvasPR = canvasImage.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (canvasPR.getColor(x, y).getOpacity() > 0) { //only overwrite pixels where text was drawn
                    combinedPW.setColor(x, y, canvasPR.getColor(x, y));
                }
            }
        }

        //set image to the 2nd imageview
        rectImageView.setImage(combinedImage);
        totalNumber.setText("Components: " + numOfRectangles);
    }

    //code for the button function of printcomponentsizes
    public void printComponentSizesButtonAction() {
        String noiseReductionInput = noiseReductionTextField.getText();
        int noiseReductionThreshold;

        if (noiseReductionInput == null || noiseReductionInput.trim().isEmpty()) {
            System.out.println("Noise reduction threshold is empty. Please enter a valid number.");
            return;
        }

        try {
            noiseReductionThreshold = Integer.parseInt(noiseReductionInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for noise reduction threshold. Please enter a valid integer.");
            return;
        }
        printComponentSizes(noiseReductionThreshold);
    }
}

//PCB IMAGE FILE SELECTION 5% !
//BLACK AND WHITE IMAGE CONVERSION AND DISPLAY 10% !
//UNION FIND IMPLEMENTATION 10% !
//ONSCREEN IDENTIFICATION OF COMPONENTS 10% !
//ONSCREEN SEQUENTIAL NUMBERING 10% !
//COUNTING COMPONENTS TOTAL 8% (NOT BY TYPE BUT IN GENERAL) (like 4%) !
//REPORTING SIZE OF COMPONENTS IN PIXELS 7% !
//COLOURING DISJOINT SETS IN BLACK AND WHITE IMAGE 10% !
//IMAGE NOISE REDUCTION AND OUTLIER MANAGEMENT 10% (only did noise reduction so like, 5% maybe) !
//JAVAFX GUI 5% !
//JUNIT TESTING 5% (2 simple tests for find and union 2.5%) !
//GENERAL 5% !

//TOTAL = 83% at most. only need 63% to pass