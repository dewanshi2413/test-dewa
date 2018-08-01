package com.tcl.aem.tclweb.workflows;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	//private static final String REGEX = "$ ";
	  // private static final String INPUT = "5a79b9786d62e_5a798493b20de_PUSH";
	   //private static Pattern pattern;
	   //private static Matcher matcher;
	private static String test1="Malayalam";
	   public static void main( String args[] ) {
	      //pattern = Pattern.compile("[$#%&@!_ ]", Pattern.CASE_INSENSITIVE);
	      ///matcher = pattern.matcher(INPUT);
	      //String str="/content/dam/tclproject/raw-images/TCL Image.png/jcr:content/renditions/original";
	      
	      //int index=str.indexOf('/');
	      //int test=str.indexOf("/jcr:content");
	      //System.out.println(str.substring(0,test));
	     // System.out.println("$$$$$$$$$"+str);
	      //System.out.println("Current INPUT is: "+INPUT);

	      //System.out.println("lookingAt(): "+matcher.find());
	      //System.out.println("matches(): "+matcher.find(0));
	      
		  String reverse="";
		   int i=0;
		   int length=test1.length();
		   System.out.println(length);
		   for(int j=length-1;j>=i;j--){
			   reverse=reverse+test1.charAt(j);
			
	   }
		  if(test1.equalsIgnoreCase(reverse)){
			  System.out.println("PAlindrome");
		  }
		   
		   
		   //System.out.println(length);
	      //S//tring reverse=new StringBuffer(test1).reverse().toString();
	      //System.out.println(reverse);
	      //if(test1.equalsIgnoreCase(reverse)){
	    	 // System.out.println("PAlindrome");
	      //}
	      //else{
	    	  //System.out.println("else");
	      //}
}
}
