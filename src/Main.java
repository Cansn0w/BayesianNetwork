import java.io.*;
import BayesianNetwork.*;

public class Main {
	
	/**
	 * @author Chenrui Liu, Di Lu
	 * 
	 * To enter testing mode:
	 *  - give a command line argument in the format "QUERY METHOD #ITERATION #SAMPLES(if the METHOD is MCMC)"
	 *  - this will time the performance of a specified method by computing a query for a number of times. ]
	 *  
	 * To enter inferencing mode:
	 *  - don't give any command line argument
	 *  - then give inputs line by line, following the format described by the assignment spec. 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length > 0)
			time(args);
		else
			infer();
	}
	
	/**
	 * This is the construction of the network described specified by the assignment.
	 * Note: You can design your own Bayesian network, given any name or value for each variable
	 * and specifying the parent->child relationship. The algorithms can cope with general graphs
	 * 
	 * Also note: Here "T" is used to denote true (happened) and F is used to denote false
	 * (not happened)
	 */
	public static BayesianNetwork getNetwork() {
		BayesianNetwork net = new BayesianNetwork();
		
		net.addNode("M", new String [] {"T", "F"}, new String[0], new String [] {"M = T: 0.2", "M = F: 0.8"});
		net.addNode("I", new String [] {"T", "F"}, new String[] {"M"}, new String [] {
				"I = T, M = T: 0.8",
				"I = T, M = F: 0.2",
				"I = F, M = T: 0.2",
				"I = F, M = F: 0.8",
				});
		net.addNode("B", new String [] {"T", "F"}, new String[] {"M"}, new String [] {
				"B = T, M = T: 0.2",
				"B = T, M = F: 0.05",
				"B = F, M = T: 0.8",
				"B = F, M = F: 0.95",
				});
		net.addNode("C", new String [] {"T", "F"}, new String[] {"I", "B"}, new String [] {
				"C = T, I = T, B = T: 0.8",
				"C = T, I = T, B = F: 0.8",
				"C = T, I = F, B = T: 0.8",
				"C = T, I = F, B = F: 0.05",
				"C = F, I = T, B = T: 0.2",
				"C = F, I = T, B = F: 0.2",
				"C = F, I = F, B = T: 0.2",
				"C = F, I = F, B = F: 0.95",
				});
		net.addNode("S", new String [] {"T", "F"}, new String[] {"B"}, new String [] {
				"S = T, B = T: 0.8",
				"S = T, B = F: 0.6",
				"S = F, B = T: 0.2",
				"S = F, B = F: 0.4",
				});
		return net;
	}

	/**
	 * This function will time the performance of VE/ MCMC.
	 */
	public static void time(String[] args) {

		BayesianNetwork net = getNetwork();
		
		try {
			String query = args[0];
			String inferenceMethod = args[1];
			int times = Integer.parseInt(args[2]);
			int n_sample = args.length > 3 ? Integer.parseInt(args[3]) : 0; // optional

			if (inferenceMethod.equals("VE")) {
				System.out.println("VE computing " + query +  " with " + times + " iterations.");
				double start = System.currentTimeMillis();
				Inference ve = new VariableElimination(net);
				
				for (int i = 0; i < times; ++i)
					ve.ask(common.parseQuery(query));
				System.out.println(System.currentTimeMillis() - start + " ms");
				
			} else if (inferenceMethod.equals("MCMC")) {
				System.out.println("MCMC computing " + query + " with " + times + " iterations using sample size" + n_sample);
				Inference mcmc = new MarkovChainMonteCarlo(net, n_sample);
				double start = System.currentTimeMillis();
				
				for (int i = 0; i < times; ++i)
					mcmc.ask(common.parseQuery(query));
				System.out.println(System.currentTimeMillis() - start + " ms");
			} else {
				throw new RuntimeException();
			}
		} catch (Exception e) {
			throw new RuntimeException("Command not recogonised.\nPlease input arguments in the format:\n    QUERY Method n_Iteration addition_arguments\nFor example:\nP(m|s,-c) MCMC 1000 10\n");
		}
	}

	/**
	 * This function will fulfill the task describe in the assignment spec to
	 * execute based on content from standard input
	 */
	public static void infer() throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		String inference = in.readLine();
		int n_lines = Integer.parseInt(in.readLine());
		
		BayesianNetwork net = getNetwork();
		
		Inference inferenceMethod;
		StringBuilder answers = new StringBuilder();

		if (inference.split("\\s+")[0].equals("VE")) {
			inferenceMethod = new VariableElimination(net);
		} else if (inference.split("\\s+")[0].equals("MCMC")) {
			int nSamples = Integer.parseInt(inference.split(" ")[1]);
			inferenceMethod = new MarkovChainMonteCarlo(net, nSamples);
		} else {
				throw new RuntimeException();
			}
		while (--n_lines >= 0)
			answers.append(inferenceMethod.ask(common.parseQuery(in.readLine())) + "\n");
		System.out.print(answers.toString());
		in.close();
	}
}
