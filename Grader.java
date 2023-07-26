import org.opencv.core.Scalar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Grader
{
    public String csvPath = "C:\\Users\\mihir\\Documents\\CSV\\E25_KEY5.csv";//Will be changed based on test foRm code
    public static String subjCode = "";
    ArrayList<Integer> incorrectQuestions = new ArrayList<Integer>();//Stores the Question Numbers and will be used to give a detailed score report
    public List<CustomRect> temp = new ArrayList<>();//A list used to store temporary values for proper organization
    public ArrayList<String> input = new ArrayList<>();//List that will store the user input in the form of letters(will be compared with the key)
    public ArrayList<String> key = new ArrayList<>();//List that will store the key for the test(will be compared with the input)
    public String line = "";//This will be used to read the CSV File line by line
    public int startValue = 0;
    public int count = 0;
    boolean isBubbled = false;//Used to determine if a region is filled(to get answer letter) - CHANGE TO A INT
    int bubbleCount;
    public int stopCount = 0;//Used as a precaution when reading csv files to prevent accessing null values.
   public int inputValue = 0;
   public Scalar color = new Scalar(0,0,0);
    public Grader(List<CustomRect> rectangles, String subjectCode)
    {
        this.subjCode = subjectCode;
        if(subjCode.equals("eng"))
        {
            gradeEng(rectangles);
         //gradeEng(rectangles);
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
            //gradeSci(color);
        }
    }
public void gradeEng(List<CustomRect> engRect)
{

    input.clear();
    bubbleCount = 0;
    //System.out.println(finalRects.size());
    while(count < 75) {
        for (int i = startValue; i < startValue + 4; i++) {
            temp.add(engRect.get(i)); //Add the set of 4 values to the temp list
            //System.out.println(temp.get(i));
        }
        for (int i = 0; i < temp.size(); i++)
        {
            //  System.out.print(i + " ");
            if(isBubbleFilled(temp.get(i).color) == true)
            {
                bubbleCount++;
                if(bubbleCount > 1)
                {
                    break;
                }
                // System.out.print(isBubbleFilled(temp.get(i)) + " ");
                if(i == 0)
                {
                    input.add("A");
                    // System.out.println(count + "A");
                    //System.out.println(count + 1 + " a");
                }
                else if(i == 1)
                {
                    input.add("B");
                    //System.out.println(count + 1 + " b");
                    // System.out.println(count + "B");
                }
                else if(i == 2)
                {
                    input.add("C");
                    // System.out.println(count + "C");
                    //System.out.println(count + 1+ " c");
                }
                else if(i == 3)
                {
                    input.add("D");
                    // System.out.println(count + "D");
                    //System.out.println(count + 1 + " d");
                }

            }

        }
        if(bubbleCount < 0)
        {
            input.add(" ");
        }

        bubbleCount = 0;
        // System.out.println();
        temp.clear();
        count++;
        startValue = startValue+4;
        // System.out.println("Start Value " + startValue);

    }
    /*
    for (int i = 0; i < input.size(); i++) {
        System.out.println(input.get(i));
    }
     */
    String[][] raggedArray = new String[13][];
    for (int i = 0; i < 10; i++) {
        raggedArray[i] = new String[6];
    }
    for (int i = 10; i < 13; i++) {
        raggedArray[i] = new String[5];
    }
    int count = 0;
    for (int i = 0; i < raggedArray.length; i++) {
        for (int j = 0; j < raggedArray[i].length; j++) {
            raggedArray[i][j] = input.get(count);
            count++;
        }
    }
    for (int i = 0; i < raggedArray.length; i++) {
        for (int j = 0; j < raggedArray[i].length; j++) {
            //System.out.print(raggedArray[i][j] + " ");
        }
        //System.out.println(); // Move to the next row after printing each row
    }
    input.clear();
    for (int col = 0; col < 6; col++) {
        for (int row = 0; row < raggedArray.length; row++) {
            if (col < raggedArray[row].length) {
                //System.out.print(raggedArray[row][col] + " ");
                input.add(raggedArray[row][col]);
            }
        }
    }
    for (int i = 0; i < input.size(); i++) //changing a to f
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

    try{

        BufferedReader b = new BufferedReader(new FileReader(csvPath));
        while((line = b.readLine()) != null)
        {
            String [] values = line.split(","); //Takes the values of the CSV File into the array
            //Takes and splits by column so values[0] is value in the first column of that line
            //System.out.println(tempCount + " " + values[2]);
            if(!(values[0] == null))
            {
                key.add(values[0]);
            }

            //tempCount++;
            // key.add(values[2]); //2 is the third column which holds the Reading test key
        }
        System.out.println("KEY SIZE" + key.size());

    }
    catch(Exception e)
    {
        // System.out.println(e);
    }

    //COMPARING THE KEY AND THE INPUT TO GET THE RESULTS
    for (int i = 0; i < input.size(); i++) {

        if(input.get(i).equals(" ")){
            System.out.println("Empty Answer Bubble for #" + (i+1));
            // appendToOutput("Empty Answer Bubble for #" + (i+1));
            System.out.println("The Correct Answer is " + key.get(i));
            //appendToOutput("The Correct Answer is " + key.get(i));
            //countIncorrect++;
            incorrectQuestions.add((i+1));
        }
        else if(!(key.get(i).equals(input.get(i))))
        {

            System.out.println("# " + (i+1) + " is incorrect");
            //appendToOutput("# " + (i+1) + " is incorrect");
            System.out.println("You inputted " + input.get(i));
            System.out.println("The Correct Answer is " + key.get(i));
            //appendToOutput("The Correct Answer is " + key.get(i));
            //countIncorrect++;
            incorrectQuestions.add((i+1));

        }
        //System.out.println(key.get(i));
    }
    int countCorrect = 75 - incorrectQuestions.size();
    try{
        BufferedReader b2 = new BufferedReader(new FileReader("C:\\Users\\mihir\\Documents\\CSV\\E25_KEY5.csv"));
        while((line = b2.readLine()) != null)
        {
            String [] stringValues = line.split(","); //Takes the values of the CSV File into the array
            //int[] intValues = new int[2];
            ArrayList<Integer> intValues = new ArrayList<>();
            for (int i = 4; i < stringValues.length; i++) {
                // System.out.println(Integer.parseInt(stringValues[i]));
                intValues.add(Integer.parseInt(stringValues[i]));
            }
            if(intValues.get(0) == countCorrect)
            {
                System.out.println("You have a total of " + countCorrect + " correct questions");
                // appendToOutput("You have a total of " + countCorrect + " correct questions");
                System.out.println("Your scaled score is " + intValues.get(1));
                //appendToOutput("Your scaled score is " + intValues.get(1));
                break;
            }
            //tempCount++;
            // key.add(values[2]); //2 is the third column which holds the Reading test key
        }


    }
    catch(Exception e)
    {
        System.out.println(e);
    }
/*
    for (int i = 0; i < input.size(); i++) {
        System.out.println((i+1) + input.get(i));
    }
*

    /*
    // Loop through the ragged array by column
    int maxColumns = 0;
    for (int i = 0; i < raggedArray.length; i++) {
        if (raggedArray[i].length > maxColumns) {
            maxColumns = raggedArray[i].length;
        }
    }

    for (int col = 0; col < maxColumns; col++) {
        for (int row = 0; row < raggedArray.length; row++) {
            if (col < raggedArray[row].length) {
                System.out.print(raggedArray[row][col] + " ");
            } else {
                System.out.print("  "); // To maintain alignment for shorter rows
            }
        }
        System.out.println(); // Move to the next column after printing each column
    }
    */
}

public void gradeMath(List<CustomRect> mathRect)
{
    input.clear();
    bubbleCount = 0;
    //System.out.println(finalRects.size());
    while(count < 60) {
        for (int i = startValue; i < startValue + 5; i++) {
            temp.add(mathRect.get(i)); //Add the set of 4 values to the temp list
            //System.out.println(temp.get(i));
        }
        for (int i = 0; i < temp.size(); i++)
        {
            //  System.out.print(i + " ");
            if(isBubbleFilled(temp.get(i).color) == true)
            {
                bubbleCount++;
                if(bubbleCount > 1)
                {
                    break;
                }
                // System.out.print(isBubbleFilled(temp.get(i)) + " ");
                if(i == 0)
                {
                    input.add("A");
                    // System.out.println(count + "A");
                    //System.out.println(count + 1 + " a");
                }
                else if(i == 1)
                {
                    input.add("B");
                    //System.out.println(count + 1 + " b");
                    // System.out.println(count + "B");
                }
                else if(i == 2)
                {
                    input.add("C");
                    // System.out.println(count + "C");
                    //System.out.println(count + 1+ " c");
                }
                else if(i == 3)
                {
                    input.add("D");
                    // System.out.println(count + "D");
                    //System.out.println(count + 1 + " d");
                }
                else if(i == 4)
                {
                    input.add("E");
                    // System.out.println(count + "D");
                    //System.out.println(count + 1 + " d");
                }
            }

        }
        if(bubbleCount < 0)
        {
            input.add(" ");
        }

        bubbleCount = 0;
        // System.out.println();
        temp.clear();
        count++;
        startValue = startValue+5;
        // System.out.println("Start Value " + startValue);

    }
    /*
    for (int i = 0; i < input.size(); i++) {
        System.out.println(input.get(i));
    }
     */
    int count = 0;
    String[][] math = new String[10][6];

    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 6; j++) {
            math[i][j] = input.get(count);
            count++;
        }
    }
    input.clear();
    for (int i = 0; i < 6; i++) {
        for (int row = 0; row < math.length; row++) {
          input.add(math[row][i]);
            //System.out.println("Element at column " + columnToAccess + " and row " + row + ": " + element);
        }
    }
    for (int i = 0; i < input.size(); i++) //changing a to f
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
            else if(input.get(i).equals("E"))
            {
                input.set(i, "K");
            }
        }
    }
    /*
    for (int i = 0; i < input.size(); i++) {
        System.out.println((i+1) + input.get(i));
    }
    */
    try{

        BufferedReader b = new BufferedReader(new FileReader(csvPath));
        while((line = b.readLine()) != null)
        {
            String [] values = line.split(","); //Takes the values of the CSV File into the array
            //Takes and splits by column so values[0] is value in the first column of that line
            //System.out.println(tempCount + " " + values[2]);
            if(stopCount > 59) {
                break;
            }
            key.add(values[1]);
            //tempCount++;
            // key.add(values[2]); //2 is the third column which holds the Reading test key
            stopCount++;
        }


    }
    catch(Exception e)
    {
        // System.out.println(e);
    }
    for (int i = 0; i < key.size(); i++) {

        if(input.get(i).equals(" ")){
            System.out.println("Empty Answer Bubble for #" + (i+1));
            // appendToOutput("Empty Answer Bubble for #" + (i+1));
            System.out.println("The Correct Answer is " + key.get(i));
            //appendToOutput("The Correct Answer is " + key.get(i));
            //countIncorrect++;
            incorrectQuestions.add((i+1));
        }
        else if(!(key.get(i).equals(input.get(i))))
        {

            System.out.println("# " + (i+1) + " is incorrect");
            //appendToOutput("# " + (i+1) + " is incorrect");
            System.out.println("You inputted " + input.get(i));
            System.out.println("The Correct Answer is " + key.get(i));
            //appendToOutput("The Correct Answer is " + key.get(i));
            //countIncorrect++;
            incorrectQuestions.add((i+1));

        }
        //System.out.println(key.get(i));
    }
}
/*
    public void gradeEng(List<CustomRect> engRect) //mihir
    {
        input.clear();
        bubbleCount = 0;
        //System.out.println(finalRects.size());
        while(count < 75) {
            for (int i = startValue; i < startValue + 4; i++) {
                temp.add(engRect.get(i)); //Add the set of 4 values to the temp list
                //System.out.println(temp.get(i));
            }
            for (int i = 0; i < temp.size(); i++)
            {
                //  System.out.print(i + " ");
                if(isBubbleFilled(temp.get(i).color) == true)
                {
                    bubbleCount++;
                    if(bubbleCount > 1)
                    {
                        break;
                    }
                    // System.out.print(isBubbleFilled(temp.get(i)) + " ");
                    if(i == 0)
                    {
                        input.add("A");
                        // System.out.println(count + "A");
                        //System.out.println(count + 1 + " a");
                    }
                    else if(i == 1)
                    {
                        input.add("B");
                        //System.out.println(count + 1 + " b");
                        // System.out.println(count + "B");
                    }
                    else if(i == 2)
                    {
                        input.add("C");
                        // System.out.println(count + "C");
                        //System.out.println(count + 1+ " c");
                    }
                    else if(i == 3)
                    {
                        input.add("D");
                        // System.out.println(count + "D");
                        //System.out.println(count + 1 + " d");
                    }

                }

            }
            if(bubbleCount < 0)
            {
                input.add(" ");
            }

            bubbleCount = 0;
            // System.out.println();
            temp.clear();
            count++;
            startValue = startValue+4;
            // System.out.println("Start Value " + startValue);

        }


        String[][] Reading = new String[13][6];
        System.out.println("INPUT SIZE" + input.size());
        for (int i = 0; i < 13; i++)
        {
            for (int j = 0; j < 6; j++)
            {
                if(inputValue > 74)
                {
                    break;
                }
                if (shouldSkipSpace(i, j,"eng")) {
                    continue;  // Skip to the next iteration
                }

                // Access the space and assign a value
                //System.out.println(inputValue);
                Reading[i][j] = input.get(inputValue);
                //System.out.println(input.get(inputValue));
                inputValue++;

            }
        }

        //String[][] transposedArray = new String[6][13];

        // Generate the transpose of the original array
        //for (int i = 0; i < 13; i++) {
        //    for (int j = 0; j < 6; j++) {
        //        transposedArray[j][i] = Reading[i][j];
        //    }
        //}


        //for (String[] a:transposedArray) {
         //   System.out.println(Arrays.toString(a));
        //}


       input.clear();
        for (int j = 0; j < Reading[0].length; j++) {
            for (int i = 0; i < Reading.length; i++) {
                // Check if the space should be skipped

                if (shouldSkipSpace(i, j,"eng")) {
                    continue;  // Skip to the next iteration
                }

                // Access the space and print its value
                //System.out.println(Reading[i][j]);
                //System.out.println(Reading[i][j]);
                if(!(Reading[i][j] == null)) {
                    input.add(Reading[i][j]);
                }
            }
        }
        System.out.println("NEW INPUT SIZE" + input.size());


        for (String a:input) {
            //System.out.println(a);
        }

        for (int i = 0; i < input.size(); i++) //changing a to f
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



        System.out.println("I AM HERE");
        //NOW WE HAVE ALL OF THE ANSWERS IN THE CORRECT ORDER
        //PUT THE GRADING CODE HERE

        try{

            BufferedReader b = new BufferedReader(new FileReader(csvPath));
            while((line = b.readLine()) != null)
            {
                String [] values = line.split(","); //Takes the values of the CSV File into the array
                //Takes and splits by column so values[0] is value in the first column of that line
                //System.out.println(tempCount + " " + values[2]);
                if(!(values[0] == null))
                {
                    key.add(values[0]);
                }

                //tempCount++;
                // key.add(values[2]); //2 is the third column which holds the Reading test key
            }
            System.out.println("KEY SIZE" + key.size());

        }
        catch(Exception e)
        {
            // System.out.println(e);
        }

        //COMPARING THE KEY AND THE INPUT TO GET THE RESULTS
        for (int i = 0; i < input.size(); i++) {

            if(input.get(i).equals(" ")){
                System.out.println("Empty Answer Bubble for #" + (i+1));
               // appendToOutput("Empty Answer Bubble for #" + (i+1));
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
                incorrectQuestions.add((i+1));
            }
            else if(!(key.get(i).equals(input.get(i))))
            {

                System.out.println("# " + (i+1) + " is incorrect");
                //appendToOutput("# " + (i+1) + " is incorrect");
                System.out.println("You inputted " + input.get(i));
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
                incorrectQuestions.add((i+1));

            }
            //System.out.println(key.get(i));
        }
        //SCALE THE TEST
        int countCorrect = 75 - incorrectQuestions.size();
        try{
            BufferedReader b2 = new BufferedReader(new FileReader("C:\\Users\\mihir\\Documents\\CSV\\E25_KEY5.csv"));
            while((line = b2.readLine()) != null)
            {
                String [] stringValues = line.split(","); //Takes the values of the CSV File into the array
                //int[] intValues = new int[2];
                ArrayList<Integer> intValues = new ArrayList<>();
                for (int i = 4; i < stringValues.length; i++) {
                    // System.out.println(Integer.parseInt(stringValues[i]));
                    intValues.add(Integer.parseInt(stringValues[i]));
                }
                if(intValues.get(0) == countCorrect)
                {
                    System.out.println("You have a total of " + countCorrect + " correct questions");
                   // appendToOutput("You have a total of " + countCorrect + " correct questions");
                    System.out.println("Your scaled score is " + intValues.get(1));
                    //appendToOutput("Your scaled score is " + intValues.get(1));
                    break;
                }
                //tempCount++;
                // key.add(values[2]); //2 is the third column which holds the Reading test key
            }


        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    */
