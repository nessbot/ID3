import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * CS1675
 * Assignment 1
 *
 * Created by Steve on 1/29/2015.
 */
public class ID3Tree {
	ArrayList<TrainingExample> s;
	ArrayList<String> possibleLabels;
	ArrayList<String> feats = new ArrayList<String>();
	int diversity;
	int indentAmmount = 0;
	HashMap<String, ArrayList<String>> possibleFeatureValues;
	Node root = new Node();

	/**
	 * Mini collection class to store a double diversity value and numerator used to calculate
	 * that diversity value
	 */
	private class DiversityPair {

		double diversity;
		int n;

		private DiversityPair(Double d, int n) {
			this.diversity = d;
			this.n = n;
		}

	}

	/**
	 * Mini collection class to store a a label - frequency pair
	 */
	private class LabelPair {

		String label;
		int freq;

		private LabelPair(String l, int f) {
			this.label = l;
			this.freq = f;
		}

	}

	public ID3Tree(ArrayList<TrainingExample> s, ArrayList<String> possibleLabels,
				   HashMap<String, ArrayList<String>> features, int diversity) {
		this.s = s;
		this.possibleLabels = possibleLabels;
		this.possibleFeatureValues = features;
		this.diversity = diversity;
		this.indentAmmount = 0;
		feats.addAll(features.keySet());
		runID3(s, feats);
	}

	/**
	 * Tests decision tree on a test set
	 *
	 * @param s test set of examples
	 * @return number of correct guesses
	 */
	public int test(ArrayList<TrainingExample> s) {
		int numCorrect = 0;

		for (TrainingExample x : s) {
			String correctLabel = x.getLabel();
			String guessLabel = recTest(root, x);
			if (correctLabel.equals(guessLabel)) {
				numCorrect++;
			}
		}
		return numCorrect;
	}

	/**
	 * Traverses the tree by following values from an test example
	 *
	 * @param x example being examined
	 * @return the label associated with this path
	 */
	private String recTest(Node node, TrainingExample x) {

		if (node.label != null) {
			return node.label;
		} else {
			String value = x.getFeatureValue(node.getFeature());        //find feature value from example that matches the current nodes's feature
			return recTest(node.getChild(value), x);
		}

	}

	/**
	 * Method called to print the data stored in the tree to standard out
	 * calls recPrintTree to recursively print the tree
	 */
	public void printTree() {
		this.indentAmmount = 0;
		recPrintTree(root);
	}

	/**
	 * Recursively traverses the decision tree to print the entries in a human-readable form
	 * Called by public method printTree
	 *
	 * @param node the decision tree node to be examined
	 */
	private void recPrintTree(Node node) {
		String indent = changeIndent(indentAmmount);
		if (node.getLabel() != null) {                    //base case - leaf node
			System.out.println("  " + node.printLabelInfo());
		} else {
			HashMap<String, Node> children = node.getChildren();
			for (Map.Entry<String, Node> child : children.entrySet()) {
				System.out.print(indent + node.getFeature() + "=" + child.getKey());
				indentAmmount++;
				if (child.getValue().getLabel() == null) {    //if next call is to a leaf node, next line
					System.out.println();
				}
				recPrintTree(child.getValue());
				indentAmmount--;
			}
		}
	}

	/**
	 * ID3 algorithm starting point. Called by constructor to build decision tree.
	 *
	 * @param s     set of training examples
	 * @param feats set of features
	 */
	private void runID3(ArrayList<TrainingExample> s, ArrayList<String> feats) {
		this.root = recID3(s, feats);
	}

