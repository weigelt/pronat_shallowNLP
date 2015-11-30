package edu.kit.ipd.parse.shallownlp;

/**
 * This class represents a chunk 
 * @author Markus Kocybik
 */
public class Chunk {
	
	private String name;
	private int predecessor;
	private int successor;
	
	public Chunk[] convertIOB(String[] iob) {
		Chunk[] result = new Chunk[iob.length];
		
		int tmp = 0;
		//set chunk name and calculate the number of predecessors    
		for (int i = 0; i < iob.length; i++) {
			result[i] = new Chunk();
			
			if (iob[i].contains("-")) 
				result[i].name = iob[i].substring(2);
			else
				result[i].name = iob[i];
					
			if (iob[i].startsWith("I") && 
					((iob[i-i].startsWith("B") || iob[i-i].startsWith("I"))
							&& iob[i-1].substring(2).equals(iob[i].substring(2)))) {
				tmp++;
				result[i].predecessor = tmp; 
			}
			else {
				tmp = 0;
				result[i].predecessor = tmp;
			}
		}
		tmp = 0;
		
		// calculate the number of successors
		for (int i = iob.length - 1; i >= 0; i--) {
			if (i < iob.length - 1) {
				if ((iob[i].startsWith("B") || iob[i].startsWith("I"))
 						&& iob[i+1].startsWith("I") 
								&& iob[i].substring(2).equals(iob[i+1].substring(2))) {
					tmp++;
					result[i].successor = tmp; 
				}
				else {
					tmp = 0;
					result[i].successor = tmp;
				}
			}
			else {
				result[i].successor = tmp;
			}
		}	
		return result;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public int getPredecessor() {
		return predecessor;
	}
	public void setPredecessor(int predecessor) {
		this.predecessor = predecessor;
	}
	public int getSuccessor() {
		return successor;
	}
	public void setSuccessor(int successor) {
		this.successor = successor;
	}
	
	
}

