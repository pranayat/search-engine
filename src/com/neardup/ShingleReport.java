package com.neardup;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Collections;
import java.lang.Object;

import java.util.Arrays;

import com.panayotis.gnuplot.GNUPlot;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.plot.Plot;

public class ShingleReport {
	
	private Connection conn;
	
	public ShingleReport(Connection conn) {
		this.conn = conn;
	}
	
	public static<T> T[] subArray(T[] array, int beg, int end) {
        return Arrays.copyOfRange(array, beg, end + 1);
    }

	public float[] report(float[] errors, int count) {
		try {
			//get pairs of jaccard and approx_jaccard values
			List<Float> diffsimvalues = new ArrayList<Float>();
			int numvalues = 0;
			float sumdiffsimvalues = 0;
			float diffsim;
			PreparedStatement pstmtsim = conn.prepareStatement("SELECT jaccard, approx_jaccard from docsimilarities");
			ResultSet rssim = pstmtsim.executeQuery();
			while(rssim.next()) {
				
				diffsim = Math.abs(rssim.getFloat("jaccard")-rssim.getFloat("approx_jaccard"));
				sumdiffsimvalues += diffsim;
				diffsimvalues.add(diffsim);
				numvalues+=1;
			}
			
			// average, the median, and the first and third quartile of the observed absolute errors
			Collections.sort(diffsimvalues);
			if(count==0) {
				errors[0] = sumdiffsimvalues/numvalues; //average
				errors[1] = diffsimvalues.get((int)Math.floor(numvalues/2)); //median
				errors[2] = diffsimvalues.get((int)Math.floor(numvalues/4)); //firstquantile
				errors[3] = diffsimvalues.get(numvalues - (int)Math.floor(numvalues/4)); //thirdquantile
			}else if(count==1) {
				errors[4] = sumdiffsimvalues/numvalues; //average
				errors[5] = diffsimvalues.get((int)Math.floor(numvalues/2)); //median
				errors[6] = diffsimvalues.get((int)Math.floor(numvalues/4)); //firstquantile
				errors[7] = diffsimvalues.get(numvalues - (int)Math.floor(numvalues/4)); //thirdquantile
			}else if(count==2) {
				errors[8] = sumdiffsimvalues/numvalues; //average
				errors[9] = diffsimvalues.get((int)Math.floor(numvalues/2)); //median
				errors[10] = diffsimvalues.get((int)Math.floor(numvalues/4)); //firstquantile
				errors[11] = diffsimvalues.get(numvalues - (int)Math.floor(numvalues/4)); //thirdquantile
			}else {
				errors[12] = sumdiffsimvalues/numvalues; //average
				errors[13] = diffsimvalues.get((int)Math.floor(numvalues/2)); //median
				errors[14] = diffsimvalues.get((int)Math.floor(numvalues/4)); //firstquantile
				errors[15] = diffsimvalues.get(numvalues - (int)Math.floor(numvalues/4)); //thirdquantile
			}
			
//			System.out.println(average);
//			System.out.println(median);
//			System.out.println(firstquartile);
//			System.out.println(thirdquartile );
			return errors;
			
		} catch (SQLException e) {
	    	   System.out.println(e);
	    	   try {
	    		   conn.rollback();
	    	   } catch (SQLException e1) {
	    		   e1.printStackTrace();
	    	   }
	       }
		return errors;
	}
	
	public void plot(float[] errors) {

		float[][] ploterrors = new float[errors.length][2];
		float[][] averageerrors = new float[4][2];
		float[][] medianerrors = new float[4][2];
		float[][] fqerrors = new float[4][2];
		float[][] tqerrors = new float[4][2];
		int count = 0;
		for (int i = 0;i<errors.length;i++) {
			if (i<4) {
				ploterrors[i][0] = 1;
			} else if (i<8) {
				ploterrors[i][0] = 4;
			} else if (i<12) {
				ploterrors[i][0] = 16;
			} else {
				ploterrors[i][0] = 32;
			}
			ploterrors[i][1] = errors[i];
		}
		System.out.println(Arrays.toString(errors));
		int count1=0;
		int count2=0;
		int count3=0;
		int count4=0;
		for (int i = 0; i<ploterrors.length;i++) {
			if (i%4==0) {
				averageerrors[count1] = ploterrors[i];
				count1++;
			}else if (i%4==1) {
				medianerrors[count2] = ploterrors[i];
				count2++;
			}else if (i%4==2) {
				fqerrors[count3] = ploterrors[i];
				count3++;
			}else {
				tqerrors[count4] = ploterrors[i];
				count4++;
			}
		}
		
//	    for (int i = 0; i<4; i++) {
//	    	for (int j= 0; j<4; j++) {
//	    		if (i==0) {
//	    			ploterrors[0][i]=errors[i][j];
//	    		}
//	    		ploterrors[j][i]=errors[i][j];
//	    		System.out.println(errors[i][j]);
//	    	}
//	    }
	   
		JavaPlot p = new JavaPlot();
	    p.setTitle("Error-Plot");
	    p.getAxis("x").setLabel("n", "Arial", 20);
	    p.getAxis("y").setLabel("error");
	    p.getAxis("x").setBoundaries(0, 40);
	    p.setKey(JavaPlot.Key.TOP_RIGHT);
	    DataSetPlot s1 = new DataSetPlot(averageerrors);
	    s1.setTitle("Average");
	    p.addPlot(s1);
	    DataSetPlot s2 = new DataSetPlot(medianerrors);
	    s2.setTitle("Median");
	    p.addPlot(s2);
	    DataSetPlot s3 = new DataSetPlot(fqerrors);
	    s3.setTitle("first Quantile");
	    p.addPlot(s3);
	    DataSetPlot s4 = new DataSetPlot(tqerrors);
	    s4.setTitle("third Quantile");
	    p.addPlot(s4);
	    p.plot();
	    
//		GNUPlot jg = new GNUPlot();
//	    Plot plot = new Plot("") {
//	        {
//	            xlabel = "n";
//	            ylabel = "error";
//	        }
//	    };
//	    
//	    double[] x = { 1, 4,16,32};
//	    DataTableSet dts = plot.addNewDataTableSet("Error-Plot");
//	    dts.addNewDataTable("average", x, ploterrors[0]);
//	    dts.addNewDataTable("median", x, ploterrors[1]);
//	    dts.addNewDataTable("first quantile", x,ploterrors[2] );
//	    dts.addNewDataTable("third quantile", x, ploterrors[3]);
//	    jg.execute(plot, jg.plot2d);
	}
}