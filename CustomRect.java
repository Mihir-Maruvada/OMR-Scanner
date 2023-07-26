import org.opencv.core.Rect;
import org.opencv.core.Scalar;

public class CustomRect extends Rect implements Comparable<CustomRect> {
    public Scalar color;
    public CustomRect(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public CustomRect(int x, int y, int width, int height, Scalar col) {

        super(x, y, width, height);
        //super();
        this.color = col;
        //RectDraw.color.add(col);
    }

    @Override
    public int compareTo(CustomRect other) {
        int yComparison = Integer.compare(this.y, other.y);
        if (yComparison != 0) {
            return yComparison;
        }

        return Integer.compare(this.x, other.x);
    }
}
