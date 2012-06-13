package net.jumperz.security;

import java.util.*;
import java.util.logging.Logger;

public class MHashDoSDetector
{
public static final int UNKNOWN	= 0;
public static final int GOOD	= 1;
public static final int BAD	= 2;

public static float threshold = 0.90f;

private Set hashSet1 = new HashSet();
private Set hashSet2 = new HashSet();
private Set hashSet3 = new HashSet();
private int r = ( new Random() ).nextInt( 20000 );
//--------------------------------------------------------------------------------
public void addParameterName( String parameterName )
{
hashSet1.add( Integer.toString( PHP5Hash( parameterName ) + r ) );
hashSet2.add( Integer.toString( PHP4Hash( parameterName ) + r ) );
hashSet3.add( Integer.toString( JavaHash( parameterName ) + r ) );
}
//--------------------------------------------------------------------------------
public static int JavaHash( String s ) {
return s.hashCode();
}
//--------------------------------------------------------------------------------
public static int PHP5Hash( String s ) {
	int h = 5381;
	int count = s.length();
	int offset = 0;
        int len = count;
	if ( len > 0) {
	    int off = offset;
	    char val[] = s.toCharArray();

            for (int i = 0; i < len; i++) {
                h = 33*h + val[off++];
            }
            //hash = h;
        }
        return h;
    }
//--------------------------------------------------------------------------------
    public static int PHP4Hash( String s ) {
	int h = 5381;
	int count = s.length();
	int offset = 0;
        int len = count;
	if ( len > 0) {
	    int off = offset;
	    char val[] = s.toCharArray();

            for (int i = 0; i < len; i++) {
                h = 33*h ^ val[off++];
            }
            //hash = h;
        }
        return h;
    }
//--------------------------------------------------------------------------------
public int getResult()
{
int uniqueParameterCount = hashSet1.size();
if( hashSet2.size() > uniqueParameterCount )
	{
	uniqueParameterCount = hashSet2.size();
	}
if( hashSet3.size() > uniqueParameterCount )
	{
	uniqueParameterCount = hashSet3.size();
	}

if( uniqueParameterCount < 500 )
	{
	return UNKNOWN;
	}
else
	{
	/*
			System.out.println( "===========" );
			System.out.println( uniqueParameterCount + "/" + hashSet1.size() );
			System.out.println( uniqueParameterCount + "/" + hashSet2.size() );
			System.out.println( uniqueParameterCount + "/" + hashSet3.size() );
			
	
			System.out.println( ( (float)hashSet1.size() / (float)uniqueParameterCount ) );
			System.out.println( ( (float)hashSet2.size() / (float)uniqueParameterCount ) );
			System.out.println( ( (float)hashSet3.size() / (float)uniqueParameterCount ) );
	*/
			
	if( ( (float)hashSet1.size() / (float)uniqueParameterCount ) <= threshold
	 || ( (float)hashSet2.size() / (float)uniqueParameterCount ) <= threshold
	 || ( (float)hashSet3.size() / (float)uniqueParameterCount ) <= threshold
	  )
		{
		return BAD;
		}
	else
		{
		return GOOD;
		}
	}
}
//--------------------------------------------------------------------------------
}
