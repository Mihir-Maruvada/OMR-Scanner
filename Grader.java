import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

public class Grader
{
    public String csvPath = "C:\\Users\\mihir\\Documents\\CSV\\E25_KEY4.csv";//Will be changed based on test fom code
    ArrayList<Integer> incorrectQuestions = new ArrayList<Integer>();
    public List<CustomRect> temp = new ArrayList<>();
    public ArrayList<String> input = new ArrayList<>();
    int startValue = 0;
    int count = 0;
    boolean isBubbled = false;
    public Grader(List<CustomRect> rectangles, String subjCode)
    {
        if(subjCode.equals("eng"))
        {
            gradeEng(rectangles);
        }
        else if(subjCode.equals("math"))
        {
            gradeMath(rectangles);
        }
        else if(subjCode.equals("read"))
        {
            gradeRead(rectangles);
        }
        else if(subjCode.equals("sci"))
        {
            gradeSci(rectangles);
        }
    }

    public void gradeEng(List<CustomRect> engRect)
    {

    }
    public void gradeMath(List<CustomRect> mathRect)
    {

    }
    public void gradeRead(List<CustomRect> readingRect)
    {
        while(count < 40) {
            for (int i = startValue; i < startValue + 4; i++) {
                temp.add(readingRect.get(i)); //Add the set of 4 values to the temp list

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
        int inputValue = 0;
        String[][] Reading = new String[7][6];
        for (int i = 0; i < 7; i++)
        {
            for (int j = 0; j < 6; j++)
            {
                if (shouldSkipSpace(i, j)) {
                    continue;  // Skip to the next iteration
                }

                // Access the space and assign a value
                Reading[i][j] = input.get(inputValue);
                inputValue++;
            }

        }
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
        for (int i = 0; i < 40; i++)
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
    }
    public void gradeSci(List<CustomRect> sciRect)
    {

    }
    public static boolean isBubbleFilled(Scalar color)
    {

        if(color.val[0] == 255.0)
        {
            return false;
        }
        return true;
    }

    public static boolean shouldSkipSpace(int i, int j)
    {
        if((i == 5 & j == 5)||(i == 6 & j == 5))
        {
            return true;
        }
        return false;
    }
}
