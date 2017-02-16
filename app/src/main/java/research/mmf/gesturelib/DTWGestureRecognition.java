package research.mmf.gesturelib;

import android.util.Log;
import java.util.ArrayList;

/***
 * Core algorithm for gesture recognition
 * Author: Mingming Fan
 * Contact: fmmbupt@gmail.com
 * Contact the author for use of the code
 * */

public class DTWGestureRecognition
{
	private static final int INFINITE = Integer.MAX_VALUE;
    private static final String TAG = "DTW";
    private static final int G = 10;

	public DTWGestureRecognition()
	{
		
	}

	private double Distance(AccData templateData, AccData newData)
	   {
	   	double dis = 0;
	   	dis = Math.sqrt((templateData.getX() - newData.getX())*(templateData.getX() - newData.getX())+ (templateData.getY() - newData.getY())*(templateData.getY() - newData.getY())+(templateData.getZ() - newData.getZ())*(templateData.getZ() - newData.getZ()));
	   	return dis;
	   }
	
	public void Quantization(AccData data)
	{
		if(data.getX() <= G)
		{
			data.setX(Math.round(data.getX()));
		}
		else if(data.getX() > G && data.getX() < 2*G)
		{
			data.setX(G + Math.round(data.getX()-G));
		}
		else
		{
			data.setX(2*G);
		}
		
		if(data.getY() <= G)
		{
			data.setY(Math.round(data.getY()));
		}
		else if(data.getY() > G && data.getY() < 2*G)
		{
			data.setY(G + Math.round(data.getY()-G));
		}
		else
		{
			data.setY(2*G);
		}
		
		if(data.getZ() <= G)
		{
			data.setZ(Math.round(data.getZ()));
		}
		else if(data.getZ() > G && data.getZ() < 2*G)
		{
			data.setZ(G + Math.round(data.getZ()-G));
		}
		else
		{
			data.setZ(2*G);
		}
	}
	
   public double DTW(ArrayList<AccData> sensorData, ArrayList<AccData> template, int r)
   {
	   int sensorDataLength = sensorData.size();
	   int templateLength = template.size();
	   
	   Log.v(TAG,"Sample Size:"+sensorDataLength);
	   Log.v(TAG,"Template Size:"+templateLength);
	   
	   double[][] distance = new double[templateLength][sensorDataLength];
	   
	   int r2 = r + Math.abs(sensorDataLength - templateLength);

		for(int i = 0; i < templateLength; i++)
			for(int j = 0; j < sensorDataLength; j++)
			{
				distance[i][j] = INFINITE;
			}
		
		Log.v(TAG,"After Initial Matrix");

		distance[0][0] = 2 * (Distance(template.get(0),sensorData.get(0)));
		

		for(int i = 1; i <= Math.min(r2,templateLength-1);i++)
			distance[i][0] = distance[i-1][0]+ Distance(template.get(i),sensorData.get(0));

		for(int j = 1; j <= Math.min(r2,sensorDataLength-1); j++)
			distance[0][j] = distance[0][j-1]+Distance(template.get(0),sensorData.get(j));

		Log.v(TAG,"Init distance");
		
		
		int iStart, iMax;
		for(int j = 1; j < sensorDataLength; j++)
		{
			if(j - r2 <= 0)
			{
				iStart = 1;
			}
			else
			{
				iStart = j-r2;
			}

			if(j + r2 >= templateLength)
			{
				iMax = templateLength -1;
			}
			else
			{
				iMax = j + r2;
			}

			for(int i = iStart; i <= iMax; i++)
			{
				double g1 = distance[i-1][j]+ Distance(template.get(i),sensorData.get(j));
				double g2 = distance[i-1][j-1] + 2*Distance(template.get(i),sensorData.get(j));
				double g3 = distance[i][j-1] + Distance(template.get(i),sensorData.get(j));  
				g2 = Math.min(g1,g2);
				g3 = Math.min(g2,g3);
				distance[i][j] = g3;
			}         
		}
		Log.v(TAG,"Unify");
		double dist;  // final average distance between the input data and template data;
		dist = distance[templateLength-1][sensorDataLength -1]/(double)(templateLength + sensorDataLength);
		return dist;
   }
   

	public int GestureRecognition(ArrayList<ArrayList<AccData>> templates, ArrayList<AccData> SensorData)
	{
	    double tempMin = INFINITE;
	    int  tagMin = templates.size();
	    double[] result = new double[templates.size()];

	    for(int i = 0; i < templates.size(); i++)
	    	{
	    		Log.v(TAG,"Before DTW"+i);
			    result[i]= DTW(SensorData,templates.get(i),(int)(templates.get(i).size()/30));
			    Log.v(TAG,"Distance:"+result[i]);
			    if(result[i] < tempMin)
			    	{
			         tagMin = i;
			         tempMin = result[i];
			    	}
	    	}
		  return tagMin;
	}
}