	/**
	 * Recursive ID3 algorithm. Builds decision tree recursively.
	 *
	 * @param s     set of training examples
	 * @param feats set of features
	 */
	private Node recID3(ArrayList<TrainingExample> s, ArrayList<String> feats) {
		//create root node
		Node root = new Node();
		if (haveSameLabel(s)) {                                           // if everything in S has the same label
			root.setLabel(s.get(0).label);                              // set root's label to the ubiquitous label
			root.setLabelFraction(s.size(), s.size());                    // set fraction of label to 1
			return root;                                                // and return it
		}
		if (feats.isEmpty()) {                                          // if vector of features is empty
			LabelPair lp = getCommonLabel(s);
			root.setLabel(lp.label);                                    // return the most likely label (defaults to first label)
			root.setLabelFraction(lp.freq, s.size());
			return root;
		} else {
			HashMap<String, Double> gains = new HashMap<String, Double>();  // calculate and store gains of each feature
			for (String feature : feats) {
				gains.put(feature, findGain(feature, s));
			}
			String bestFeature = getBestFeature(gains);                     // find the best feature i.e. highest gain
			root.setFeature(bestFeature);                                   // associate current node with the best feature
			for (String value : possibleFeatureValues.get(bestFeature)) {   // now only looking at best feature
				ArrayList<TrainingExample> sv = new ArrayList<TrainingExample>();           // generate subset of s where
				ArrayList<String> newFeats = new ArrayList<String>();

				for (String f : feats) {                                    // make new feature vector without
					if (!f.equals(bestFeature))                             //  the best feature
						newFeats.add(f);
				}
				for (TrainingExample x : s) {                                       // generate subset of examples comprised of
					if (x.getFeatureValue(bestFeature).equals(value)) {     //  examples wherebest feature equals the
						sv.add(x);                                          //  currently examnined value (Sv)
					}
				}

				if (!sv.isEmpty()) {                                        // make child of this node from subset Sv
					Node tempNode = recID3(sv, newFeats);                   //  using remaining features (newFeats)
					root.addChild(value, tempNode);
				} else {
					LabelPair lp = getCommonLabel(s);                        // if Sv is an empty set create a leaf node
					root.addChild(value, new Node(lp.label));                //  with most common label of current set, S
				}
			}
			return root;
		}
	}

	/**
	 * Calculates the information gain of a set
	 * uses diversity method set during class construction
	 *
	 * @param feature the feature which defines the set
	 * @param s       the set being examined
	 * @return the information gain value between 0 and 1
	 */
	private double findGain(String feature, ArrayList<TrainingExample> s) {
		ArrayList<String> values = possibleFeatureValues.get(feature);
		ArrayList<DiversityPair> diversityPairs = new ArrayList<DiversityPair>();
		DiversityPair tempDiversity;
		Double setDiversity = 0.0;

		for (String value : values) {                                           // for each possible values of the examined feature
			ArrayList<TrainingExample> sfv = getFeatureValueSet(feature, value, s);        // Subset of s, S(f=v)
			switch (diversity) {                                                // calculate diversity
				case 0:
					tempDiversity = entropy(sfv);
					break;
				case 1:
					tempDiversity = misclassifcation(sfv);
					break;
				case 2:
					tempDiversity = gini(sfv);
					break;
				default:                                                        // probably irrelevant but here for debugging
					throw new IllegalArgumentException();
			}
			diversityPairs.add(tempDiversity);                                  // collect diversity values, plus the
		}                                                                       //  size of their respective sets S(x=v)
		switch (diversity) {
			case 0:
				setDiversity = entropy(s).diversity;
				break;
			case 1:
				setDiversity = misclassifcation(s).diversity;
				break;
			case 2:
				setDiversity = gini(s).diversity;
		}
		//calculate gain
		double subsetDiversity = 0;                                                     // sum of diversity of S(x=v) sets
		int d = s.size();                                                       // size of example sets
		for (DiversityPair dp : diversityPairs) {
			subsetDiversity += (((double) dp.n / d) * dp.diversity);
		}
		return (setDiversity - subsetDiversity);
	}

	/**
	 * Calculates the entropy of set of examples
	 *
	 * @param s set of examples used for calculation
	 * @return a DiversityPair containing the entropy value and the size of the set  //todo: make this just return entropy
	 */
	private DiversityPair entropy(ArrayList<TrainingExample> s) {
		int d = s.size();
		int n;                                                         // numerator and denom for probability calc
		double tempEnt;
		double result = 0;

		for (String label : possibleLabels) {
			n = 0;
			for (TrainingExample x : s) {
				if (x.getLabel().equals(label)) {
					n++;
				}
			}
			double p = (double) n / d;                                      // probability
			if (p == 0) {
				tempEnt = 0;
			} else {
				tempEnt = -(p * (Math.log(p) / Math.log(2)));
			}
			result += tempEnt;
		}
		return new DiversityPair(result, d);
	}

