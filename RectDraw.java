import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.event.*;
import java.sql.SQLOutput;
import java.util.Iterator;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class RectDraw
{
    //public static List<Scalar> color = new ArrayList<>();
    public static ArrayList<String> colorList = new ArrayList<>();
    private static boolean addRectEnabled = false;
    private static boolean removeRectEnabled = false;
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private static final int PAUSE_DURATION_MS = 150;
    public static List<CustomRect> finalRects = new ArrayList<>();
    public static List<CustomRect> regionRects = new ArrayList<>();
    private static JFrame frame;
    private static JPanel imagePanel;

    public static JButton button = new JButton("GRADE");

    private static Mat image;
    private static Mat binary;
    private static Mat result;
   public static double filledPercentage;
    public static List<List<CustomRect>> groupedRects = new ArrayList<>();
    public static List<CustomRect> currentGroup = new ArrayList<>();
    public static List<CustomRect> sortedRects = new ArrayList<>();
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);//Need this to access/load OpenCV Library

        String imagePath = "path/to/your/image.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\BottomPhoto.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\MathBubbled.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\E25English.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\EnglishBubbled.jpg";
        //imagePath = "C:\\Users\\mihir\\Desktop\\E25Reading.jpg";
        //imagePath = "C:\\Users\\mihir\\Desktop\\E25Math.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\E25English2.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\E25English3.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\E25Math2.jpg";
         image = Imgcodecs.imread(imagePath);

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY); //Convert the image to grey scale

         binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);//Now apply threshodl to the grey image to get the binary image

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);//Now we find all the contours(we will filter them later).

       //Custom Rect is my custom implementation of the OpenCV rect
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
        sort(regionRects, false);

        //Grader read = new Grader(color, "read");
        //Now we can draw these sorted rectangles

        frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setTitle("Filled Regions");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));


        JScrollPane scrollPane = new JScrollPane();
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);


        imagePanel = new JPanel();
        imagePanel.setLayout(new FlowLayout());
        scrollPane.setViewportView(imagePanel);


        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Draw rectangles based on the sortedRects list
        result = image.clone();
         finalRects = new ArrayList<>();
         int count = 0;
        for (CustomRect rect : sortedRects)
        {
            // Calculate filled percentage
            double filledPercentage = calculateFilledPercentage(rect, binary);

            // Adjust color and check nesting based on filled percentage and length condition
            Scalar color;
            if (filledPercentage >= 0.7) //This means that the rectangle and the bubble inside is filled.
            {
                // Check if the rectangle is inside another rectangle and satisfies length condition
                if (isInsideAnyRectangle(rect, finalRects, false) || rect.width <= rect.height) //Check if this rectangle is inside any rectangle or if its width is less than its height(this is to prevent any rectangle inside rectangle cases and any Question numbers being drawn)
                {
                    // sortedRects.remove(sortedRects.indexOf(rect));
                    continue;//If either of these conditions are met then that means that we shouldn't draw this rectangle
                } else {
                    //finalRects.add(rect);
                    color = new Scalar(0, 0, 255);//This sets the color to red(According to OpenCV's BGR)
                    CustomRect colorRect = new CustomRect(rect.x, rect.y, rect.width, rect.height, color);
                    finalRects.add(colorRect);
                    //colorList.add("RED");
                    //System.out.print("RED ");
                }
            } else {
                // Check if the rectangle is inside another rectangle and satisfies length condition
                if (isInsideAnyRectangle(rect, finalRects, true) || rect.width <= rect.height) //Check if this rectangle is inside any rectangle or if its width is less than its height(this is to prevent any rectangle inside rectangle cases and any Question numbers being drawn)
                {
                    //  sortedRects.remove(sortedRects.indexOf(rect));
                    continue;//If either of these conditions are met then that means that we shouldn't draw this rectangle
                } else {
                    //finalRects.add(rect);
                    color = new Scalar(255, 0, 0);//This sets the color to red(According to OpenCV's BGR)
                    CustomRect colorRect = new CustomRect(rect.x, rect.y, rect.width, rect.height, color);
                    finalRects.add(colorRect);
                    //colorList.add("BLUE");
                    //System.out.print("BLUE ");
                }
            }

            Point topLeft = new Point(rect.x, rect.y);
            Point bottomRight = new Point(rect.x + rect.width, rect.y + rect.height);
            Imgproc.rectangle(result, topLeft, bottomRight, color, 2);

            displayImage(result);

            //The code below pauses for a short duration so that we can see the rectangles being drawn step by step

            try
            {
                //Thread.sleep(PAUSE_DURATION_MS);
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            count++;
            if(count % 5 == 0)
            {
                //System.out.println();
            }
        }

        /*
        //SEE if the colors are correct.
        for (int i = 0; i < finalRects.size(); i++)
        {
            if((i % 4 == 0) && !(i == 0))
            {
                System.out.println();
            }
            if(finalRects.get(i).color.val[0] == 255)//THIS MEANS THAT THE RECTANGLE IS BLUE
            {
                System.out.print("BLUE ");
            }
            else{
                System.out.print("RED ");
            }

        }
        */

        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 'A' || e.getKeyChar() == 'a') {
                    toggleAddRect();
                } else if (e.getKeyChar() == 'R' || e.getKeyChar() == 'r') {
                    toggleRemoveRect();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
       // System.out.println("FINAL RECTS SIZE" + finalRects.size());
        //System.out.println("COLOR LIST SIZE" + colorList.size());a
        for (int i = 0; i < colorList.size(); i++) {
          // System.out.println(colorList.get(i));
        }


        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sort(finalRects, true);
                //Grader eng = new Grader(finalRects, "eng");

                //Grader read = new Grader(finalRects, "read");
                Grader math = new Grader(finalRects, "math");
                //print();
            }
        });
    }
    public static void print()
    {
        int count = 0;
        /*
        List<CustomRect> copyFinalRects = new ArrayList<>();
        for (int i = 0; i < finalRects.size() ; i++) {
            copyFinalRects.add(finalRects.get(i));
        }

       finalRects.clear();
       *
         */
        for (CustomRect rect :finalRects)
        {
            // Calculate filled percentage
            double filledPercentage = calculateFilledPercentage(rect, binary);

            // Adjust color and check nesting based on filled percentage and length condition
            Scalar color;
            if (filledPercentage >= 0.7) //This means that the rectangle and the bubble inside is filled.
            {
                // Check if the rectangle is inside another rectangle and satisfies length condition
                if (isInsideAnyRectangle(rect, finalRects, false) || rect.width <= rect.height) //Check if this rectangle is inside any rectangle or if its width is less than its height(this is to prevent any rectangle inside rectangle cases and any Question numbers being drawn)
                {
                    // sortedRects.remove(sortedRects.indexOf(rect));
                    continue;//If either of these conditions are met then that means that we shouldn't draw this rectangle
                } else {
                    //finalRects.add(rect);
                    color = new Scalar(0, 0, 255);//This sets the color to red(According to OpenCV's BGR)
                    CustomRect colorRect = new CustomRect(rect.x, rect.y, rect.width, rect.height, color);
                    //finalRects.add(colorRect);
                    //colorList.add("RED");
                    System.out.print("RED ");
                }
            } else {
                // Check if the rectangle is inside another rectangle and satisfies length condition
                if (isInsideAnyRectangle(rect, finalRects, true) || rect.width <= rect.height) //Check if this rectangle is inside any rectangle or if its width is less than its height(this is to prevent any rectangle inside rectangle cases and any Question numbers being drawn)
                {
                    //  sortedRects.remove(sortedRects.indexOf(rect));
                    continue;//If either of these conditions are met then that means that we shouldn't draw this rectangle
                } else {
                    //finalRects.add(rect);
                    color = new Scalar(255, 0, 0);//This sets the color to red(According to OpenCV's BGR)
                    CustomRect colorRect = new CustomRect(rect.x, rect.y, rect.width, rect.height, color);
                    //finalRects.add(colorRect);
                    //colorList.add("BLUE");
                    //System.out.print("BLUE ");
                }
            }

            Point topLeft = new Point(rect.x, rect.y);
            Point bottomRight = new Point(rect.x + rect.width, rect.y + rect.height);
            Imgproc.rectangle(result, topLeft, bottomRight, color, 2);

            displayImage(result);

            //The code below pauses for a short duration so that we can see the rectangles being drawn step by step

            try
            {
                //Thread.sleep(PAUSE_DURATION_MS);
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            count++;
            if(count % 4 == 0)
            {
                //System.out.println();
            }
        }
    }
    private static void toggleAddRect() {
        addRectEnabled = !addRectEnabled;
        System.out.println("Add Rectangle: " + (addRectEnabled ? "Enabled" : "Disabled"));
    }

    private static void toggleRemoveRect() {
        removeRectEnabled = !removeRectEnabled;
        System.out.println("Remove Rectangle: " + (removeRectEnabled ? "Enabled" : "Disabled"));
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

        for (int y = rect.y; y < rect.y + rect.height; y++)
        {
            for (int x = rect.x; x < rect.x + rect.width; x++)
            {
                double[] pixel = binaryImage.get(y, x);
                if (pixel[0] == 255)
                {
                    whitePixels++;
                }
            }
        }

        return (double) whitePixels / totalPixels;
    }

    private static boolean isInsideAnyRectangle(Rect rect, List<CustomRect> rectangles, boolean sameSize)
    {
        for (CustomRect r : rectangles)
        {
            if (r.equals(rect))
            {
                continue;
            }

            if (r.contains(rect.tl()) && (!sameSize || r.width > rect.width || r.height > rect.height))
            {
                return true;
            }
        }

        return false;
    }
public static void sort(List<CustomRect> rectList, boolean b)
{
   // System.out.println("IN SORT METHOD");
    Collections.sort(rectList);
//color.clear();
    colorList.clear();
    groupedRects.clear();
    currentGroup.clear();
    sortedRects.clear();
    int prevY = Integer.MIN_VALUE;

    for (CustomRect rect : rectList)
    {
        if (Math.abs(rect.y - prevY) <= 7) //To draw the rectangles by row, we can set a range of the y value(in this case 7)
        {

            currentGroup.add(rect);
        } else {

            Collections.sort(currentGroup, Comparator.comparingInt(rectangle -> rectangle.x));//Sort the current group list based on the x coordinate


            groupedRects.add(currentGroup);
            //System.out.println(currentGroup.size());

            currentGroup = new ArrayList<>();
            currentGroup.add(rect);
        }

        prevY = rect.y;
    }


    Collections.sort(currentGroup, Comparator.comparingInt(rectangle -> rectangle.x));


    groupedRects.add(currentGroup);

    //Now we can flatten the grouped rects list to get the rectangles in their final sorted order
    sortedRects = groupedRects.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    if(b) {
        finalRects.clear();
        for (int i = 0; i < sortedRects.size(); i++)
        {
            finalRects.add(sortedRects.get(i));
        }
    }
    /*
    if(b)//This should only be done in the add rectangle method(so final rect will be edited here)
    {
        finalRects.clear();
        finalRects.addAll(sortedRects);
       //System.out.println(finalRects.size());
        for (int i = 0; i < finalRects.size(); i++) {
            filledPercentage = calculateFilledPercentage(finalRects.get(i), binary);
            System.out.println("FILLED PERCENTAGE" + filledPercentage);
            if (filledPercentage >= 0.7) {
                // color.add(new Scalar(0, 0, 255)); // Red color
                colorList.add("RED");
            } else {
                //color.add(new Scalar(255, 0, 0)); // Blue color\
                colorList.add("BLUE");
            }
        }
    }
    */
        for (int i = 0; i < sortedRects.size(); i++) {
            filledPercentage = calculateFilledPercentage(sortedRects.get(i), binary);
            //System.out.println("FILLED PERCENTAGE" + filledPercentage);
            if (filledPercentage >= 0.7) {
                // color.add(new Scalar(0, 0, 255)); // Red color
                colorList.add("RED");
                //System.out.println("RED");
            } else {
                //color.add(new Scalar(255, 0, 0)); // Blue color\
                colorList.add("BLUE");
               // System.out.println("BLUE");
            }
        }

    //System.out.println(color.size());
}

    private static void displayImage(Mat image)
    {
        BufferedImage img = matToBufferedImage(image);
        ImageIcon icon = new ImageIcon(img);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        button.setBounds(400,500,95,30);
        imagePanel.removeAll();
        imagePanel.add(lbl);
        imagePanel.add(button);
        frame.revalidate();
        frame.repaint();
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                java.awt.Point clickPoint = e.getPoint();
                System.out.println(clickPoint);
                if (addRectEnabled) {

                    addRectangle(clickPoint);
                } else if (removeRectEnabled) {

                  //  System.out.println(clickPoint);
                    removeRectangle(clickPoint);
                }
                //clickPoint.setLocation(0,0);
            }
        });
    }
    private static void addRectangle(java.awt.Point c2Point) {
        int avgWidth = 0;
        int avgHeight = 0;
        for (CustomRect rect : finalRects) {
            avgWidth += rect.width;
            avgHeight += rect.height;
        }
        avgWidth /= finalRects.size();
        avgHeight /= finalRects.size();

        int rectangleWidth = avgWidth;
        int rectangleHeight = avgHeight;
        int rectangleX = (int) (c2Point.getX() - rectangleWidth / 2);
        int rectangleY = (int) (c2Point.getY() - rectangleHeight / 2);

        CustomRect newRect = new CustomRect(rectangleX, rectangleY, rectangleWidth, rectangleHeight);

        if (isInsideAnyRectangle(newRect, finalRects, true)) {
            return;
        }

        filledPercentage = calculateFilledPercentage(newRect, binary);

        Scalar color;
        if (filledPercentage >= 0.7) {
            colorList.add("RED");
            color = new Scalar(0, 0, 255); // Red color
        } else {
            colorList.add("BLUE");
            color = new Scalar(255, 0, 0); // Blue color
        }
        newRect = new CustomRect(rectangleX, rectangleY, rectangleWidth, rectangleHeight, color);
        finalRects.add(newRect);

        // result = image.clone();
        //drawRectangles();
        //sort(finalRects, true);
        Point topLeft = new Point(newRect.x, newRect.y);
        Point bottomRight = new Point(newRect.x + newRect.width, newRect.y + newRect.height);
        Imgproc.rectangle(result, topLeft, bottomRight, color, 2);

        displayImage(result);
        
        //drawRectangles();
        //displayImage(result);

        /*
        result = image.clone();
        displayImage(result);
        for (CustomRect rect : finalRects)
        {
            // Calculate filled percentage
            double filledPercentage = calculateFilledPercentage(rect, binary);

            // Adjust color and check nesting based on filled percentage and length condition
            color = new Scalar(0, 0, 0);
            if (filledPercentage >= 0.7) //This means that the rectangle and the bubble inside is filled.
            {
                // Check if the rectangle is inside another rectangle and satisfies length condition
                if (isInsideAnyRectangle(rect, finalRects, false) || rect.width <= rect.height) //Check if this rectangle is inside any rectangle or if its width is less than its height(this is to prevent any rectangle inside rectangle cases and any Question numbers being drawn)
                {
                    // sortedRects.remove(sortedRects.indexOf(rect));
                    continue;//If either of these conditions are met then that means that we shouldn't draw this rectangle
                } else {
                    //finalRects.add(rect);
                    color = new Scalar(0, 0, 255);//This sets the color to red(According to OpenCV's BGR)
                }
            } else {
                // Check if the rectangle is inside another rectangle and satisfies length condition
                if (isInsideAnyRectangle(rect, finalRects, true) || rect.width <= rect.height) //Check if this rectangle is inside any rectangle or if its width is less than its height(this is to prevent any rectangle inside rectangle cases and any Question numbers being drawn)
                {
                    //  sortedRects.remove(sortedRects.indexOf(rect));
                    continue;//If either of these conditions are met then that means that we shouldn't draw this rectangle
                } else {
                    //finalRects.add(rect);
                    color = new Scalar(255, 0, 0);//This sets the color to red(According to OpenCV's BGR)
                }
            }

            Point topLeft = new Point(rect.x, rect.y);
            Point bottomRight = new Point(rect.x + rect.width, rect.y + rect.height);
            Imgproc.rectangle(result, topLeft, bottomRight, color, 2);

            displayImage(image);

            //The code below pauses for a short duration so that we can see the rectangles being drawn step by step
            try
            {
                Thread.sleep(PAUSE_DURATION_MS);
                //Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //displayImage(image);
        }
        */
    }


    /*private static void removeRectangle(java.awt.Point cPoint) {
        org.opencv.core.Point opencvClickPoint = new org.opencv.core.Point(cPoint.getX(), cPoint.getY());

        //Iterator<CustomRect> iterator = finalRects.iterator();
        for (int i = 0; i < finalRects.size(); i++) {
            finalRectsClone.add(finalRects.get(i));
        }

        for (CustomRect rect : finalRectsClone) {
            //CustomRect rect = iterator.next();
            if (rect.contains(opencvClickPoint)) {
                finalRectsClone.remove(finalRectsClone.indexOf(rect));
                //break;
            }
        }

    finalRects.clear();
        for (int i = 0; i < finalRects.size(); i++) {
            finalRects.add(finalRectsClone.get(i));
        }

        result = image.clone();
        for (CustomRect rect : finalRects) {
           filledPercentage = calculateFilledPercentage(rect, binary);

            Scalar color;
            if (filledPercentage >= 0.7) {
                color = new Scalar(0, 0, 255); // Red color
            } else {
                color = new Scalar(255, 0, 0); // Blue color
            }
            Point topLeft = new Point(rect.x, rect.y);
            Point bottomRight = new Point(rect.x + rect.width, rect.y + rect.height);
            Imgproc.rectangle(result, topLeft, bottomRight, color, 2);
        }
        //drawRectangles();

        displayImage(result);
        finalRectsClone.clear();
    }
     */
    private static void removeRectangle(java.awt.Point clickPoint) {
        org.opencv.core.Point opencvClickPoint = new org.opencv.core.Point(clickPoint.getX(), clickPoint.getY());

        Iterator<CustomRect> iterator = finalRects.iterator();
        while (iterator.hasNext()) {
            CustomRect rect = iterator.next();
            if (rect.contains(opencvClickPoint)) {
                iterator.remove();
                break;
            }
        }

        result = image.clone();
        drawRectangles();

        displayImage(result);
    }
    private static void drawRectangles() {
        //result = image.clone();
        for (CustomRect rect : finalRects) {
            double filledPercentage = calculateFilledPercentage(rect, binary);

            Scalar color;
            if (filledPercentage >= 0.7) {
                if (isInsideAnyRectangle(rect, finalRects, false) || rect.width <= rect.height) {
                    continue;
                } else {
                    color = new Scalar(0, 0, 255);
                }
            } else {
                if (isInsideAnyRectangle(rect, finalRects, true) || rect.width <= rect.height) {
                    continue;
                } else {
                    color = new Scalar(255, 0, 0);
                }
            }

            Point topLeft = new Point(rect.x, rect.y);
            Point bottomRight = new Point(rect.x + rect.width, rect.y + rect.height);
            Imgproc.rectangle(result, topLeft, bottomRight, color, 2);
        }
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

