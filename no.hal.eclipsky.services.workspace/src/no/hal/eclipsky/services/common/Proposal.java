package no.hal.eclipsky.services.common;

import org.eclipse.jdt.core.CompletionProposal;

public class Proposal{
	private final String name;
	private final String value;
	private final int score;
	
	public Proposal(String name, String value, int score) {
		this.name = name;
		this.value = value;
		this.score = score;
	}
	
	public static Proposal getProposal(CompletionProposal prop){
		if (prop == null) {
			System.out.println("Prop is null");
			return null;
		}
		String output = prop.toString();
		String name = getValueFromProp(output, "name:", ',');
		String value = getValueFromProp(output, "completion:", ',');
		int score = Integer.parseInt(getValueFromProp(output, "relevance:", '}'));
		
		return new Proposal(name, value, score);
	}
	
	private static String getValueFromProp(String prop, String key, char delimeter) {
		int startVal, endVal;
		
		// Extract value
		startVal = prop.indexOf(key) + key.length();
		endVal = prop.indexOf(delimeter, startVal);
		return prop.substring(startVal, endVal);
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public int getScore() {
		return score;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + score;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Proposal other = (Proposal) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (score != other.score)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Proposal [name=" + name + ", value=" + value + ", score="
				+ score + "]";
	}
	
}
