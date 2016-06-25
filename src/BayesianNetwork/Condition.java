package BayesianNetwork;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Condition class
 * 
 * Represent a list of variable assignment or random variable outcome.
 * Constructed by a list of events.
 */
public class Condition implements Iterable<Event> {
	private Event[] events;

	public Condition(List<Event> l) {
		Collections.sort(l);
		events = l.toArray(new Event[0]);
	}

	/**
	 * Test supporting map keying.
	 * @param other
	 * @return
	 */
	public boolean equals(Condition other) {
		if (events.length != other.events.length)
			return false;
		for (int i = 0; i < events.length; i++)
			if (!events[i].equals(other.events[i]))
				return false;
		return true;
	}

	public boolean equals(Object other) {
		if (other instanceof Condition)
			return equals((Condition) other);
		return false;
	}

	public int hashCode() {
		int ret = 1;
		for (Event e : events)
			ret *= e.hashCode();
		return ret;
	}

	/**
	 * Contain tests.
	 * @param event
	 * @return
	 */
	public boolean contains(Event event) {
		for (Event e : events)
			if (e.equals(event))
				return true;
		return false;
	}

	public boolean contains(Condition other) {
		for (Event e : other.events)
			if (!contains(e))
				return false;
		return true;
	}

	public boolean mention(Variable var) {
		for (Event e : events)
			if (e.node == var)
				return true;
		return false;
	}

	public String toString() {
		String ret = "[";
		for (Event e : events)
			ret += e + ", ";
		if (events.length > 0)
			ret = ret.substring(0, ret.length() - 2);
		ret += "]";
		return ret;
	}

	@Override
	public Iterator<Event> iterator() {
		return new ConditionItoreror();
	}

	public class ConditionItoreror implements Iterator<Event> {

		private int index;

		public ConditionItoreror() {
			index = 0;
		}

		@Override
		public boolean hasNext() {
			return index < events.length;
		}

		@Override
		public Event next() {
			index++;
			return events[index - 1];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
