package BayesianNetwork;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BayesianNetwork
 * 
 * @author Di lu, Chenrui Liu
 * 
 *         Bayesian Network represents a probabilistic model describing a set of
 *         random variables and their conditional probabilities. The network is
 *         constructed upon a directed acyclic graph.
 * 
 *         This class is the Java implementation of the network
 * 
 *         This implementation relies on string named variables to differentiate
 *         and identify variables. Builder creational pattern is used to support
 *         complex network construction.
 */
public class BayesianNetwork implements Iterable<String> {

	// the underlying map recording all variables.
	public Map<String, Variable> nodes;

	/**
	 * The constructor.
	 */
	public BayesianNetwork() {
		nodes = new LinkedHashMap<String, Variable>();
	}

	/**
	 * Add node to the network from strings. A node should be added when all of
	 * its parents have been added.
	 * 
	 * This is a builder function where build parts functions are forwarded to
	 * other methods, upon a frailer of building, the operations will be
	 * reverted.
	 * 
	 * @param name
	 *            - a string to represent the variable name like "name"
	 * @param values
	 *            - string array representing the domain of the variable, for
	 *            example ["true", "false"] or ["rainy", "sunny", "overcast"]
	 * @param parents
	 *            - string array denotes the parent nodes of this variable i.e.
	 *            the dependent variables, like ["a", "b", "c", "weather"]
	 * @param probabilities
	 *            - string array describes the conditional or unconditional
	 *            probabilities of the variable, for example [
	 *            "a = true, weather = sunny : 0.8",
	 *            "a = false, weather = sunny : 0.4"] for the variable "a". An
	 *            prior probability for 'a' can be denoted by ["a = True : 0.2,
	 *            a = False:0.7, a = unknown = 0.1]
	 */
	public void addNode(String name, String[] values, String[] parents, String[] probabilities) {
		Variable var = new Variable(this, name);
		nodes.put(name, var);
		try {
			for (String v : values)
				var.addValue(v);
			for (String p : parents)
				var.addParent(p);
			for (String p : probabilities)
				var.addProbability(p);
			for (Variable v : var.parents)
				v.children.add(var);
		} catch (ValidationError e) {
			nodes.remove(name);
			throw e;
		}
	}

	/**
	 * Check if network contains a variable of give name.
	 */
	public boolean hasNode(String name) {
		return nodes.containsKey(name);
	}

	/**
	 * Get the variable of given name.
	 */
	public Variable getNode(String name) {
		if (nodes.containsKey(name))
			return nodes.get(name);
		else
			throw new RuntimeException("No such variable <" + name + ">.");
	}

	/**
	 * Convert string to a condition
	 * 
	 * Taking a string describing a list of events like
	 * "a = true, weather = sunny"
	 */
	public Condition parseCondition(String line) {
		line = line.replaceAll("\\s+", "");
		String[] cond = line.split(","); // where cond (conditions) is like
											// ["a=true", "weather=sunny"]

		List<Event> conditionList = new ArrayList<Event>();
		for (String event : cond)
			if (!event.isEmpty())
				conditionList.add(parseEvent(event));
		return new Condition(conditionList);
	}

	/**
	 * Convert string to an event
	 * 
	 * Taking a string describing a event like "a = true" Requires a equation
	 * sign, a name on its left and a value on the right.
	 */
	public Event parseEvent(String line) {
		line = line.replaceAll("\\s+", "");
		String[] e = line.split("=");
		if (nodes.containsKey(e[0]))
			return nodes.get(e[0]).parseEvent(line);
		else
			throw new RuntimeException("No such variable <" + e[0] + ">.");
	}

	/**
	 * Query the conditional probability table of the given variable
	 */
	public double query(String name, String condition) {
		return getNode(name).getProbability(condition);
	}

	@Override
	public Iterator<String> iterator() {
		return nodes.keySet().iterator();
	}
}
