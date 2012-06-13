package net.jumperz.net;

public interface MAbstractParameter
{
public static final int UNKNOWN   = -1;
public static final int URI       = 0;
public static final int BODY      = 1;
public static final int MULTIPART = 2;
public static final int COOKIE    = 3;
public static final int HEADER    = 4;
public static final int PARAM     = 5;
// --------------------------------------------------------------------------------
public int getType();
public String getName();
public String getValue();
public void setValue( String s );
public void setName( String s );
// --------------------------------------------------------------------------------
}