	/**
	 * Calculates the misclassification value for a set
	 *
	 * @param s the set of examples being tested
	 * @return a DiversityPair containing the misclassification value and the size of the set  //todo: make this just return misclass value
	 */
	private DiversityPair misclassifcation(ArrayList<TrainingExample> s) {
		int d = s.size();
		int n = 0;
		String max = getCommonLabel(s).label;
		for (TrainingExample x : s) {
			if (x.getLabel().equals(max)) {
				n++;
			}
		}
		double p = (double) n / d;

		return new DiversityPair(1 - p, d);
	}

	/**
	 * Calculates the gini value for a set
	 *
	 * @param s the set of examples being tested
	 * @return a DiversityPair containing the gini value and the size of the set  //todo: make this just return gini value
	 */
	private DiversityPair gini(ArrayList<TrainingExample> s) {
		int d = s.size();
		int n;
		double result = 0;
		for (String label : possibleLabels) {
			n = 0;
			for (TrainingExample x : s) {                                           // for cases where the example's feature
				if (x.getLabel().equals(label)) {
					n++;
				}
			}
			double p = (double) n / d;
			double tempGini = p * p;
			result += tempGini;
		}
		return new DiversityPair(1 - result, d);                                // since gini = 1-(Sum(p^2) over labels)
	}

	/**
	 * Generates subset of s, containing only examples in which the selected feature matches has the selected value
	 *
	 * @param feature string feature name that is being tested
	 * @param value   string value name against which the feature is being tested
	 * @param s       collection of examples from which the set will be generated
	 * @return subset containing only those examples where the feature had the value
	 */
	private ArrayList<TrainingExample> getFeatureValueSet(String feature, String value, ArrayList<TrainingExample> s) {
		ArrayList<TrainingExample> tempS = new ArrayList<TrainingExample>(s);
		Iterator itr = tempS.iterator();
		while (itr.hasNext()) {
			TrainingExample test = (TrainingExample) itr.next();
			if (!test.getFeatureValue(feature).equals(value)) {
				itr.remove();
			}
		}
		return tempS;
	}

	/**
	 * Finds the most useful feature for an example set by compairing gains
	 * most useful = highest gain
	 *
	 * @param gains HashMap of feature => gain
	 * @return the feature from gains with the highest gain
	 */
	private String getBestFeature(HashMap<String, Double> gains) {
		String feature = null;
		double max = 0.;
		for (Map.Entry pair : gains.entrySet()) {
			if (Double.compare((Double) pair.getValue(), max) >= 0) {             //if gain is greater than max
				max = (Double) pair.getValue();
				feature = (String) pair.getKey();
			} // todo: possibly just quit if max = 1 since it will be max; may save time for large datasets
		}
		return feature;
	}

	/**
	 * Finds the most common label of a set
	 *
	 * @param s set of examples
	 * @return LabelPair containing the label value and it's frequency in s
	 */
	private LabelPair getCommonLabel(ArrayList<TrainingExample> s) {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		int max = 0;
		for (String label : possibleLabels) {
			counts.put(label, 0);
		}
		for (TrainingExample x : s) {                             //go through
			counts.put(x.label, counts.get(x.label) + 1);
			if (counts.get(x.label) > max) {
				max++;
			}
		}
		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			if (entry.getValue() == max) {
				return new LabelPair(entry.getKey(), max);
			}
		}
		return null;
	}

	/**
	 * Examines example set to see if all examples have the same label
	 *
	 * @param s example set to be examined
	 * @return true if every example in the set has the same label,
	 * false otherwise
	 */
	private boolean haveSameLabel(ArrayList<TrainingExample> s) {
		String test = s.get(0).label;
		for (TrainingExample ex : s) {
			if (!ex.label.equals(test)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the indent ammount used by @Method recPrintTree
	 *
	 * @param x the number of times to indent by
	 * @return a string of spaces based on the ammount to indent by
	 */
	private String changeIndent(int x) {
		String result = "";
		for (int i = 0; i < x; i++) {
			result = result + "  ";
		}
		return result;
	}
}
