import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Created by Steven Saylor on 1/29/2015.
 * CS 1675
 * Assignment 2
 */
public class ID3<E, T> {
	private static ArrayList<String> featureNames;
	private static ArrayList<String> possibleLabels;
	private static int diversity;   //value representing diversity function to use
	private static ArrayList<TrainingExample> trainExamples = new ArrayList<TrainingExample>();
	private static ArrayList<TrainingExample> testExamples = new ArrayList<TrainingExample>();
	private static HashMap<String, ArrayList<String>> possibleFeatureValues = new HashMap<String, ArrayList<String>>();

	public static void main(String[] args) throws IOException {
		printHeader();
		readFiles(args);
		System.out.println("Using " + args[0] + " in gain function.");
		ID3Tree id3Tree = new ID3Tree(trainExamples, possibleLabels, possibleFeatureValues, diversity);

		DecimalFormat df = new DecimalFormat("###.###");

		int numCorrect = id3Tree.test(trainExamples);
		double percent = (((double) numCorrect) / trainExamples.size()) * 100;
		System.out.println("The accuracy on the training data is: " + numCorrect + "/" + trainExamples.size() +
				" = " + df.format(percent) + "%");

		numCorrect = id3Tree.test(testExamples);
		percent = (((double) numCorrect) / testExamples.size()) * 100;
		System.out.println("The accuracy on the test data is: " + numCorrect + "/" + testExamples.size() +
				" = " + df.format(percent) + "%");

		System.out.println("The final decision tree:");
		id3Tree.printTree();

	}

	private static void readFiles(String[] args) throws IOException {
		BufferedReader br;
		String line;
		String[] ln;

		if (args[0].equals("entropy")) {
			diversity = 0;
		} else if (args[0].equals("misclassification")) {
			diversity = 1;
		} else if (args[0].equals("gini")) {
			diversity = 2;
		} else {
			System.out.println("Error parsing diversity function. Defaulting to entropy");
			diversity = 0;
		}

		featureNames = new ArrayList<String>();

        /* READ CONFIG FILE*/
		br = new BufferedReader(new FileReader(args[1]));
		possibleLabels = new ArrayList<String>(Arrays.asList((br.readLine().split(","))));
		while ((line = br.readLine()) != null) {
			ln = line.split(",");
			String name = ln[0];
			ArrayList<String> values = new ArrayList<String>();
			for (int i = 1; i < ln.length; i++) {
				values.add(ln[i]);
			}
			possibleFeatureValues.put(name, values);      //map values to feature
			featureNames.add(name);         //for keeping track of feature order
		}

        /*Read Train File*/
		br = new BufferedReader(new FileReader(args[2]));
		while ((line = br.readLine()) != null) {
			ln = line.split(",");
			int number = Integer.parseInt(ln[0]);
			String label = ln[1];       //set clafs
			String[] values = new String[ln.length - 1];
			for (int i = 2; i < ln.length; i++) {
				values[i - 2] = ln[i];
			}
			trainExamples.add(new TrainingExample(number, label, values, featureNames));
		}

        /*Read Test File*/
		br = new BufferedReader(new FileReader(args[3]));
		while ((line = br.readLine()) != null) {
			ln = line.split(",");
			int number = Integer.parseInt(ln[0]);
			String label = ln[1];       //set clafs
			String[] values = new String[ln.length - 1];
			for (int i = 2; i < ln.length; i++) {
				values[i - 2] = ln[i];
			}
			testExamples.add(new TrainingExample(number, label, values, featureNames));
		}
	}

	private static void printHeader() {
		System.out.println("################################################################################");
		System.out.println("# ID3 Implementation                CS1675                          Homework 2 #");
		System.out.println("#------------------------------------------------------------------------------#");
		System.out.println("#                               Steven Saylor                                  #");
		System.out.println("################################################################################");
		System.out.println();
	}
}
