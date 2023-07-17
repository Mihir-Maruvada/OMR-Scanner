import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
public class mathGrader {
    String csvPath = "C:\\Users\\mihir\\Documents\\CSV\\E25_KEY4.csv";
    static ArrayList <String> key = new ArrayList<>();
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);


        String imagePath = "path/to/your/image.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\RightTiltCropped.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\BottomPhoto.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\E25Reading.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\E25English.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\EnglishBubbled.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\MathBubbled.jpg";
        Mat image = Imgcodecs.imread(imagePath);

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY); //Convert the image to grey scale

        // Apply threshold to obtain binary image
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);//Now apply threshodl to the grey image to get the binary image


        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE); //Now we find all the contours(we will filter them later).


        List<CustomRect> regionRects = new ArrayList<>();//Custom Rect is my custom implementation of the OpenCV rect
        CustomRect overallRect = null;

        for (MatOfPoint contour : contours)
        {
            Rect boundingRect = Imgproc.boundingRect(contour);
            if ((boundingRect.area() > 750) && (boundingRect.area() < 3000)) //Filter out the contours by area
            {
                if (overallRect == null)
                {
                    overallRect = new CustomRect(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height);
                    regionRects.add(overallRect);
                } else
                {
                    CustomRect CustomRect = new CustomRect(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height);
                    overallRect = unionRectangles(overallRect, CustomRect);
                    regionRects.add(CustomRect);
                }
            }
        }

        //Now sort the Rectangles first by y(to get the row) and then by x in that row(to get it in READING ORDER)
        //We will use the y value of the prevous rectangle and the y value of the current rectangle and see if their differece is within a range
        //This range will tell us that those rectangels are in the same row.
        //SEE BELOW
        Collections.sort(regionRects);


        List<List<CustomRect>> groupedRects = new ArrayList<>();
        List<CustomRect> currentGroup = new ArrayList<>();
        int prevY = Integer.MIN_VALUE;

        for (CustomRect rect : regionRects) {
            if (Math.abs(rect.y - prevY) <= 7) //To draw the rectangles by row, we can set a range of the y value(in this case 7)
            {
                currentGroup.add(rect);
            } else {
                Collections.sort(currentGroup, Comparator.comparingInt(rectangle -> rectangle.x));//Sort the current group list based on the x coordinate

                groupedRects.add(currentGroup);

                currentGroup = new ArrayList<>();
                currentGroup.add(rect);
            }

            prevY = rect.y;
        }

        Collections.sort(currentGroup, Comparator.comparingInt(rectangle -> rectangle.x));


        groupedRects.add(currentGroup);

