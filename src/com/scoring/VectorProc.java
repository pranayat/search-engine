package com.scoring;


import org.la4j.vector.functor.VectorProcedure;
import org.la4j.Vector;

public class VectorProc implements VectorProcedure{
	
	double val;
	
	public VectorProc(double val){
		this.val = val;
	}
	
	@Override
	public void apply(int i, double value) {
		value = (1/this.val);
	}

}
