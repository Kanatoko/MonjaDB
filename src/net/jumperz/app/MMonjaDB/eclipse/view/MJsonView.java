/*
 * Created on Mar 13, 2012
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.jumperz.app.MMonjaDB.eclipse.view;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import net.jumperz.app.MMonjaDB.eclipse.MUtil;
import net.jumperz.app.MMonjaDB.eclipse.dialog.MActionDialog;
import net.jumperz.app.MMonjaDB.eclipse.dialog.MPromptDialog;
import net.jumperz.app.MMonjaDBCore.MInputView;
import net.jumperz.app.MMonjaDBCore.MOutputView;
import net.jumperz.app.MMonjaDBCore.action.mj.*;
import net.jumperz.app.MMonjaDBCore.event.MEvent;
import net.jumperz.app.MMonjaDBCore.event.MEventManager;
import net.jumperz.mongo.MMongoUtil;

import java.util.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;
import com.mongodb.*;

public class MJsonView
extends MAbstractView
implements MOutputView, MInputView
{
private Text text;
private Object _id;
private Action saveAction;
//--------------------------------------------------------------------------------
public MJsonView()
{
eventManager.register2( this );
}
//--------------------------------------------------------------------------------
public void init2()
{
parent.setLayout( new FormLayout() );
text = new Text( parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI );
FormData fd_text = new FormData();
fd_text.top = new FormAttachment(0, 1);
fd_text.left = new FormAttachment(0, 1);
fd_text.bottom = new FormAttachment(100, -1);
fd_text.right = new FormAttachment(100, -1);
text.setLayoutData(fd_text);

saveAction = new Action(){ public void run(){//-----------
onSave();
}};//-----------
saveAction.setToolTipText( "Save To Database" );
saveAction.setText( "Save" );
initAction( saveAction, "disk.png", null );
saveAction.setEnabled( false );
}
//--------------------------------------------------------------------------------
private void onSave()
{
//DB db = dataManager.getDB();
//Object o = db.eval( text.getText(), null );
String collName = dataManager.getCollName();

String s = text.getText();
s = s.replaceAll( "\r", "" );
s = s.replaceAll( "\n", "" );
s = s.replaceAll( "\t", "" );

executeAction( "db." + collName + ".save(" + s + ")" );
dataManager.reloadDocument();
}
//--------------------------------------------------------------------------------
public void dispose()
{
eventManager.removeObserver2( this );
super.dispose();
}
//--------------------------------------------------------------------------------
public void setFocus()
{
}
//--------------------------------------------------------------------------------
private void onFind()
{
shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//*****

text.setText( "" );
saveAction.setEnabled( false );

}});//********

}
//--------------------------------------------------------------------------------
private void onUse()
{
onFind();
}
//--------------------------------------------------------------------------------
private void onDisconnect()
{
onFind();
}
//--------------------------------------------------------------------------------
private void drawJson( DBObject data )
{
DB db = dataManager.getDB();
text.setText( MMongoUtil.toJson( db, data ) );
saveAction.setEnabled( true );
}
//--------------------------------------------------------------------------------
private void onEdit( MEditAction action )
{
final DBObject data = dataManager.getDocumentDataByAction( action );
if( data == null )
	{
	return;
	}

shell.getDisplay().asyncExec( new Runnable(){ public void run()	{//-----

drawJson( data );

}});//-----
}
//--------------------------------------------------------------------------------
public void update( final Object e, final Object source )
{
final MEvent event = ( MEvent )e;
final String eventName = event.getEventName();
if( event.getEventName().indexOf( event_mj_edit + "_end" ) == 0 )
	{
	MEditAction action = ( MEditAction )source;
	onEdit( action );
	}
else if( event.getEventName().indexOf( event_find + "_end" ) == 0 )
	{
	onFind();
	}
else if( event.getEventName().indexOf( event_use + "_end" ) == 0 )
	{
	onUse();
	}
else if( event.getEventName().indexOf( event_disconnect + "_end" ) == 0 )
	{
	onDisconnect();
	}
/*
else if( event.getEventName().indexOf( event_mj_edit_field + "_end" ) == 0 )
	{
	onEditField( ( MEditFieldAction )source );
	}
*/
}
//--------------------------------------------------------------------------------
}
