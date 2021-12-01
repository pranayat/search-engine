package com.scoring;

import org.la4j.matrix.functor.MatrixFunction;

public class updateMatrix implements MatrixFunction{
	double val;
	
	public updateMatrix(double val){
		this.val = val;
	}
	
	@Override
	public double evaluate(int i, int j, double value){
		return 1/this.val;
	}
}
