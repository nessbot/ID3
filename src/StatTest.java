import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


/**
 * Created by Steven Saylor on 1/29/2015.
 * CS 1675
 * Assignment 2
 */
public class StatTest {
	private static ArrayList<String> featureNames;
	private static ArrayList<String> possibleLabels;
	private static int diversity;   //value representing diversity function to use
	private static ArrayList<TrainingExample> trainExamples = new ArrayList<TrainingExample>();
	private static ArrayList<TrainingExample> testExamples = new ArrayList<TrainingExample>();
	private static HashMap<String, ArrayList<String>> possibleFeatureValues = new HashMap<String, ArrayList<String>>();
	private static boolean shuffle = false;
	private static final String[] DIVERSITY_NAMES = {"entropy", "gini", "misclassification"};
	private static final int FOLDS = 5;
	private static ArrayList<ArrayList<TrainingExample>> kFold = new ArrayList<ArrayList<TrainingExample>>();
	private static DecimalFormat df = new DecimalFormat("###.###");
	private static PrintWriter writer;

	public static void main(String[] args) throws IOException {
		writer = new PrintWriter("E:\\statsResults.csv");
		try {
			readFiles(args);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Proper usage: ID3 <diversity function> <Config file> <Training File> (optional)<Test File>");
			System.exit(1);
		}
		printHeader();

		if (shuffle) {                            // if only one data set, shuffle data and make train and test set
			Collections.shuffle(trainExamples);


			int n = trainExamples.size() / FOLDS;    // size of each fold
			int remainder = trainExamples.size() % FOLDS;
			for (int i = 0; i < FOLDS; i++) {
				ArrayList<TrainingExample> temp = new ArrayList<TrainingExample>();
				for (int j = 0; j < n; j++) {        // use one quarter of data for test set
					temp.add(trainExamples.get(0));
					trainExamples.remove(0);
				}
				kFold.add(temp);
			}
			for (int i = 0; i < remainder; i++) {            // distribute remaining examples
				kFold.get(i).add(trainExamples.get(0));
				trainExamples.remove(0);
			}
		}
		for (int i = 0; i < FOLDS; i++) {
			testExamples = new ArrayList<TrainingExample>();
			trainExamples = new ArrayList<TrainingExample>();
			System.out.println("####### NEW SET #######");
			testExamples = kFold.get(i);
			for (int j = 0; j < FOLDS; j++) {
				if (j != i) {
					trainExamples.addAll(kFold.get(j));
				}
			}
			testStats(trainExamples, testExamples);
		}
		writer.close();

	}

	private static void testStats(ArrayList<TrainingExample> train, ArrayList<TrainingExample> test) {
		for (int k = 0; k < 3; k++) {
			diversity = k;

			System.out.println("Using " + DIVERSITY_NAMES[k] + " in gain function.");
			ID3Tree id3Tree = new ID3Tree(train, possibleLabels, possibleFeatureValues, diversity);

			int numCorrect = id3Tree.test(train);
			double percent = (((double) numCorrect) / train.size()) * 100;
			System.out.println("The accuracy on the training data is: " + numCorrect + "/" + train.size() +
					" = " + df.format(percent) + "%");

			numCorrect = id3Tree.test(test);
			percent = (((double) numCorrect) / test.size()) * 100;
			System.out.println("The accuracy on the test data is: " + numCorrect + "/" + test.size() +
					" = " + df.format(percent) + "%");
			writer.println(DIVERSITY_NAMES[k] + "," + numCorrect + "," + test.size());
		}
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
			values.addAll(Arrays.asList(ln).subList(1, ln.length));
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
			System.arraycopy(ln, 2, values, 0, ln.length - 2);
			trainExamples.add(new TrainingExample(number, label, values, featureNames));
		}

        /*Read Test File*/
		if (args.length > 3) {
			br = new BufferedReader(new FileReader(args[3]));
			while ((line = br.readLine()) != null) {
				ln = line.split(",");
				int number = Integer.parseInt(ln[0]);
				String label = ln[1];       //set clafs
				String[] values = new String[ln.length - 1];
				System.arraycopy(ln, 2, values, 0, ln.length - 2);
				testExamples.add(new TrainingExample(number, label, values, featureNames));
			}
		} else {
			shuffle = true;
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
