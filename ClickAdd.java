import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClickAdd {
    private static List<CustomRect> regionRects;
    private static Mat image;
    private static Mat binary;
    private static Mat result;
    private static JFrame frame;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Provide the input image file path
        String imagePath = "path/to/your/image.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\LeftTiltTopCropped.jpg";
        imagePath = "C:\\Users\\mihir\\Desktop\\EnglishBubbled.jpg";
        // Load the image
        image = Imgcodecs.imread(imagePath);

        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply threshold to obtain binary image
        binary = new Mat();
        Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        // Find contours of gray filled regions
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Filter out small contours and find the overall region
        regionRects = new ArrayList<>();
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
        Collections.sort(regionRects);

        // Draw rectangles based on the sorted regionRects list
        result = image.clone();
        drawRectangles();
        frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setTitle("Filled Regions");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JScrollPane scrollPane = new JScrollPane();
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(image.cols() + 20, image.rows() + 20));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        for (int i = 0; i < regionRects.size(); i++) {
            System.out.print("WIDTH" + regionRects.get(i).width + " HEIGHT " + regionRects.get(i).height);
            System.out.println();
        }
        displayImage(result, "Filled Regions");
    }

    private static void drawRectangles() {
        for (CustomRect rect : regionRects) {
            double filledPercentage = calculateFilledPercentage(rect, binary);

            Scalar color;
            if (filledPercentage >= 0.7) {
                if (isInsideAnyRectangle(rect, regionRects, false) || rect.width <= rect.height) {
                    continue;
                } else {
                    color = new Scalar(0, 0, 255);
                }
            } else {
                if (isInsideAnyRectangle(rect, regionRects, true) || rect.width <= rect.height) {
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

    private static void displayImage(Mat image, String title) {
        BufferedImage img = matToBufferedImage(image);
        ImageIcon icon = new ImageIcon(img);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);

        JScrollPane scrollPane = (JScrollPane) frame.getContentPane().getComponent(0);
        scrollPane.setViewportView(lbl);
        scrollPane.revalidate();
        scrollPane.repaint();

        frame.setTitle(title);

        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                java.awt.Point clickPoint = e.getPoint();
                addRectangle(clickPoint);
            }
        });
    }

    private static void addRectangle(java.awt.Point clickPoint) {
        int rectangleWidth = 36;
        int rectangleHeight = 26;
        int rectangleX = (int) (clickPoint.getX() - rectangleWidth / 2);
        int rectangleY = (int) (clickPoint.getY() - rectangleHeight / 2);

        // Create a new CustomRect representing the new rectangle
        CustomRect newRect = new CustomRect(rectangleX, rectangleY, rectangleWidth, rectangleHeight);

        // Check if the new rectangle is inside any existing rectangle
        if (isInsideAnyRectangle(newRect, regionRects, true)) {
            return; // Skip drawing the rectangle
        }

        // Add the new rectangle to the regionRects list
        regionRects.add(newRect);

        result = image.clone();
        drawRectangles();

        displayImage(result, "Filled Regions");
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
