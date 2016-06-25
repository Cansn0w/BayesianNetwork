
/**
 * A common interface to be implemented by inference methods i.e.
 * VariableElimination and MarkovChainMonteCarlo
 */
public interface Inference {

	public String ask(String query);
}
