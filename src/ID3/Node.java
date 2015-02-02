package ID3;

import java.util.HashMap;

/**
 * Created by Steve on 1/29/2015.
 * todo: split between leaf and  internal node classes
 */
public class Node {
	String label;
	int labelN;
	int labelD;
	String feature;
	HashMap<String, Node> children = new HashMap<String, Node>();

	public Node() {
	}

	public Node(String label) {
		setLabel(label);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		this.feature = null;      //probably not needed but just to ensure that a node can't have both
	}

	/**
	 * Adds child to to map value -> child
	 *
	 * @param value value of feature used for link
	 * @param child the node to which that value-link points
	 */
	public void addChild(String value, Node child) {
		children.put(value, child);
	}

	public Node getChild(String value) {
		return children.get(value);
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
		this.label = null;      //probably not needed but just to ensure that a node can't have both
	}

	public HashMap<String, Node> getChildren() {
		return children;
	}

	public void setLabelFraction(int n, int d) {
		this.labelN = n;
		this.labelD = d;
	}

	public String printLabelInfo() {
		return (label + " : " + labelN + "/" + labelD);
	}
}
