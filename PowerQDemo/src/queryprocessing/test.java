package queryprocessing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class test
{

	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		String c = "today is ok";
		StringBuffer a = new StringBuffer();
		StringBuffer b = new StringBuffer();
		a.append("Hello! world");
		System.out.println(a.toString() + " " + a.length());
		a.append(b);
		System.out.println(a.toString() + " " + a.length());
		System.out.println("\nCompute");
		System.out.println("\n Compute");
		System.out.println(c + b.toString());
		String org = "hello";
		String mdf = org.replace("ll","gg");
		System.out.println(mdf);
		System.out.println(org);
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter("./testlog", true));
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			bw.write(dateFormat.format(cal.getTime()) + " hello\n");
			bw.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		String[] a1 = new String[] {"aaa", "vvv"};
		String[] a2 = new String[] {"bbb", "vvv"};
		String[] a3 = new String[] {"ccc", "vvv"};
		ArrayList<String[]> list = new ArrayList<String[]>();
		list.add(a1);
		list.add(a2);
		list.add(a3);
		String[][] d2 = list.toArray(new String[list.size()][]);
		for(int i = 0 ; i < d2.length; i++)
		{
			for(int j = 0; j < d2[i].length; j++)
			{
				System.out.println(d2[i][j]);
			}
		}
	}

}
