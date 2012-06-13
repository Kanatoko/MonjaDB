package net.jumperz.app.MMonjaDB.eclipse.view;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Composite;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import java.text.Collator;
import java.util.*;
import net.jumperz.util.*;
import java.io.*;
import net.jumperz.gui.*;

import net.jumperz.app.MMonjaDB.eclipse.MUtil;
import net.jumperz.app.MMonjaDB.eclipse.dialog.*;
import net.jumperz.app.MMonjaDBCore.MDataManager;
import net.jumperz.app.MMonjaDBCore.MOutputView;
import net.jumperz.app.MMonjaDBCore.action.*;
import net.jumperz.app.MMonjaDBCore.action.mj.MShowAllDbStatsAction;
import net.jumperz.app.MMonjaDBCore.event.*;
import java.util.*;

public class MDBList
extends MAbstractView
implements MOutputView
{
private Table table;
private Image image;
private java.util.List statsList;
private int sortOrder = 0;
//--------------------------------------------------------------------------------
public MDBList()
{
MEventManager.getInstance().register2( this );
}
//--------------------------------------------------------------------------------
public void init2()
{
parent.setLayout( formLayout );

table = new Table( parent, SWT.BORDER | SWT.FULL_SELECTION );
table.setHeaderVisible( true );
table.setLinesVisible( true );
FormData d1 = new FormData();
d1.top = new FormAttachment( 0, 1 );
d1.left = new FormAttachment( 0, 1 );
d1.right = new FormAttachment( 100, -1 );
d1.bottom = new FormAttachment( 100, -1 );
table.setLayoutData( d1 );

	//listeners
table.addListener( SWT.MouseDoubleClick, this );
table.addListener( SWT.Selection, this );
table.addListener( SWT.KeyDown, this );

image = MUtil.getImage( parent.getShell().getDisplay(), "database.png" );

if( dataManager.getMongo() != null )
	{
	actionManager.executeAction( "mj show all db stats" );
	}
}
//--------------------------------------------------------------------------------
public void setFocus()
{
}
//--------------------------------------------------------------------------------
private  void onShowDbs( MShowDBAction action )
{
/*
	//check Context
if( action.getContext() != MDataManager.getInstance().getMongo() )
	{
	debug( "invalid state" );
	return;
	}
*/

actionManager.executeAction( "mj show all db stats" );
}
//--------------------------------------------------------------------------------
private void onTableSelect( Event event )
{
TableItem[] items = table.getSelection();
if( items.length != 1 )
	{
	return;
	}
String dbName = items[ 0 ].getText( 0 );

if( isActive() )
	{
	actionManager.executeAction( "use " + dbName );
	}
}
// --------------------------------------------------------------------------------
protected void handleEvent2( Event event )
{
if( event.widget == table )
	{
	switch( event.type )
		{
		case SWT.Selection:
			onTableSelect( event );
			break;
		}
	}
else if( MSwtUtil.getTableColumns( table ).contains( event.widget ) )
	{
	switch( event.type )
		{
		case SWT.Selection:
			onTableColumnSelect( ( TableColumn )event.widget );
			break;
		case SWT.Resize:
			onTableColumnResize();
			break;
		}
	}
}
//--------------------------------------------------------------------------------
private double toDouble( Object value )
{
if( value instanceof Integer )
	{
	Integer intval = ( Integer )value;
	return ( new Double( intval.intValue() ) ).doubleValue();
	}
else if( value instanceof Double )
	{
	return ( ( Double )value ).doubleValue();
	}
else
	{
	if( value.toString().matches( "^[0-9\\.]+$" ) )
		{
		return Double.parseDouble( value.toString() );
		}
	else
		{
		return 0;
		}
	}
}
//--------------------------------------------------------------------------------
private void onTableColumnSelect( TableColumn column )
{
final String columnName = column.getText();

	//sort order
if( sortOrder == 1 )
	{
	sortOrder = -1;
	}
else
	{
	sortOrder = 1;
	}

final int _sortOrder = sortOrder;

Comparator c = new Comparator(){/**************/
public int compare( Object o1, Object o2 )
{
Map map1 = ( Map )o1;
Map map2 = ( Map )o2;

Object value1 = map1.get( columnName );
Object value2 = map2.get( columnName );

debug( value1.getClass() );
debug( value2.getClass() );


if( value1 == null || value2 == null )
	{
	return 0;
	}

if( ( value1 instanceof Integer || value1 instanceof Double )
 && ( value2 instanceof Integer || value2 instanceof Double ) 
  )
	{
	double double1 = toDouble( value1 );
	double double2 = toDouble( value2 );
	if( double1 > double2 )
		{
		return 1 * _sortOrder;
		}
	else if( double1 == double2 )
		{
		return 0;
		}
	else
		{
		return -1 * _sortOrder;
		}
	}

String str1 = value1.toString();
String str2 = value2.toString();
Collator collator = Collator.getInstance( Locale.getDefault() );
return collator.compare( str1, str2 ) * _sortOrder;
}
public boolean equals( Object o1, Object o2 )
{
return o1.equals( o2 );
}
};/***************/

Collections.sort( statsList, c );

drawTable( statsList );
}
// --------------------------------------------------------------------------------
private void onTableColumnResize()
{
MSwtUtil.setTableColumnWidthToProperties( DBLIST_TABLE, table, prop );
}
//--------------------------------------------------------------------------------
private void clearTable()
{
	//reset table
TableColumn[] columns = table.getColumns();
for( int i = 0; i < columns.length;  ++i )
	{
	columns[ i ].dispose();
	}
table.removeAll();
}
//--------------------------------------------------------------------------------
private Map convertMap( Map data )
{
Map sortedMap = null;
sortedMap = new LinkedHashMap();
sortedMap.put( "db", data.get( "db" ) );
Map tmpMap = new HashMap();
tmpMap.putAll( data );
tmpMap.remove( "db" );
sortedMap.putAll( tmpMap );
return sortedMap;
}
//--------------------------------------------------------------------------------
private void drawTable( final java.util.List list )
{
final MDBList dbList = this;
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

clearTable();

if( list.size() == 0 )
	{
	return;
	}

java.util.List columnNameList = new ArrayList();
try
	{	
		//set columns
	{
	Map sortedMap = convertMap( ( Map )list.get( 0 ) );
	Iterator p = sortedMap.keySet().iterator();
	while( p.hasNext() )
		{
		String columnName = ( String )p.next();
		TableColumn column = new TableColumn ( table, SWT.NONE );
		column.setText( columnName );
		columnNameList.add( columnName );		
		//column.setWidth( 35 );
		}
	
	//MSwtUtil.getTableColumnWidthFromProperties( DBLIST_TABLE, table, prop, new int[]{ 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40 } );
	MSwtUtil.getTableColumnWidthFromProperties2( DBLIST_TABLE, table, prop );
	MSwtUtil.addListenerToTableColumns2( table, dbList );
	}
	
	debug( columnNameList );
	
		//draw items
	{
	for( int i = 0; i < list.size(); ++i )
		{
		TableItem item = new TableItem( table, SWT.NONE );
		item.setImage( image );
		Map data = convertMap( ( Map )list.get( i ) );

		data.put( data_type, data_type_db );
		for( int k = 0; k < columnNameList.size(); ++k )
			{
			String columnName = ( String )columnNameList.get( k );
			Object value = data.get( columnName );
			if( value != null )
				{
				item.setText( k, value.toString() );			
				}
			else
				{
				item.setText( k, "" );
				}
			}
		}
	}
	
	}
catch( Exception e )
	{
	e.printStackTrace();
	MEventManager.getInstance().fireErrorEvent( e );
	}

DB db = MDataManager.getInstance().getDB();
if( db != null )
	{
	selectItem( db.getName() );
	}

}});//*****
}
//--------------------------------------------------------------------------------
private synchronized void onShowAllDbStats( MShowAllDbStatsAction action )
{
/*
	//check Context
if( action.getContext() != MDataManager.getInstance().getMongo() )
	{
	debug( "invalid state" );
	return;
	}
*/

statsList = action.getStatsList();
if( statsList.size() == 0 )
	{
	return;
	}

drawTable( statsList );
}
//--------------------------------------------------------------------------------
private void selectItem( String dbName )
{
if( table.getItemCount() == 0 )
	{
	return;
	}
TableItem[] items = table.getItems();
for( int i = 0; i < items.length; ++i )
	{
	String itemName = items[ i ].getText( 0 );
	if( itemName.equals( dbName ) )
		{
		if( table.getSelectionIndex() == i )
			{
			break;
			}
		else
			{
			table.select( i );
			break;	
			}
		}
	}
}
//--------------------------------------------------------------------------------
private void onUse( MUseAction action )
{
final String dbName = action.getDBName();

shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

selectItem( dbName );

}});//*****
}
//--------------------------------------------------------------------------------
private void onDisconnect()
{
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

clearTable();
statsList = new ArrayList();

}});//*****

}
//--------------------------------------------------------------------------------
public void dispose()
{
MEventManager.getInstance().removeObserver2( this );

super.dispose();
}
//--------------------------------------------------------------------------------
public void update( final Object e, final Object source )
{
//threadPool.addCommand( new MCommand() {	public void execute(){ //-----------------

final MEvent event = ( MEvent )e;
final String eventName = event.getEventName();

if( eventName.indexOf( event_showdbs + "_end" ) == 0 )
	{
	MShowDBAction action = ( MShowDBAction )source;
	onShowDbs( action );	
	}
else if( eventName.indexOf( event_mj_all_db_stats + "_end" ) == 0 )
	{
	MShowAllDbStatsAction action = ( MShowAllDbStatsAction )source;
	onShowAllDbStats( action );
	}
else if( eventName.indexOf( event_use + "_end" ) == 0 )
	{
	MUseAction action = ( MUseAction )source;
	onUse( action );
	}
else if( event.getEventName().indexOf( event_disconnect + "_end" ) == 0 )
	{
	onDisconnect();
	}

//	} public void breakCommand(){}	} ); //------------
}
//--------------------------------------------------------------------------------
/*
    public void init(IViewSite site, IMemento memento) throws PartInitException {

    debug( "--" );
    debug( site );
    debug( memento );
        init(site);
    }
//--------------------------------------------------------------------------------
public void saveState(IMemento memento)
{
debug( "--saveState--" );
debug( memento );
}*/
//--------------------------------------------------------------------------------
}
