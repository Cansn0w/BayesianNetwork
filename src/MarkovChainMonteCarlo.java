import BayesianNetwork.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * An instance of Markov Chain Monte Carlo algorithm called Gibbs Sampling implemented in java
 * 
 * @author Chenrui Liu, Di Lu
 * 		   
 * 		   This algorithm is invoked to compute a query in the same way with VariableElimination class.
 * 		   The additional information needed to feed in is the number of samples it will generate.
 * 		   
 * 		   MCMC is an approximate algorithm that generates samples from the data set conditioned
 * 		   on the distribution field to reveal the probability of the queried variable. 
 */

public class MarkovChainMonteCarlo implements Inference{

	Random r; // random value generator
	BayesianNetwork bn;
	int nSamples;

	
	public MarkovChainMonteCarlo(BayesianNetwork bn, int nSamples) {
		this.bn = bn;
		this.r = new Random();
		this.nSamples = nSamples;
	}

	// dist := {ValueName -> # of occurrence}
	/**
	 * Normalise the probability distribution of the queried variable's domain,
	 * @param dist - a map of value namea to its probability, eg. T -> 0.5
	 */
	public void normaliseDistribution(Map<String, Double> dist) {
		double sum = 0.0;
		for (Double d : dist.values()) sum += d;
		for (Entry<String, Double> ent : dist.entrySet())
			// normalize and convert to percentage
			ent.setValue(100.0 * (ent.getValue() / sum));
	}

	/**
	 * Computes the probability of the variable given its parents
	 * 
	 * @param var - Xi, a random variable
	 * @param value - a possible outcome of Xi, namely xi
	 * @param state - the current state, represented as a map of <variable name -> value>
	 * @return P(xi | Parents(Xi))
	 */
	public double computePrbGivenParent(Variable var, String value, Map<String, String> state) 
			throws ValidationError {
		List<String> cond = new ArrayList<String>();
		cond.add(var.name + "=" + value);
		for (Variable parent : var.parents)
			cond.add(parent.name + "=" + state.get(parent.name));
		return bn.query(var.name, common.join(",", cond));
	}

	/**
	 * Conditioned on the current state configuration, sample from the domain
	 * of the selected variable given its Markov Blanket.
	 * ie, sampling conditioned on P(xi | mb(Xi)) 
	 *                             = alpha * PRODUCT_OVER: P(zj | Parents(Zj)), for each child Zj of Xi 
	 * 
	 * @param var - Xi, from whose domain a new value is sampled
	 * @param state - the current configuration of all variables' values
	 * @return xi
	 */
	public String getSample(Variable var, Map<String, String> state) throws ValidationError {
		
		// dist := {ValueName -> probability}
		Map<String, Double> distribution = new HashMap<String, Double>();

		// calculate probability of each possible outcome of var
		for (String value : var.domain.keySet()) {
			double p = 0.0;
			
			// 1. P(xi | Parent(Xi))
			p += computePrbGivenParent(var, value, state);

			// 2. PRODUCT_OVER: P(zj | Parents(Zj)), for each child Zj of Xi 
			state.put(var.name, new String(value)); 
			for (Variable child : var.children) {
				p *= computePrbGivenParent(child, state.get(child.name), state);
			}
			distribution.put(value, p);
		}

		normaliseDistribution(distribution);
		// lottery! 
		double i = r.nextInt(100) + r.nextDouble();
		Double cummulation = 0.0;
		for (Entry<String, Double> ent : distribution.entrySet()) {
			cummulation += ent.getValue();
			if (cummulation >= i)
				return ent.getKey();
		}
		
		return null; // this should never be returned
	}
	
	// each query is a pair like <"C", {"C=T", "I=F", "B=F"}>
	/**
	 * Perform Gibbs sampling on the Bayesian network.
	 * 
	 * @param cause - name of the queried variable, eg. "C"
	 * @param spec - the specification of variable names in the query, eg. {"C=T", "I=F", "B=F"}
	 * @return answer to the query. 
	 */
	public String ask(String cause, List<String> spec) throws ValidationError {
		double probability;
		try {
			// First try if the query can be answered directly without inferencing
			return String.format("%.6f", bn.query(cause, common.join(",", spec)));
			
		} catch (Exception oooppps) { // otherwise we need to perform inference
			// a possible configuration of the whole sample space, maps: < name -> value >
			Map<String, String> state = new LinkedHashMap<String, String>(); 
			String queryValue = null;

			// EVIDENCES
			List<String> evidenceNames = new ArrayList<String>();
			for (String e : spec) {
				String name = e.split("=")[0];
				if (!name.equals(cause)) {
					String value = e.split("=")[1];
					state.put(name, value); // the evidence variables are fixed
					evidenceNames.add(name);
				} else {
					queryValue = e.split("=")[1]; // store the query value, eg. if A=T is the cause, T is query value.
				}
			}
			// NON-EVIDENCES
			List<String> nonEvdNames = new ArrayList<String>();
			for (String name : bn.nodes.keySet()) {
				if (!evidenceNames.contains(name)) {
					nonEvdNames.add(name);
					Object[] tmp = bn.getNode(name).domain.keySet().toArray();
					state.put(name
			               , (String) tmp[r.nextInt(tmp.length)]); // assign random value for non-evd
				}
			}

			// Traveling around variables and 'flip' values 
			int counter = 0;  // count # of occurrences of the queried value
			for (int i = 0; i < nSamples; ++i) {
				/*
				 * There can be multiple ways to choose the order of picking up non-evidence variables, 
				 *  1. select randomly 
				 *  2. cycle through
				 */
			//	 Variable var = bn.getNode(nonEvdNames.get(i % nonEvdNames.size()));   // 1. CYCLE

				 Collections.shuffle(nonEvdNames);									   // 2. RANDOM
				 Variable var = bn.getNode(nonEvdNames.get(0));

				// draw a new sample
				String newVal = getSample(var, state);
				// update state
				state.put(var.name, newVal);
				// update counter
				if (state.get(cause).equals(queryValue))
					++counter;
			}
			probability = (double) counter/ nSamples;
			return String.format("%.6f", probability);
		}
	}
	
	@Override
	/**
	 * The ask method, will perform Gibbs sampling algorithm on the input
	 * network and return the result probability in string.
	 * 
	 * @param query - a String in the format "A = a1 | B = b2, C = c1", the spacing is not important.
	 * @return - the answer to the query
	 */
	public String ask(String query) {
		query = query.replaceAll("\\s+", "");
		String[] contents = query.split("\\|");
		List<String> evidences = new ArrayList<String>();
		
		String cause = contents[0].substring(0, contents[0].indexOf('=')); // get cause name
		evidences.add(contents[0]); // add the cause-value pair to the evidence list
		
		if (contents.length > 1) {
			String[] tmp = contents[1].split(",");
			for (String elem : tmp)
				evidences.add(common.convert(elem));
		}
		
		return ask(cause, evidences);
	}
}
