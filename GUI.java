import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import org.opencv.core.*;
import java.util.Iterator;



public class GUI
{
    private static JTextArea outputTextArea;
    //static String imagePath = "";
    static String selectedOption = "";
    static String csvPath = "";
    static String[] imagePath = new String[2];
    private static JFrame frame;
    private static JPanel imageCenterPanel;
    public static JButton grade = new JButton("Proceed");
    public static List<CustomRect> engMathRect = new ArrayList<>();
    public static int [] scores = new int [4];
private static JPanel topPanel;
    public static ArrayList<String> colorList = new ArrayList<>();
    private static boolean addRectEnabled = false;
    private static boolean removeRectEnabled = false;
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 400;
    private static final int PAUSE_DURATION_MS = 150;
    public static java.util.List<CustomRect> finalRects = new ArrayList<>();
    public static java.util.List<CustomRect> regionRects = new ArrayList<>();
    //private static JFrame frame;
    private static JPanel imagePanel;

    public static JButton button = new JButton("TOGGLE REMOVE RECT");

    private static Mat image;
    private static Mat binary;
    private static Mat result;
    public static double filledPercentage;
    public static java.util.List<java.util.List<CustomRect>> groupedRects = new ArrayList<>();
    public static java.util.List<CustomRect> currentGroup = new ArrayList<>();
    public static List<CustomRect> sortedRects = new ArrayList<>();

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);//Need this to access/load OpenCV Library

        String uploadIconPath = "C:\\Users\\mihir\\Downloads\\uploadIcon2.png";
        //String uploadIconPath2 = "C:\\Users\\mihir\\Downloads\\uploadIcon2.png";
        imagePath[0] = "";//ENGLISH/MATH IMAGE PATH
        imagePath[1] = "";//READING/SCIENCE IMAGE PATH
        // Create the main JFrame
        frame = new JFrame("GUI Example");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create a panel for the top section
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        // Create and resize the image label
        ImageIcon image = new ImageIcon(uploadIconPath);
        Image scaledImage = image.getImage().getScaledInstance(100, -1, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel imageLabel = new JLabel(scaledIcon);

        // Create a centering panel for the image label
        imageCenterPanel = new JPanel(new GridBagLayout());
        imageCenterPanel.add(imageLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // Column index of the cell in which the component will be placed
        gbc.gridy = 0; // Row index of the cell in which the component will be placed
        gbc.insets = new Insets(0, 0, 0, 200); // Adjust the left inset to move the image to the left

        imageCenterPanel.add(imageLabel, gbc);
        // Add the image centering panel to the top panel
        topPanel.add(imageCenterPanel, BorderLayout.CENTER);

        // Add a mouse listener to the image label
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose a file");

                int userSelection = fileChooser.showOpenDialog(frame);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                    //imagePath = filePath;
                    imagePath[0] = filePath.replace("\\", "\\\\");;

                    appendToOutput("Selected file path: " + imagePath[0]);
                }
            }
        });

        ImageIcon image2 = new ImageIcon(uploadIconPath);
        Image scaledImage2 = image2.getImage().getScaledInstance(100, -1, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon2 = new ImageIcon(scaledImage2);
        JLabel imageLabel2 = new JLabel(scaledIcon2);

        // Create a centering panel for the image label
        //JPanel imageCenterPanel2 = new JPanel(new GridBagLayout());
        imageCenterPanel.add(imageLabel2);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc.gridx = 0; // Column index of the cell in which the component will be placed
        gbc.gridy = 0; // Row index of the cell in which the component will be placed
        gbc.insets = new Insets(0, 200, 0,0 ); // Adjust the left inset to move the image to the left

        imageCenterPanel.add(imageLabel2, gbc);
        // Add the image centering panel to the top panel
        topPanel.add(imageCenterPanel, BorderLayout.CENTER);

        // Add a mouse listener to the image label
        imageLabel2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose a file");

                int userSelection = fileChooser.showOpenDialog(frame);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                    //imagePath = filePath;
                    imagePath[1] = filePath.replace("\\", "\\\\");;

                    appendToOutput("Selected file path: " + imagePath[1]);
                }
            }
        });

        // Create a panel for the dropdown menu
        JPanel dropdownPanel = new JPanel();
        dropdownPanel.setLayout(new FlowLayout());

        // Create the dropdown menu
        String[] options = {"E25", "Option 2", "Option 3"};
        JComboBox<String> dropdown = new JComboBox<>(options);
        dropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedOption = (String) dropdown.getSelectedItem();
                if(selectedOption.equals("E25"))
                {
                    csvPath = "C:\\Users\\mihir\\Documents\\CSV\\E25_KEY5.csv";
                }
                appendToOutput("Selected option: " + selectedOption);
            }
        });

        // Add the dropdown menu to the dropdown panel
        dropdownPanel.add(dropdown);

        // Create a panel for the grade button
        JPanel gradeButtonPanel = new JPanel();
        gradeButtonPanel.setLayout(new BorderLayout());

        // Create the grade button
        JButton gradeButton = new JButton("GRADE");
        gradeButton.setPreferredSize(new Dimension(100, 50));
        gradeButton.setBackground(Color.GREEN);
        gradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Perform grading action here
                /*
                appendToOutput("Grade button clicked");
                if(selectedOption.equals(""))
                {
                    appendToOutput("PLEASE SELECT A TEST");
                }

                if(imagePath[0].equals(""))
                {
                    appendToOutput("PLEASE INPUT ENGLISH/MATH IMAGE");
                }

                if(imagePath[1].equals(""))
                {
                    appendToOutput("PLEASE INPUT READING/SCIENCE IMAGE");
                }

                if(!(imagePath[0].equals("")) && !(imagePath[1].equals("")) && !(selectedOption.equals("")))//MEANS THAT ALL INPUTS ARE AVAILAB
                {
                    //RectDraw engMath = new RectDraw(imagePath[0]);
                    engMathGUI();
                }

                 */
                //imagePath[0] = "C:\\Users\\mihir\\Desktop\\English_and_Math.jpg";
                //imagePath[1] = "C:\\Users\\mihir\\Desktop\\Reading_and_Science.jpg";
                //csvPath = "C:\\Users\\mihir\\Documents\\CSV\\E25_KEY5.csv";
                engMathGUI();
            }
        });

        // Add the grade button to the grade button panel
        gradeButtonPanel.add(gradeButton, BorderLayout.CENTER);

        // Create a panel for the bottom section
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        // Create a panel for the output area
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BorderLayout());

        // Create a scrollable text area for output
        outputTextArea = new JTextArea(10, 40);
        outputTextArea.setEditable(false);
        outputTextArea.setFont(new Font("Arial", Font.PLAIN, 16)); // Set the desired font size
        JScrollPane scrollPane = new JScrollPane(outputTextArea);

        // Add the scroll pane to the output panel
        outputPanel.add(scrollPane, BorderLayout.CENTER);

        // Add the dropdown panel to the bottom panel
        bottomPanel.add(dropdownPanel, BorderLayout.NORTH);

        // Add the grade button panel to the bottom panel
        bottomPanel.add(gradeButtonPanel, BorderLayout.CENTER);

        // Add the output panel to the bottom panel
        bottomPanel.add(outputPanel, BorderLayout.SOUTH);

        // Add the top panel to the main frame
        frame.add(topPanel, BorderLayout.NORTH);

        // Add the bottom panel to the main frame
        frame.add(bottomPanel, BorderLayout.CENTER);
        frame.setResizable(false);
        // Set the frame to be visible
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        String message = "Welcome to the program!\n\nPlease follow the instructions:\n1. Click the upload image to select a file.\n2. Choose an option from the drop-down menu.\n3. Click the 'GRADE' button to proceed.\n\nEnjoy using the program!";
        JOptionPane.showMessageDialog(null, message, "Instructions", JOptionPane.INFORMATION_MESSAGE);
        //JOptionPane.setLocationRelativeTo(null);
    }

    public static void engMathGUI()
    {
        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();
        String path = imagePath[0];
        //System.out.println(imagePath[0]);

        image = Imgcodecs.imread(path);

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

        //frame = new JFrame();
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
        //int count = 0;
        for (CustomRect rect : sortedRects) {
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

            org.opencv.core.Point topLeft = new org.opencv.core.Point(rect.x, rect.y);
            org.opencv.core.Point bottomRight = new org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height);
            Imgproc.rectangle(result, topLeft, bottomRight, color, 2);
        }
            //displayImage(result);LAST USAGE(IN CASE NEED TO UNCOMMENT)

            //The code below pauses for a short duration so that we can see the rectangles being drawn step by step
