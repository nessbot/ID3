import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Steve on 1/29/2015.
 */
public class Example {
	int number;
	String label;       //classifcation label
	HashMap<String, String> featureValues;      //key is feature, value is value

	/**
	 * Constructs a new training example
	 *
	 * @param label       the classification of the training example. What was it's result?
	 * @param values      array of values associated with each feature
	 * @param featureList array of features to use as keys for Feature -> Value hashmap
	 */
	public Example(int number, String label, String[] values, ArrayList<String> featureList) {
		featureValues = new HashMap<String, String>();
		this.number = number;
		this.label = label;
		for (int i = 0; i < featureList.size(); i++) {
			featureValues.put(featureList.get(i), values[i]);
		}
	}

	/**
	 * Returns the value associated with a feature
	 *
	 * @param feature the feature to be assessed
	 * @return the value associated with this feature
	 */
	public String getFeatureValue(String feature) {
		return featureValues.get(feature);
	}

	public int getNumber() {
		return number;
	}

	public String getLabel() {
		return label;
	}

	public HashMap<String, String> getFeatureValues() {
		return featureValues;
	}
}
