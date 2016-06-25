import java.util.Iterator;

/**
 * Class implementing commonly used static functions by both inference methods.
 */
public class common {

	/**
	 * Join strings by a separator.
	 * 
	 * The same with String.join in Java8
	 */
	public static String join(String sep, Iterable<? extends CharSequence> list) {
		StringBuilder builder = new StringBuilder();
		Iterator<? extends CharSequence> it = list.iterator();
		while (it.hasNext()) {
			builder.append(it.next());
			if (it.hasNext())
				builder.append(sep);
		}
		return builder.toString();
	}
	
	/**
	 * @param s - input string like "-a"
	 * @return convert to "A=F"
	 */
	public static String convert(String s) {
		s = s.toUpperCase();
		if (s.startsWith("-"))
			return s.substring(1) + "=F";
		else
			return s + "=T";
	}
	
	/**
	 * This function will parse a string in the format used in the assignment
	 * to a generic format that is used in the network implementation. 
	 * For example,
	 * 
	 * "P(m|c,-s)" will be converted into "M=T|C=T,S=F" where "T" and "F" are
	 * used to denote true or false in the above network construction section.
	 */
	public static String parseQuery(String query) {
		String[] q = query.split("\\(")[1].split("\\)")[0].split("\\|");

		String ret = common.convert(q[0]) + " | ";

		if (q.length > 1) {
			String[] evidences = q[1].split(",");
			for (int i = 0; i < evidences.length; i++)
				ret += common.convert(evidences[i]) + ", ";
			if (evidences.length > 0)
				ret = ret.substring(0, ret.length() - 2);
		}
		return ret;
	}
}
