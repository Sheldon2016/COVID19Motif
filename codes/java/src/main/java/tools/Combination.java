package tools;

public class Combination {

	String[] s = null;
	int counter = 0;

	public static String[] getStr(int m, int n) {
		int bar = 1000;//1000000;
		String res[] = null;
		if (m == n) {
			res = new String[1];
			res[0] = "";
			for (int i = 0; i < m; i++) {
				res[0] += "1";
			}
			return res;
		}
		if (m < n) {
			System.out.println("No combination: m<n!");
			return res;
		}
		if(n==0) {
			res = new String[1];
			res[0] = "";
			for (int i = 0; i < m; i++) {
				res[0] += "0";
			}
			return res;
		}
		if(n==1||m-n==1) {
			String b1="", b2="";
			if(n==1) {b1="1";b2="0";}
			if(m-n==1) {b1="0";b2="1";}
			res = new String[m];
			for (int i = 0; i < m; i++) {
				res[i] = "";
				for(int j=0;j<m;j++) {
					if(i==j)
						res[i] += b1;
					else
						res[i] += b2;
				}
			}
			return res;
		}		
		int num = C(m,n);
		if(num<=0){
			//System.out.println("Too big: find "+n +"nodes from "+m+" nodes!");
			num = bar;
		}
		if(num>bar) {
			//System.out.println("Big: find "+n +"nodes from "+m+" nodes!");
			num = bar;
		}
		if(n==2||m-n==2) {
			String b1="", b2="";
			if(n==2) {b1="1";b2="0";}
			if(m-n==2) {b1="0";b2="1";}
			res = new String[num];
			int counter = 0;
			for(int i=0;i<m-1;i++) {
				for(int j=i+1;j<m;j++) {
					res[counter]="";
					for(int t=0;t<m;t++) {
						if(t==i||t==j) res[counter]+=b1;
						else res[counter]+=b2;
					}
					counter++;
					if(counter>=num) return res;
				}
			}
		}
		if(n==3||m-n==3) {
			String b1="", b2="";
			if(n==3) {b1="1";b2="0";}
			if(m-n==3) {b1="0";b2="1";}
			res = new String[num];
			int counter = 0;
			for(int i=0;i<m-2;i++) {
				for(int j=i+1;j<m-1;j++) {
					for(int p=j+1;p<m;p++) {
						res[counter]="";
						for(int t=0;t<m;t++) {
							if(t==i||t==j||t==p) res[counter]+=b1;
							else res[counter]+=b2;
							}
						counter++;
						if(counter>=num) return res;
						}
					}
			}
		}
		if(n==4||m-n==4) {
			String b1="", b2="";
			if(n==4) {b1="1";b2="0";}
			if(m-n==4) {b1="0";b2="1";}
			res = new String[num];
			int counter = 0;
			for(int i=0;i<m-3;i++) {
				for(int j=i+1;j<m-2;j++) {
					for(int p=j+1;p<m-1;p++) {
						for(int q=p+1;q<m;q++) {
							res[counter]="";
							for(int t=0;t<m;t++) {
								if(t==i||t==j||t==p) res[counter]+=b1;
								else res[counter]+=b2;
								}
							counter++;
							if(counter>=num) return res;
							}
						}
					}
			}
		}
		if(n>4||m-n>4)
			System.out.println("Not supporting to find "+n+" nodes from "+m+" nodes!");
		return res;
	}
	public String[] getStr2(int m, int n) {
		counter = 0;
		if (m == n) {
			s = new String[1];
			s[0] = "";
			for (int i = 0; i < m; i++) {
				s[0] += "1";
			}
			return s;
		}
		if (m < n) {
			System.out.println("No combination: m<n!");
			return s;
		}
		//if(m>100&&n==4||m>50&&n==3||m>2000&&n==2){
		//	System.out.println("Big combination candidates: "+num);
		//	return s;
		//}
		int num = C(m,n);
		if(num>1000000||num<=0){
			//System.out.println("Big combination candidates: "+num);
			int m1=m;
			int num1 = C(m1,n);
			while(num1>1000000||num1<=0){
				m1--;
				num1 = C(m1,n);
			}
			s = new String[num1];
			gencom(m1 - 1, n, "0");
			gencom(m1 - 1, n - 1, "1");
			String plug = "";
			for(int i=0;i<m-m1;i++){
				plug+="0";
			}
			for(int i=0;i<s.length;i++){
				s[i]+=plug;
			}
			
			return s;
		}
		s = new String[num];
		gencom(m - 1, n, "0");
		gencom(m - 1, n - 1, "1");
		return s;
	}

	void gencom (int m, int n, String str){
		if(counter==s.length)
			return;
		if(m<n||m==0){
			s[counter]=str;
			counter++;
			return;
			}
		if(m==n){
			for(int i=0;i<m;i++){
				str+="1";
			}
			s[counter]=str;
			counter++;
			return;
		}
		if(n==0){
			for(int i=0;i<m;i++){
				str+="0";
			}	
			s[counter]=str;
			counter++;
			return;
		}
		try {
			//System.out.println(C(m-1,n));
		gencom(m-1, n, str+"0");
		gencom(m-1, n-1, str+"1");
		}catch(Exception e) {
			System.out.println("StackOverflowError "+C(m-1,m));
			return;
		}
	}

	
	public static int A(int n, int m) {// n>m
		int result = 1;
		for (int i = m; i > 0; i--) {
			result *= n;
			n--;
		}
		return result;
	}
	public static int C2(int n, int m) {
		// int denominator=factorial(up);
		int denominator = A(m, m);
		// ·Ö×Ó
		int numerator = A(n, m);
		return numerator / denominator;
	}
	public static int C(int n, int m)// a simplified version
	{
		int helf = n / 2;
		if (m > helf) {
			m = n - m;
		}
		int numerator = A(n, m);
		int denominator = A(m, m);
		return numerator / denominator;
	}

}