/*
    public void gradeEng(List<CustomRect> engRect)
    {
        input.clear();
        //System.out.println(finalRects.size());

        ArrayList<ArrayList<String>>[] arList = new ArrayList<>[13];
        int row = 0;
        for (int i = 0; i < engRect.size(); i+=4) {
            ArrayList<String> arrTemp = new ArrayList<>();
            arrTemp.add()

        }


        while(count < 75) {
            for (int i = startValue; i < startValue + 4; i++) {
                temp.add(engRect.get(i)); //Add the set of 4 values to the temp list
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
                        // System.out.println(count + "A");
                        //System.out.println(count + 1 + " a");
                    }
                    else if(i == 1)
                    {
                        input.add("B");
                        //System.out.println(count + 1 + " b");
                        // System.out.println(count + "B");
                    }
                    else if(i == 2)
                    {
                        input.add("C");
                        // System.out.println(count + "C");
                        //System.out.println(count + 1+ " c");
                    }
                    else if(i == 3)
                    {
                        input.add("D");
                        // System.out.println(count + "D");
                        //System.out.println(count + 1 + " d");
                    }

                }

            }
            if(isBubbled == false)
            {
                //input.add(" ");
            }
            isBubbled = false;
            // System.out.println();
            temp.clear();
            count++;
            startValue = startValue+4;
            // System.out.println("Start Value " + startValue);

        }


        String[][] Reading = new String[13][6];
        System.out.println("INPUT SIZE" + input.size());
        for (int i = 0; i < 13; i++)
        {
            for (int j = 0; j < 6; j++)
            {
                if(inputValue > 74)
                {
                    break;
                }
                if (shouldSkipSpace(i, j,"eng")) {
                    continue;  // Skip to the next iteration
                }

                // Access the space and assign a value
                //System.out.println(inputValue);
                Reading[i][j] = input.get(inputValue);
                //System.out.println(input.get(inputValue));
                inputValue++;

            }
        }

        //String[][] transposedArray = new String[6][13];

        // Generate the transpose of the original array
        //for (int i = 0; i < 13; i++) {
        //    for (int j = 0; j < 6; j++) {
        //        transposedArray[j][i] = Reading[i][j];
        //    }
        //}


        //for (String[] a:transposedArray) {
        //   System.out.println(Arrays.toString(a));
        //}


        input.clear();
        for (int j = 0; j < Reading[0].length; j++) {
            for (int i = 0; i < Reading.length; i++) {
                // Check if the space should be skipped

                if (shouldSkipSpace(i, j,"eng")) {
                    continue;  // Skip to the next iteration
                }

                // Access the space and print its value
                //System.out.println(Reading[i][j]);
                //System.out.println(Reading[i][j]);
                if(!(Reading[i][j] == null)) {
                    input.add(Reading[i][j]);
                }
            }
        }
        System.out.println("NEW INPUT SIZE" + input.size());

        for (int i = 0; i < input.size(); i++) //changing a to f
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



        System.out.println("I AM HERE");
        //NOW WE HAVE ALL OF THE ANSWERS IN THE CORRECT ORDER
        //PUT THE GRADING CODE HERE

        try{

            BufferedReader b = new BufferedReader(new FileReader(csvPath));
            while((line = b.readLine()) != null)
            {
                String [] values = line.split(","); //Takes the values of the CSV File into the array
                //Takes and splits by column so values[0] is value in the first column of that line
                //System.out.println(tempCount + " " + values[2]);
                if(!(values[0] == null))
                {
                    key.add(values[0]);
                }

                //tempCount++;
                // key.add(values[2]); //2 is the third column which holds the Reading test key
            }
            System.out.println("KEY SIZE" + key.size());

        }
        catch(Exception e)
        {
            // System.out.println(e);
        }

        //COMPARING THE KEY AND THE INPUT TO GET THE RESULTS
        for (int i = 0; i < input.size(); i++) {

            if(input.get(i).equals(" ")){
                System.out.println("Empty Answer Bubble for #" + (i+1));
                // appendToOutput("Empty Answer Bubble for #" + (i+1));
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
                incorrectQuestions.add((i+1));
            }
            else if(!(key.get(i).equals(input.get(i))))
            {

                System.out.println("# " + (i+1) + " is incorrect");
                //appendToOutput("# " + (i+1) + " is incorrect");
                System.out.println("You inputted " + input.get(i));
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
                incorrectQuestions.add((i+1));

            }
            //System.out.println(key.get(i));
        }
        //SCALE THE TEST
        int countCorrect = 75 - incorrectQuestions.size();
        try{
            BufferedReader b2 = new BufferedReader(new FileReader("C:\\Users\\mihir\\Documents\\CSV\\E25_KEY5.csv"));
            while((line = b2.readLine()) != null)
            {
                String [] stringValues = line.split(","); //Takes the values of the CSV File into the array
                //int[] intValues = new int[2];
                ArrayList<Integer> intValues = new ArrayList<>();
                for (int i = 4; i < stringValues.length; i++) {
                    // System.out.println(Integer.parseInt(stringValues[i]));
                    intValues.add(Integer.parseInt(stringValues[i]));
                }
                if(intValues.get(0) == countCorrect)
                {
                    System.out.println("You have a total of " + countCorrect + " correct questions");
                    // appendToOutput("You have a total of " + countCorrect + " correct questions");
                    System.out.println("Your scaled score is " + intValues.get(1));
                    //appendToOutput("Your scaled score is " + intValues.get(1));
                    break;
                }
                //tempCount++;
                // key.add(values[2]); //2 is the third column which holds the Reading test key
            }


        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
*/
    /*
    public void gradeMath(List<CustomRect> mathRect)
    {
        while(count < 60) {
            for (int i = startValue; i < startValue + 5; i++) {
                temp.add(mathRect.get(i)); //Add the set of 4 values to the temp list
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
        System.out.println("---------------UNSORTED LIST------------------");
        for (int i = 0; i < input.size() ; i++) {
            System.out.println((i+1) + input.get(i));
        }
        System.out.println("----------------------------------------------------------");
        inputValue = 0;
        String[][] Reading = new String[10][6];
        System.out.println("INPUT SIZE" + input.size());
        for (int i = 0; i < 10; i++)
        {
            for (int j = 0; j < 6; j++)
            {

                if(inputValue > 59)
                {
                    break;
                }
                // Access the space and assign a value
                //System.out.println(inputValue);
                Reading[i][j] = input.get(inputValue);
                //System.out.println(input.get(inputValue));
                inputValue++;

            }
        }

        input.clear();
        for (int j = 0; j < Reading[0].length; j++) {
            for (int i = 0; i < Reading.length; i++) {
                // Check if the space should be skipped

                // Access the space and print its value
                //System.out.println(Reading[i][j]);
               input.add(Reading[i][j]);
            }
        }
        for (int i = 0; i < 60; i++)
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
        System.out.println("---------------PRINTING THE ORDERED LIST------------------");
        for (int i = 0; i < input.size() ; i++) {
            System.out.println((i+1) + input.get(i));
        }
        System.out.println("----------------------------------------------------------");
        //NOW THE LIST IS READY FOR GRADING

        try{

            BufferedReader b = new BufferedReader(new FileReader(csvPath));
            while((line = b.readLine()) != null)
            {
                String [] values = line.split(","); //Takes the values of the CSV File into the array
                //Takes and splits by column so values[0] is value in the first column of that line
                //System.out.println(tempCount + " " + values[2]);
                if(stopCount > 59) {
                    break;
                }
                key.add(values[1]);
                //tempCount++;
                // key.add(values[2]); //2 is the third column which holds the Reading test key
                stopCount++;
            }


        }
        catch(Exception e)
        {
            // System.out.println(e);
        }
        for (int i = 0; i < key.size(); i++) {

            if(input.get(i).equals(" ")){
                System.out.println("Empty Answer Bubble for #" + (i+1));
                // appendToOutput("Empty Answer Bubble for #" + (i+1));
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
                incorrectQuestions.add((i+1));
            }
            else if(!(key.get(i).equals(input.get(i))))
            {

                System.out.println("# " + (i+1) + " is incorrect");
                //appendToOutput("# " + (i+1) + " is incorrect");
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
                incorrectQuestions.add((i+1));

            }
            //System.out.println(key.get(i));
        }
        //SCALE THE TEST
        int countCorrect = 60 - incorrectQuestions.size();
        try{
            BufferedReader b2 = new BufferedReader(new FileReader("C:\\Users\\mihir\\Documents\\CSV\\E25_KEY5.csv"));
            while((line = b2.readLine()) != null)
            {
                String [] stringValues = line.split(","); //Takes the values of the CSV File into the array
                //int[] intValues = new int[2];
                ArrayList<Integer> intValues = new ArrayList<>();
                for (int i = 4; i < stringValues.length; i++) {
                    // System.out.println(Integer.parseInt(stringValues[i]));
                    intValues.add(Integer.parseInt(stringValues[i]));
                }
                if(intValues.get(2) == countCorrect)
                {
                    System.out.println("You have a total of " + countCorrect + " correct questions");
                    // appendToOutput("You have a total of " + countCorrect + " correct questions");
                    System.out.println("Your scaled score is " + intValues.get(3));
                    //appendToOutput("Your scaled score is " + intValues.get(1));
                    break;
                }
                //tempCount++;
                // key.add(values[2]); //2 is the third column which holds the Reading test key
            }


        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
     */
    public void gradeRead(List<CustomRect> readRect)
    {
        //System.out.println("IN GRADE READ METHOD");
        //System.out.println("readRect size " + readRect.size());
        //System.out.println(readRect.get(0).color.val[0]);
        //System.out.println(color.size());
        while(count < 40) {
            for (int i = startValue; i < startValue + 4; i++) {
                temp.add(readRect.get(i)); //Add the set of 4 values to the temp list

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
         inputValue = 0;
        String[][] Reading = new String[7][6];
        for (int i = 0; i < 7; i++)
        {
            for (int j = 0; j < 6; j++)
            {
                if (shouldSkipSpace(i, j,"read")) {
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
                if (shouldSkipSpace(i, j,"read")) {
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
        //NOW THE LIST IS READY FOR GRADING
        try{

            BufferedReader b = new BufferedReader(new FileReader(csvPath));
            while((line = b.readLine()) != null)
            {
                String [] values = line.split(","); //Takes the values of the CSV File into the array
                //Takes and splits by column so values[0] is value in the first column of that line
                //System.out.println(tempCount + " " + values[2]);
                if(stopCount > 39) {
                    break;
                }
                key.add(values[2]);
                //tempCount++;
                // key.add(values[2]); //2 is the third column which holds the Reading test key
                stopCount++;
            }


        }
        catch(Exception e)
        {
            // System.out.println(e);
        }
        for (int i = 0; i < key.size(); i++) {

            if(input.get(i).equals(" ")){
                System.out.println("Empty Answer Bubble for #" + (i+1));
                // appendToOutput("Empty Answer Bubble for #" + (i+1));
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
                incorrectQuestions.add((i+1));
            }
            else if(!(key.get(i).equals(input.get(i))))
            {

                System.out.println("# " + (i+1) + " is incorrect");
                //appendToOutput("# " + (i+1) + " is incorrect");
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
                incorrectQuestions.add((i+1));

            }
            //System.out.println(key.get(i));
        }
        int countCorrect = 40 - incorrectQuestions.size();
        try{
            BufferedReader b2 = new BufferedReader(new FileReader("C:\\Users\\mihir\\Documents\\CSV\\E25_KEY5.csv"));
            while((line = b2.readLine()) != null)
            {
                String [] stringValues = line.split(","); //Takes the values of the CSV File into the array
                //int[] intValues = new int[2];
                ArrayList<Integer> intValues = new ArrayList<>();
                for (int i = 4; i < stringValues.length; i++) {
                    // System.out.println(Integer.parseInt(stringValues[i]));
                    intValues.add(Integer.parseInt(stringValues[i]));
                }
                if(intValues.get(4) == countCorrect)
                {
                    System.out.println("You have a total of " + countCorrect + " correct questions");
                    // appendToOutput("You have a total of " + countCorrect + " correct questions");
                    System.out.println("Your scaled score is " + intValues.get(5));
                    //appendToOutput("Your scaled score is " + intValues.get(1));
                    break;
                }
                //tempCount++;
                // key.add(values[2]); //2 is the third column which holds the Reading test key
            }


        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    /*
    public void gradeSci(List<Scalar> sciColor)
    {
        while(count < 40) {
            for (int i = startValue; i < startValue + 4; i++) {
                temp.add(sciColor.get(i)); //Add the set of 4 values to the temp list

            }
            for (int i = 0; i < temp.size(); i++)
            {
                //  System.out.print(i + " ");
                if(isBubbleFilled(temp.get(i)) == true)
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
         inputValue = 0;
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
        //NOW WE CAN GRADE
        try{

            BufferedReader b = new BufferedReader(new FileReader(csvPath));
            while((line = b.readLine()) != null)
            {
                String [] values = line.split(","); //Takes the values of the CSV File into the array
                //Takes and splits by column so values[0] is value in the first column of that line
                //System.out.println(tempCount + " " + values[2]);
                if(stopCount > 39) {
                    break;
                }
                key.add(values[3]);
                //tempCount++;
                // key.add(values[2]); //2 is the third column which holds the Reading test key
                stopCount++;
            }


        }
        catch(Exception e)
        {
            // System.out.println(e);
        }
        for (int i = 0; i < key.size(); i++) {

            if(input.get(i).equals(" ")){
                System.out.println("Empty Answer Bubble for #" + (i+1));
                // appendToOutput("Empty Answer Bubble for #" + (i+1));
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
                incorrectQuestions.add((i+1));
            }
            else if(!(key.get(i).equals(input.get(i))))
            {

                System.out.println("# " + (i+1) + " is incorrect");
                //appendToOutput("# " + (i+1) + " is incorrect");
                System.out.println("The Correct Answer is " + key.get(i));
                //appendToOutput("The Correct Answer is " + key.get(i));
                //countIncorrect++;
                incorrectQuestions.add((i+1));

            }
            //System.out.println(key.get(i));
        }
        int countCorrect = 40 - incorrectQuestions.size();
        try{
            BufferedReader b2 = new BufferedReader(new FileReader("C:\\Users\\mihir\\Documents\\CSV\\E25_KEY5.csv"));
            while((line = b2.readLine()) != null)
            {
                String [] stringValues = line.split(","); //Takes the values of the CSV File into the array
                //int[] intValues = new int[2];
                ArrayList<Integer> intValues = new ArrayList<>();
                for (int i = 4; i < stringValues.length; i++) {
                    // System.out.println(Integer.parseInt(stringValues[i]));
                    intValues.add(Integer.parseInt(stringValues[i]));
                }
                if(intValues.get(0) == countCorrect)
                {
                    System.out.println("You have a total of " + countCorrect + " correct questions");
                    // appendToOutput("You have a total of " + countCorrect + " correct questions");
                    System.out.println("Your scaled score is " + intValues.get(1));
                    //appendToOutput("Your scaled score is " + intValues.get(1));
                    break;
                }
                //tempCount++;
                // key.add(values[2]); //2 is the third column which holds the Reading test key
            }


        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
     */

    public static boolean isBubbleFilled(Scalar color)
    {
        //System.out.println(color.val[0]);
        if(color.val[0] == 255.0)
        {
            return false;
        }
        return true;
    }

    public static boolean shouldSkipSpace(int i, int j, String code)
    {
        if(code.equals("eng"))
        {
            if((i == 5 & j == 10)||(i == 5 & j == 11)||(i == 5 & j == 12))
            {
                return true;
            }
        }
        else if(code.equals("read") || code.equals("sci"))
        {
            if((i == 5 & j == 5)||(i == 6 & j == 5))
            {
                return true;
            }
        }

        return false;
    }
}