/*
            try
            {
                //Thread.sleep(PAUSE_DURATION_MS);
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
*/

            /*
            count++;
            if(count % 5 == 0)
            {
                //System.out.println();
            }

             */


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
        //System.out.println("OUTSIDE KEY PRESS LISTENER");
        /*
        final char[] c = new char[1];
        imagePanel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyChar());
                System.out.println("INSIDE KEY PRESSED LISTENER");
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
         */
        displayImage(result, false);
        //frame.setResizable(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sort(finalRects, true);
                //Grader eng = new Grader(finalRects, "eng");

                //Grader read = new Grader(finalRects, "read");
                //Grader math = new Grader(finalRects, "math");
                //print();
                toggleRemoveRect();
            }
        });

        grade.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sort(finalRects, true);
                //Grader eng = new Grader(finalRects, "eng");
                for (int i = 0; i < finalRects.size(); i++) {
                    engMathRect.add(finalRects.get(i));
                }
                readSciGUI();
                //Grader read = new Grader(finalRects, "read");
                //Grader engMathObj = new Grader(finalRects, "engMath", csvPath);
                //Grader readSciObj = new Grader(finalRects, "readSci", csvPath);
                //print();
                //toggleRemoveRect();
            }
        });
    }
    public static void readSciGUI()
    {
        finalRects.clear();
        regionRects.clear();
        colorList.clear();
        groupedRects.clear();
        currentGroup.clear();
        sortedRects.clear();
        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();

        String path = imagePath[1];
        //System.out.println(imagePath[0]);
        image = Imgcodecs.imread(path);

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

        //frame = new JFrame();
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
        //int count = 0;
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

            org.opencv.core.Point topLeft = new org.opencv.core.Point(rect.x, rect.y);
            org.opencv.core.Point bottomRight = new org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height);
            Imgproc.rectangle(result, topLeft, bottomRight, color, 2);

            //displayImage(result);LAST USAGE(IN CASE NEED TO UNCOMMENT)

            //The code below pauses for a short duration so that we can see the rectangles being drawn step by step
