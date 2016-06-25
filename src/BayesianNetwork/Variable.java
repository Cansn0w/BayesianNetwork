package BayesianNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Variable class
 * 
 * Represent a random variable in Bayesian network.
 * 
 * This implementation enclose a directed network structure by recording the
 * parents and children nodes.
 * 
 * This class takes partial responsibility of construction the network that will
 * be called from network class.
 */
public class Variable {
	public final String name;
	public List<Variable> parents;
	public List<Variable> children;
	public Map<String, Value> domain;
	public Map<Condition, Double> probabilities;
	public BayesianNetwork network;

	public Variable(String name) {
		this.name = name;
		parents = new ArrayList<Variable>();
		children = new ArrayList<Variable>();
		domain = new HashMap<String, Value>();
		probabilities = null;
	}

	public Variable(BayesianNetwork net, String name) {
		this(name);
		bind(net);
	}

	public void bind(BayesianNetwork net) {
		network = net;
	}

	/**
	 * Parse a string into an event object.
	 * 
	 * @param line - e.g. "a=true"
	 */
	public Event parseEvent(String line) {
		line = line.replaceAll("\\s+", "");
		String[] e = line.split("=");
		if (e.length != 2)
			throw new ValidationError("Expected \"variable=value\", received " + line);

		if (network.hasNode(e[0]))
			return new Event(network.getNode(e[0]), e[1]);
		else
			throw new ValidationError("No such variable <" + e[0] + ">.");
	}

	/**
	 * Parse a string into an condition object.
	 * @param line - e.g. "a=true, weather=sunny"
	 * 
	 */
	public Condition parseCondition(String line) {
		line = line.replaceAll("\\s+", "");
		String[] cond = line.split(",");
		if (cond.length != parents.size() + 1)
			throw new ValidationError(
					"Number of events (" + cond.length + ") mismatches required number (" + parents.size() + 1 + ").");

		List<Event> conditionList = new ArrayList<Event>();
		for (String event : cond) {
			conditionList.add(parseEvent(event));
		}

		return new Condition(conditionList);
	}

	/**
	 * Index the probability by a condition
	 */
	public Double getProbability(String cond) {
		return probabilities.get(parseCondition(cond));
	}

	/**
	 * Builder methods
	 */
	public void addParent(String name) {
		try {
			addParent(network.getNode(name));
		} catch (ValidationError e) {
			throw new ValidationError("The specified parent node \"" + name + "\" does not exist (yet).");
		}
	}

	public void addParent(Variable parent) {
		if (parent != null)
			parents.add(parent);
		else
			throw new ValidationError("The specified parent node does not exist (yet).");
	}

	public void addValue(String name) {
		if (name == null)
			throw new ValidationError("Invalid value name \"" + name + "\".");
		if (domain.containsKey(name))
			throw new ValidationError("Value with name \"" + name + "\"already exists.");
		domain.put(name, new Value(name, this));
	}

	/**
	 * 
	 * @param line - e.g, "a = true, weather = sunny : 0.8"
	 */
	public void addProbability(String line) {
		if (probabilities == null) {
			probabilities = new HashMap<Condition, Double>();

			List<Variable> varList = new ArrayList<Variable>(parents);
			varList.add(this);

			for (Condition c : allConditions(varList))
				probabilities.put(c, 0.0);
		}
		line = line.replaceAll("\\s+", "");

		String[] desc = line.split(":"); // where desc (description) is like
											// ["a=true,weather=sunny", "0.8"]
		if (desc.length != 2)
			throw new ValidationError("Only one ':' in an description allowed.");

		Double probability = Double.parseDouble(desc[1]);

		Condition condition = parseCondition(desc[0]);

		if (probabilities.containsKey(condition)) {
			probabilities.put(condition, probability);
		} else {
			throw new ValidationError("Provided condition mismatch.");
		}
	}

	public Value getValue(String name) {
		return domain.get(name);
	}

	/**
	 * Method to get all combination of variable outcome.
	 * 
	 * This method will make recursive call to the underneath method to generate
	 * the list.
	 */
	public static List<Condition> allConditions(List<Variable> varList) {
		List<Condition> ret = new ArrayList<Condition>();

		fill(varList, ret, new ArrayList<Event>());

		return ret;
	}

	/**
	 * A recursive method to get all combinations of variable outcome
	 * 
	 */
	private static void fill(List<Variable> src, List<Condition> dest, List<Event> walked) {
		if (src.size() == walked.size()) {
			dest.add(new Condition(walked));
			return;
		}
		Variable current = src.get(walked.size());
		for (Value v : current.domain.values()) {
			List<Event> w = new ArrayList<Event>(walked);
			w.add(new Event(current, v));
			fill(src, dest, w);
		}
	}

	public String toString() {
		String ret = name + ": [";
		if (!parents.isEmpty()) {
			for (Variable v : parents) {
				ret += v.name + ", ";
			}
			ret = ret.substring(0, ret.length() - 2);
		}
		ret += "]";
		return ret;
	}
}
