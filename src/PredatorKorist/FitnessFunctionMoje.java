package PredatorKorist;

import vivae.arena.Arena;
import vivae.example.IExperiment;
import vivae.example.TestExperiment;

/**
 * Created by IntelliJ IDEA.
 * User: koutnij
 * Date: Nov 9, 2009
 * Time: 9:20:33 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FitnessFunctionMoje {
	public IExperiment exp;
    public abstract double getFitness();

    public FitnessFunctionMoje( IExperiment exp ){
    	setExperiment(exp);
    }
	public void setExperiment(IExperiment exp) {
		this.exp = exp;		
	}
}
