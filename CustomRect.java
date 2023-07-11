import org.opencv.core.Rect;

public class CustomRect extends Rect implements Comparable<CustomRect> {
    public CustomRect(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public int compareTo(CustomRect other) {
        // Compare based on the y-coordinate of the rectangles
        int yComparison = Integer.compare(this.y, other.y);
        if (yComparison != 0) {
            return yComparison;
        }

        // Compare based on the x-coordinate of the rectangles
        return Integer.compare(this.x, other.x);
    }
}