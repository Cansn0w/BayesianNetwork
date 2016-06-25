package BayesianNetwork;

/**
 * Value class
 * 
 * Define the domain of random variables.
 * 
 * Bond to the variable upon construction, comparable by its name.
 */
public class Value implements Comparable<Value> {
	public final Variable variable;
	public final String name;

	Value(String name, Variable variable) {
		if (name == null || variable == null)
			throw new ValidationError("Invalid Value content with name: " + name + " and variable: " + variable);
		this.name = name;
		this.variable = variable;
	}

	@Override
	public int compareTo(Value other) {
		return other.name.compareTo(name);
	}

	public String toString() {
		return name;
	}
}