//Now we can flatten the grouped rects list to get the rectangles in their final sorted order
        List<CustomRect> sortedRects = groupedRects.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());



        Mat result = image.clone();
        List<CustomRect> finalRects = new ArrayList<>();
        for (CustomRect rect : sortedRects)
        {
            double filledPercentage = calculateFilledPercentage(rect, binary);
            Scalar color;
            if (filledPercentage >= 0.7) //This means that the rectangle and the bubble inside is filled.
            {
                if (isInsideAnyRectangle(rect, regionRects, false) || rect.width <= rect.height) //Check if this rectangle is inside any rectangle or if its width is less than its height(this is to prevent any rectangle inside rectangle cases and any Question numbers being drawn)
                {
                    continue; //If either of these conditions are met then that means that we shouldn't draw this rectangle
                } else {

                    color = new Scalar(0, 0, 255);//This sets the color to red(According to OpenCV's BGR)
                    finalRects.add(new CustomRect(rect.x, rect.y, rect.width, rect.height, color));
                }
            } else {
                if (isInsideAnyRectangle(rect, regionRects, true) || rect.width <= rect.height) //Check if this rectangle is inside any rectangle or if its width is less than its height(this is to prevent any rectangle inside rectangle cases and any Question numbers being drawn)
                {
                    continue;//If either of these conditions are met then that means that we shouldn't draw this rectangle
                } else {

                    color = new Scalar(255, 0, 0);//This sets the color to blue(ACCORDING TO OpenCV's BGR)
                    finalRects.add(new CustomRect(rect.x, rect.y, rect.width, rect.height, color));
                }
            }

            // Draw the rectangle
            Point topLeft = new Point(rect.x, rect.y);
            Point bottomRight = new Point(rect.x + rect.width, rect.y + rect.height);
            Imgproc.rectangle(result, topLeft, bottomRight, color, 2);

        }
        finalRects.remove(80);
        displayImage(result, "Filled Regions");
        System.out.println(finalRects.size());
        List<CustomRect> temp = new ArrayList<>();
        ArrayList<String> input = new ArrayList<>();
        ArrayList<String> finalInput = new ArrayList<>();
        int startValue = 0;
        int count = 0;
        boolean isBubbled = false;
        //System.out.println(finalRects.size());
        while(count < 60) {
            for (int i = startValue; i < startValue + 5; i++) {
                temp.add(finalRects.get(i)); //Add the set of 4 values to the temp list
                //System.out.println(temp.get(i));
            }
            for (int i = 0; i < temp.size(); i++)
            {
                //  System.out.print(i + " ");
                if(isBubbleFilled(temp.get(i).color) == true)
                {
                    isBubbled = true;
                    // System.out.print(isBubbleFilled(temp.get(i)) + " ");
                    if(i == 0)
                    {
                        input.add("A");
                        //System.out.println(count + 1 + " a");
                    }
                    else if(i == 1)
                    {
                        input.add("B");
                        //System.out.println(count + 1 + " b");
                    }
                    else if(i == 2)
                    {
                        input.add("C");
                        //System.out.println(count + 1+ " c");
                    }
                    else if(i == 3)
                    {
                        input.add("D");
                        //System.out.println(count + 1 + " d");
                    }
                    else if(i == 4)
                    {
                        input.add("E");
                        //System.out.println(count + 1 + " d");
                    }

                }

            }
            if(isBubbled == false)
            {
                input.add(" ");
            }
            isBubbled = false;
            // System.out.println();
            temp.clear();
            count++;
            startValue = startValue+4;
            // System.out.println("Start Value " + startValue);

        }

        ArrayList<Integer> tempList = new ArrayList<Integer>();
        for (int i = 0; i < 60; i++) {
            tempList.add((i+1));
        }
        int inputValue = 0;
        int[][] Reading = new int[10][6];
        System.out.println("INPUT SIZE" + input.size());
        for (int i = 0; i < 10; i++)
        {
            for (int j = 0; j < 6; j++)
            {
                if (shouldSkipSpace(i, j)) {
                    continue;  // Skip to the next iteration
                }
                if(inputValue > 59)
                {
                    break;
                }
                // Access the space and assign a value
                //System.out.println(inputValue);
                Reading[i][j] = tempList.get(inputValue);
                //System.out.println(input.get(inputValue));
                inputValue++;

            }
        }
        for (int j = 0; j < Reading[0].length; j++) {
            for (int i = 0; i < Reading.length; i++) {
                // Check if the space should be skipped
                if (shouldSkipSpace(i, j)) {
                    continue;  // Skip to the next iteration
                }

                // Access the space and print its value
                System.out.println(Reading[i][j]);
                //input.add(Reading[i][j]);
            }
        }
        /*
        inputValue = 0;
        input.clear();
        for (int j = 0; j < Reading[0].length; j++) {
            for (int i = 0; i < Reading.length; i++) {
                // Check if the space should be skipped
                if (shouldSkipSpace(i, j)) {
                    continue;  // Skip to the next iteration
                }

                // Access the space and print its value
                //System.out.println(Reading[i][j]);
                input.add(Reading[i][j]);
            }
        }
        for (int i = 0; i < input.size(); i++) {
            System.out.println((i+1) + input.get(i));
        }
        */

        /*for (int i = 0; i < 75; i++)
        {
            if( !(i % 2 == 0))//It will be odd since ArrayList starts from 0
            {
                if(input.get(i).equals("A"))
                {
                    input.set(i, "F");//Replace all of the odd(second values on paper) with F
                }
                else if(input.get(i).equals("B"))
                {
                    input.set(i, "G");
                }
                else if(input.get(i).equals("C"))
                {
                    input.set(i, "H");
                }
                else if(input.get(i).equals("D"))
                {
                    input.set(i, "J");
                }
            }
        }
        for (int i = 0; i < input.size(); i++) {
            //System.out.println((i+1) + input.get(i));
        }
        generateCSV();
        for (int i = 0; i < key.size(); i++) {

            if(input.get(i).equals(" ")){
                System.out.println("Empty Answer Bubble for #" + (i+1));
                //appendToOutput("Empty Answer Bubble for #" + (i+1));
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
            }
            else if(!(key.get(i).equals(input.get(i))))
            {

                System.out.println("# " + (i+1) + " is incorrect");
                //appendToOutput("# " + (i+1) + " is incorrect");
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
            }
            //System.out.println(key.get(i));
        }
        */
        //System.out.println(key.size());
        /*

        for (int i = 0; i < key.size(); i++) {

            if(input.get(i).equals(" ")){
                System.out.println("Empty Answer Bubble for #" + (i+1));
                //appendToOutput("Empty Answer Bubble for #" + (i+1));
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
            }
            else if(!(key.get(i).equals(input.get(i))))
            {

                System.out.println("# " + (i+1) + " is incorrect");
                //appendToOutput("# " + (i+1) + " is incorrect");
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
            }
            //System.out.println(key.get(i));
        }

        for (int i = 0; i < input.size(); i++) {
            System.out.println((i+1)+input.get(i));
        }

        for (int i = 0; i < 40; i++)
        {
            if( !(i % 2 == 0))//It will be odd since ArrayList starts from 0
            {
                if(finalInput.get(i).equals("A"))
                {
                    finalInput.set(i, "F");//Replace all of the odd(second values on paper) with F
                }
                else if(finalInput.get(i).equals("B"))
                {
                    finalInput.set(i, "G");
                }
                else if(finalInput.get(i).equals("C"))
                {
                    finalInput.set(i, "H");
                }
                else if(finalInput.get(i).equals("D"))
                {
                    finalInput.set(i, "J");
                }
            }
        }
        for (int i = 0; i < input.size(); i++)
        {
           System.out.print((i+1) + " " + input.get(i));
           System.out.println();
        }
         */
        //displayImage(result, "Filled Regions");
    }
    public static boolean shouldSkipSpace(int i, int j)
    {
        if((i == 5 & j == 10)||(i == 5 & j == 11)||(i == 5 & j == 12))
        {
            return true;
        }
        return false;
    }
    public static void generateCSV() {
        String line = "";
        int lineCount = 0; // Counter for the number of lines read

        try {
            BufferedReader b = new BufferedReader(new FileReader("C:\\Users\\mihir\\Documents\\CSV\\E25_KEY5.csv"));
            while ((line = b.readLine()) != null && lineCount < 75) { // Read until 40 lines or end of file
                String[] values = line.split(","); // Takes the values of the CSV File into the array
                if (values.length >= 3) { // Check if the array has at least 3 elements
                    key.add(values[0]); // 2 is the third column which holds the Reading test key
                } else {
                    System.out.println("Invalid line: " + line);
                }
                lineCount++; // Increment the line counter
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public static boolean isBubbleFilled(Scalar color)
    {

        if(color.val[0] == 255.0)
        {
            return false;
        }
        return true;
    }

    private static CustomRect unionRectangles(CustomRect rect1, CustomRect rect2)
    {
        int x = Math.min(rect1.x, rect2.x);
        int y = Math.min(rect1.y, rect2.y);
        int width = Math.max(rect1.x + rect1.width, rect2.x + rect2.width) - x;
        int height = Math.max(rect1.y + rect1.height, rect2.y + rect2.height) - y;
        return new CustomRect(x, y, width, height);
    }

    private static double calculateFilledPercentage(CustomRect rect, Mat binaryImage)
    {
        int totalPixels = rect.width * rect.height;
        int whitePixels = 0;

        for (int y = rect.y; y < rect.y + rect.height; y++) {
            for (int x = rect.x; x < rect.x + rect.width; x++) {
                double[] pixel = binaryImage.get(y, x);
                if (pixel[0] == 255) {
                    whitePixels++;
                }
            }
        }

        return (double) whitePixels / totalPixels;
    }

    private static boolean isInsideAnyRectangle(Rect rect, List<CustomRect> rectangles, boolean sameSize)
    {
        for (CustomRect r : rectangles) {
            if (r.equals(rect)) {
                continue;
            }

            if (r.contains(rect.tl()) && (!sameSize || r.width > rect.width || r.height > rect.height)) {
                return true;
            }
        }

        return false;
    }


    private static void displayImage(Mat image, String title)
    {
        //Scroll pane is added to see the image in case it is too big
        BufferedImage img = matToBufferedImage(image);
        ImageIcon icon = new ImageIcon(img);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);

        JScrollPane scrollPane = new JScrollPane(lbl);
        scrollPane.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(img.getWidth() + 20, img.getHeight() + 20));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static BufferedImage matToBufferedImage(Mat mat)
    {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1)
        {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
}



