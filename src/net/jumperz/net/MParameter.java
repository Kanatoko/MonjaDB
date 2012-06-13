package net.jumperz.net;

public class MParameter
implements MAbstractParameter
{
private String name;
private String value;
private int type;
// --------------------------------------------------------------------------------
public MParameter( String n, String v )
{
name = n;
value = v;
type = URI;
}
// --------------------------------------------------------------------------------
public MParameter( String n, String v, int t )
{
name = n;
value = v;
type = t;
}
//--------------------------------------------------------------------------------
public void setName( String s )
{
name = s;
}
// --------------------------------------------------------------------------------
public void setValue( String s )
{
value = s;
}
// --------------------------------------------------------------------------------
public int getType()
{
return type;
}
// --------------------------------------------------------------------------------
public String getValue()
{
return value;
}
// --------------------------------------------------------------------------------
public String getName()
{
return name;
}
// --------------------------------------------------------------------------------
public String toString()
{
return name + "=" + value;
}
// --------------------------------------------------------------------------------
}