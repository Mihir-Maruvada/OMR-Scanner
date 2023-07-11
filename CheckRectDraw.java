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

public class CheckRectDraw {
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private static final int PAUSE_DURATION_MS = 300;

    private static JFrame frame;
    private static JPanel imagePanel;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Loads the OpenCV library

        // Provide the input image file path
        String imagePath = "path/to/your/image.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\BottomPhoto.jpg";
        // Load the image
        Mat image = Imgcodecs.imread(imagePath);

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply threshold to obtain binary image
        Mat binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        // Find contours of gray filled regions
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filter out small contours and find the overall region
        List<CustomRect> regionRects = new ArrayList<>();
        CustomRect overallRect = null;

        for (MatOfPoint contour : contours) {
            Rect boundingRect = Imgproc.boundingRect(contour);
            if ((boundingRect.area() > 750) && (boundingRect.area() < 3000)) {
                if (overallRect == null) {
                    overallRect = new CustomRect(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height);
                    regionRects.add(overallRect);
                } else {
                    CustomRect CustomRect = new CustomRect(boundingRect.x, boundingRect.y, boundingRect.width, boundingRect.height);
                    overallRect = unionRectangles(overallRect, CustomRect);
                    regionRects.add(CustomRect);
                }
            }
        }

        // Sort the regionRects list based on the implemented compareTo method
        // Sort the regionRects list based on the updated compareTo method
        Collections.sort(regionRects);

        // Group and sort rectangles based on y-coordinate and x-coordinate within a range
        List<List<CustomRect>> groupedRects = new ArrayList<>();
        List<CustomRect> currentGroup = new ArrayList<>();
        int prevY = Integer.MIN_VALUE;

        for (CustomRect rect : regionRects) {
            if (Math.abs(rect.y - prevY) <= 7) {
                // Add the rectangle to the current group
                currentGroup.add(rect);
            } else {
                // Sort the current group based on x-coordinate
                Collections.sort(currentGroup, Comparator.comparingInt(rectangle -> rectangle.x));

                // Add the current group to the groupedRects list
                groupedRects.add(currentGroup);

                // Start a new group with the current rectangle
                currentGroup = new ArrayList<>();
                currentGroup.add(rect);
            }

            prevY = rect.y;
        }

        // Sort the last group based on x-coordinate
        Collections.sort(currentGroup, Comparator.comparingInt(rectangle -> rectangle.x));

        // Add the last group to the groupedRects list
        groupedRects.add(currentGroup);

        // Flatten the groupedRects list to get the final sorted order
        List<CustomRect> sortedRects = groupedRects.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Create a JFrame to hold the scroll pane
        frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setTitle("Filled Regions");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

        // Create a JScrollPane to display the images
        JScrollPane scrollPane = new JScrollPane();
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Create a JPanel to hold the image labels
        imagePanel = new JPanel();
        imagePanel.setLayout(new FlowLayout());
        scrollPane.setViewportView(imagePanel);

        // Display the frame
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Draw rectangles based on the sortedRects list
        Mat result = image.clone();

        for (CustomRect rect : sortedRects) {
            // Calculate filled percentage
            double filledPercentage = calculateFilledPercentage(rect, binary);

            // Adjust color and check nesting based on filled percentage and length condition
            Scalar color;
            if (filledPercentage >= 0.7) {
                // Check if the rectangle is inside another rectangle and satisfies length condition
                if (isInsideAnyRectangle(rect, regionRects, false) || rect.width <= rect.height) {
                    continue;  // Skip drawing the red rectangle
                } else {
                    color = new Scalar(0, 0, 255);  // Red color
                }
            } else {
                // Check if the rectangle is inside another rectangle and satisfies length condition
                if (isInsideAnyRectangle(rect, regionRects, true) || rect.width <= rect.height) {
                    continue;  // Skip drawing the blue rectangle
                } else {
                    color = new Scalar(255, 0, 0);  // Blue color
                }
            }

            // Draw the rectangle
            Point topLeft = new Point(rect.x, rect.y);
            Point bottomRight = new Point(rect.x + rect.width, rect.y + rect.height);
            Imgproc.rectangle(result, topLeft, bottomRight, color, 2);

            // Display the result in the GUI
            displayImage(result);

            // Pause for a short duration
            try {
                Thread.sleep(PAUSE_DURATION_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static CustomRect unionRectangles(CustomRect rect1, CustomRect rect2) {
        int x = Math.min(rect1.x, rect2.x);
        int y = Math.min(rect1.y, rect2.y);
        int width = Math.max(rect1.x + rect1.width, rect2.x + rect2.width) - x;
        int height = Math.max(rect1.y + rect1.height, rect2.y + rect2.height) - y;
        return new CustomRect(x, y, width, height);
    }

    private static double calculateFilledPercentage(CustomRect rect, Mat binaryImage) {
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

    private static boolean isInsideAnyRectangle(Rect rect, List<CustomRect> rectangles, boolean sameSize) {
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

    private static void displayImage(Mat image) {
        BufferedImage img = matToBufferedImage(image);
        ImageIcon icon = new ImageIcon(img);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);

        // Add the label to the image panel
        imagePanel.removeAll();
        imagePanel.add(lbl);

        // Update the scroll pane
        frame.revalidate();
        frame.repaint();
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
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

