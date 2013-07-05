package de.freinsberg.pomecaloco;

/**
 * this class represents a pair of objects
 * @author freinsberg
 *
 * @param <L> the left side object type
 * @param <R> the right side object type
 */
public class Pair<L,R> {
    private L l;
    private R r;
    
    /**
     * constructs a pair
     * @param l the left object
     * @param r the right object
     */
    public Pair(L l, R r){
        this.l = l;
        this.r = r;
    }
    
    /**
     * getter for the left object
     * @return returns the left object
     */
    public L getL() {
    	return l;
    }
    
    /**
     * getter for the right object
     * @return returns the right object
     */
    public R getR() {
    	return r;
    }
    
    /**
     * setter for the left object
     * @param l the left object that has to be added
     */
    public void setL(L l) {
    	this.l = l;
    }
    
    /**
     * setter for the right object
     * @param r the right object that has to be added
     */
    public void setR(R r) {
    	this.r = r;
    }
}