/*
            try
            {
                //Thread.sleep(PAUSE_DURATION_MS);
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
*/

            /*
            count++;
            if(count % 5 == 0)
            {
                //System.out.println();
            }

             */
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
        //System.out.println("OUTSIDE KEY PRESS LISTENER");
        /*
        final char[] c = new char[1];
        imagePanel.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyChar());
                System.out.println("INSIDE KEY PRESSED LISTENER");
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
         */
        displayImage(result, true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sort(finalRects, true);
                //Grader eng = new Grader(finalRects, "eng");

                //Grader read = new Grader(finalRects, "read");
                //Grader math = new Grader(finalRects, "math");
                //print();
                toggleRemoveRect();
            }
        });

        grade.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sort(finalRects, true);
                //Grader eng = new Grader(finalRects, "eng");
                frame.getContentPane().removeAll();
                frame.revalidate();
                frame.repaint();
                /*
                outputTextArea = new JTextArea(10, 40);
                outputTextArea.setEditable(false);
                outputTextArea.setFont(new Font("Arial", Font.PLAIN, 16)); // Set the desired font size
                */
                frame.setSize(600, 400);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                frame.setResizable(false);
                // Set the frame to be visible
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
                // Create a panel for the top section
                topPanel = new JPanel();
                topPanel.setLayout(new BorderLayout());
                JPanel gradeButtonPanel = new JPanel();
                gradeButtonPanel.setLayout(new BorderLayout());
                //ImageIcon image = new ImageIcon("C:\\Users\\mihir\\Desktop\\shareIcon2.png");
               // JLabel imageLabel = new JLabel(image);
                //topPanel.add(imageLabel);
                // Create the grade button
                JButton gradeButton = new JButton("GRADE");
                gradeButton.setPreferredSize(new Dimension(100, 50));
                gradeButton.setBackground(Color.GREEN);
