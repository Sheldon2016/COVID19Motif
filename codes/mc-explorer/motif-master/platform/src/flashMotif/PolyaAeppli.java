package flashMotif;

public class PolyaAeppli
{
	private double lambda;
	private double a;
	public PolyaAeppli(double a, double lambda)
	{
		this.a=a;
		this.lambda=lambda;
	}
	public double lowertail(long numOccurrences)
	{
		double z=(1.0-a)*lambda/a;
		double res=0.0;
		if(numOccurrences==0)
			res=Math.exp(-lambda);
		else if(numOccurrences==1)
			res=Math.exp(-lambda)*(1.0+a*z);
		else
		{
			double precVal=Double.MIN_VALUE;
			double currVal=0.0;
			double Lprec=-lambda;
			double Lcour=Lprec+Math.log(a*z);
			double Acour=Lprec;
			double Scour=1.0+a*z; 
			for (int i=2;i<=numOccurrences;i++)
			{
				double Lsuiv=Lcour+Math.log(a*(2.0*i-2.0+z)/i+a*a*(2.0-i)*Math.exp(Lprec-Lcour)/i);
				double Ssuiv=Scour+Math.exp(Lsuiv-Acour);
				double Asuiv=0.0;
				if(Ssuiv>0 && Ssuiv<Double.MAX_VALUE)
					Asuiv=Acour;
				else
				{
					Asuiv=Acour+Math.log(Scour);
					Ssuiv=1.0+Math.exp(Lsuiv-Asuiv); 
				}
				Lprec=Lcour;
				Lcour=Lsuiv; 
				Scour=Ssuiv; 
				Acour=Asuiv;
				currVal=Scour*Math.exp(Acour);
				if(currVal>=1.0)
					break;
				if(currVal>1.0E-322 && currVal==precVal)
					break;
				else
					precVal=currVal;
			} 
			res=currVal;
		}
		return res;
	}

}