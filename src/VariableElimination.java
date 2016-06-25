import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import BayesianNetwork.*;

/**
 * The Variable Elimination algorithm implementation in java
 * 
 * @author Chenrui Liu, Di Lu
 * 
 *         This algorithm will work on a BayesianNetwork to compute the prior/posterior
 *         probability of a variable given evidences.
 *
 *         The object has the method ask, which is used to query the
 *         probability. This method can be invoked with a Bayesian Network instance
 *         and a query string in the format:
 *                       A = A1 | B = b2, C = c1
 * 
 *         where the variable on the left of | will be queried and conditions on
 *         the right will be treated as evidences.
 */
public class VariableElimination implements Inference{

	BayesianNetwork network;

	/**
	 * Constructor, specify the net work to be used.
	 * 
	 */
	public VariableElimination(BayesianNetwork network) {
		this.network = network;
	}

	/**
	 * The ask method, will execute variable elimination algorithm on the input
	 * network and return the result value in string.
	 * 
	 * @param query
	 *            - a String in the format "A = a1 | B = b2, C = c1", the
	 *            spacing is not important.
	 * @return - the result value in string.
	 */
	@Override
	public String ask(String query) {
		String[] q = query.split("\\|");
		return ask(q[0], q[1]);
	}

	/**
	 * The underline variable elimination implementation.
	 */
	public String ask(String var, String observed) {
		// Get target event and evidence objects.
		Event target = network.parseEvent(var);
		Condition evidence = network.parseCondition(observed);

		// To eliminate in reverse topological ordering.
		// Assume the insertion order of the network is topologically sorted,
		// which is the case in our implementation.
		List<Variable> order = new ArrayList<Variable>();
		for (Variable v : network.nodes.values())
			order.add(0, v);

		// For each variable, make it into a factor.
		List<Factor> factors = new ArrayList<Factor>();
		for (Variable v : order) {
			factors.add(new Factor(v, evidence));

			// if the variable is a hidden variable, then perform sum out
			if (target.node != v && !evidence.mention(v)) {
				Factor temp = factors.get(0);
				for (int i = 1; i < factors.size(); i++)
					temp = temp.join(factors.get(i));
				temp.eliminate(v);
				factors.clear();
				factors.add(temp);
			}
		}

		// Point wise product of all remaining factors.
		Factor result = factors.get(0);
		for (int i = 1; i < factors.size(); i++)
			result = result.join(factors.get(i));

		// Normalize the result factor
		result.normalise();
		
		// Return the result matching the query in string format.
		return String.format("%.6f", result.p.get(new Condition(Arrays.asList(target))));
	}
}