// Add the grade button to the grade button panel
                gradeButtonPanel.add(gradeButton, BorderLayout.CENTER);

                // Create a panel for the bottom section
                JPanel bottomPanel = new JPanel();
                bottomPanel.setLayout(new BorderLayout());

                // Create a panel for the output area
                JPanel outputPanel = new JPanel();
                outputPanel.setLayout(new BorderLayout());

                // Create a scrollable text area for output
                outputTextArea = new JTextArea(10, 40);
                outputTextArea.setEditable(false);
                outputTextArea.setFont(new Font("Arial", Font.PLAIN, 16)); // Set the desired font size
                JScrollPane scrollPane = new JScrollPane(outputTextArea);

                // Add the scroll pane to the output panel
                outputPanel.add(scrollPane, BorderLayout.CENTER);

                // Add the dropdown panel to the bottom panel
               // bottomPanel.add(dropdownPanel, BorderLayout.NORTH);

                // Add the grade button panel to the bottom panel
                //bottomPanel.add(gradeButtonPanel, BorderLayout.CENTER);

                // Add the output panel to the bottom panel
                //bottomPanel.add(outputPanel, BorderLayout.SOUTH);
// Add the grade button panel to the bottom panel
                bottomPanel.add(gradeButtonPanel, BorderLayout.CENTER);

// Add the output panel to the bottom panel
                bottomPanel.add(outputPanel, BorderLayout.SOUTH);
                //frame.add(topPanel);
                frame.add(bottomPanel);
                //frame.add(gradeButtonPanel);

                // Add the top panel to the main frame
                //frame.add(topPanel, BorderLayout.NORTH);
                //Grader read = new Grader(finalRects, "read");
                Grader engMathObj = new Grader(engMathRect, "engMath", csvPath);
                Grader readSciObj = new Grader(finalRects, "readSci", csvPath);
                dispScore();
                //print();
                //toggleRemoveRect();

            }
        });
    }
//public JTextArea scoreReport = new JTextArea();
    public static void dispScore()
    {
        double totalScore = scores[0] + scores[1] +scores[2] +scores[3];
        totalScore = totalScore/4;
        totalScore = Math.round(totalScore);
        //outputTextArea.replaceSelection("");
        appendToOutput("FINAL SCALED SCORE " + totalScore);
        //frame.add(outputTextArea, BorderLayout.CENTER);
        /*
        frame.setResizable(false);
        frame.setSize(600,400);
        // Set the frame to be visible
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);\

         */
        //System.out.println(totalScore);
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

    private static void displayImage(Mat image, boolean b)
    {
        BufferedImage img = matToBufferedImage(image);
        ImageIcon icon = new ImageIcon(img);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        if(b)
        {
            grade = new JButton("GRADE 2");
            button = new JButton("TOGGLE RECT REMOVE");
        }
         grade.setBounds(400, 300, 95, 30);
        button.setBounds(400,500,95,30);
        imagePanel.removeAll();
        imagePanel.add(lbl);
        imagePanel.add(button);
        imagePanel.add(grade);
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
        org.opencv.core.Point topLeft = new org.opencv.core.Point(newRect.x, newRect.y);
        org.opencv.core.Point bottomRight = new org.opencv.core.Point(newRect.x + newRect.width, newRect.y + newRect.height);
        Imgproc.rectangle(result, topLeft, bottomRight, color, 2);

        //displayImage(result);

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

        displayImage(result, false);
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

            org.opencv.core.Point topLeft = new org.opencv.core.Point(rect.x, rect.y);
            org.opencv.core.Point bottomRight = new org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height);
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
    public static void appendToOutput (String text){
        outputTextArea.append(text + "\n");
        outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());
    }
}
