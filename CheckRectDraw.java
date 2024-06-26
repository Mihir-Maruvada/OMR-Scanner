import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CheckRectDraw
{
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private static final int PAUSE_DURATION_MS = 0;

    private static JFrame frame;
    private static JPanel imagePanel;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);//Need this to access/load OpenCV Library

        String imagePath = "path/to/your/image.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\BottomPhoto.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\MathBubbled.jpg";
        Mat image = Imgcodecs.imread(imagePath);

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY); //Convert the image to grey scale

        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);//Now apply threshodl to the grey image to get the binary image

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);//Now we find all the contours(we will filter them later).

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

        for (CustomRect rect : regionRects)
        {
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
        Mat result = image.clone();
        List<CustomRect> finalRects = new ArrayList<>();
        for (CustomRect rect : sortedRects)
        {
            // Calculate filled percentage
            double filledPercentage = calculateFilledPercentage(rect, binary);

            // Adjust color and check nesting based on filled percentage and length condition
            Scalar color;
            if (filledPercentage >= 0.7) //This means that the rectangle and the bubble inside is filled.
            {
                // Check if the rectangle is inside another rectangle and satisfies length condition
                if (isInsideAnyRectangle(rect, regionRects, false) || rect.width <= rect.height) //Check if this rectangle is inside any rectangle or if its width is less than its height(this is to prevent any rectangle inside rectangle cases and any Question numbers being drawn)
                {
                   // sortedRects.remove(sortedRects.indexOf(rect));
                    continue;//If either of these conditions are met then that means that we shouldn't draw this rectangle
                } else {
                    finalRects.add(rect);
                    color = new Scalar(0, 0, 255);//This sets the color to red(According to OpenCV's BGR)
                }
            } else {
                // Check if the rectangle is inside another rectangle and satisfies length condition
                if (isInsideAnyRectangle(rect, regionRects, true) || rect.width <= rect.height) //Check if this rectangle is inside any rectangle or if its width is less than its height(this is to prevent any rectangle inside rectangle cases and any Question numbers being drawn)
                {
                  //  sortedRects.remove(sortedRects.indexOf(rect));
                    continue;//If either of these conditions are met then that means that we shouldn't draw this rectangle
                } else {
                    finalRects.add(rect);
                    color = new Scalar(255, 0, 0);//This sets the color to red(According to OpenCV's BGR)
                }
            }

            Point topLeft = new Point(rect.x, rect.y);
            Point bottomRight = new Point(rect.x + rect.width, rect.y + rect.height);
            Imgproc.rectangle(result, topLeft, bottomRight, color, 2);

            displayImage(result);

            //The code below pauses for a short duration so that we can see the rectangles being drawn step by step
            try
            {
                Thread.sleep(PAUSE_DURATION_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(finalRects.size());
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

    private static void displayImage(Mat image)
    {
        BufferedImage img = matToBufferedImage(image);
        ImageIcon icon = new ImageIcon(img);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);

        imagePanel.removeAll();
        imagePanel.add(lbl);

        frame.revalidate();
        frame.repaint();
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

