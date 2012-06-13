package net.jumperz.app.MMonjaDB.eclipse.dialog;

import net.jumperz.app.MMonjaDB.eclipse.*;
import net.jumperz.app.MMonjaDBCore.MConstants;
import net.jumperz.app.MMonjaDBCore.MDataManager;
import net.jumperz.app.MMonjaDBCore.action.MActionManager;
import net.jumperz.util.MProperties;
import net.jumperz.util.MStringUtil;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import java.io.*;

public class MConnectDialog
extends Dialog
implements MConstants
{
private MProperties prop = MDataManager.getInstance().getProp();
private Text hostText;
private Text portText;
private Text sshText;
private Text keyText;
private Text dbText;
private Button connectionRadio1;
private Button connectionRadio2;
private FileDialog loadDialog;
//--------------------------------------------------------------------------------
public MConnectDialog( Shell parentShell )
{
super( parentShell );
}
//--------------------------------------------------------------------------------
protected void configureShell( Shell newShell )
{
super.configureShell( newShell );
newShell.setText( "Connection Configuration" );
Image image = MUtil.getImage( newShell.getDisplay(), "server_lightning.png" );
newShell.setImage( image );
}
//--------------------------------------------------------------------------------
protected void okPressed()
{
String host = hostText.getText();
int port = MStringUtil.parseInt( portText.getText(), 27017 );
String dbName = dbText.getText();

String sshStr = sshText.getText();
String keyFileName = keyText.getText();

if( connectionRadio1.getSelection() )
	{
	MActionManager.getInstance().executeAction( "connect " + host + ":" + port + "/" + dbName );
	prop.setProperty( CONNECT_DIALOG_NORMAL_CONNECTION, true );
	}
else
	{
	MActionManager.getInstance().executeAction( "mj connect ssh " + sshStr + " " + host + ":" + port + "/" + dbName + " \"" + keyFileName + "\"" );
	prop.setProperty( CONNECT_DIALOG_NORMAL_CONNECTION, false );
	}

	//save config
prop.setProperty( CONNECT_DIALOG_HOST, host );
prop.setProperty( CONNECT_DIALOG_PORT, port + "" );
prop.setProperty( CONNECT_DIALOG_DB, dbName );
prop.setProperty( CONNECT_DIALOG_SSH, sshStr );
prop.setProperty( CONNECT_DIALOG_SSH_KEY, keyFileName );
prop.setProperty( "loadDialogPath", loadDialog.getFilterPath() );

setReturnCode(OK);
close();
}
//--------------------------------------------------------------------------------
protected Control createDialogArea( Composite parent )
{
Composite composite = (Composite)super.createDialogArea( parent );
composite.setLayout( new FormLayout() );

Group connGroup = new Group( composite, SWT.SHADOW_ETCHED_OUT );
connGroup.setText("Connection Type");
FormData d7 = new FormData();
d7.top = new FormAttachment(0, 10);
d7.left = new FormAttachment(0, 10);
d7.right = new FormAttachment(0, 285);
d7.bottom = new FormAttachment(0, 100);
connGroup.setLayoutData( d7 );
connGroup.setLayout( new FormLayout() );

connectionRadio1 = new Button(connGroup, SWT.RADIO);
connectionRadio1.addSelectionListener(new SelectionAdapter() {
	@Override
	public void widgetSelected(SelectionEvent e) {
	}
});
FormData fd_connectionRadio1 = new FormData();
fd_connectionRadio1.top = new FormAttachment(0, 8);
fd_connectionRadio1.right = new FormAttachment(100, -59);
fd_connectionRadio1.left = new FormAttachment( 0, 12);
connectionRadio1.setLayoutData(fd_connectionRadio1);
connectionRadio1.setText("Normal");

connectionRadio2 = new Button(connGroup, SWT.RADIO);
FormData fd_connectionRadio2 = new FormData();
fd_connectionRadio2.top = new FormAttachment(connectionRadio1, 5);
fd_connectionRadio2.right = new FormAttachment(100, -59);
fd_connectionRadio2.left = new FormAttachment(0, 12);
connectionRadio2.setLayoutData(fd_connectionRadio2);
connectionRadio2.setText("Via SSH");
Group grpSshConfiguration = new Group(composite, SWT.NONE);
grpSshConfiguration.setText("SSH Configuration");
grpSshConfiguration.setLayout(new FormLayout());
FormData fd_grpSshConfiguration = new FormData();
fd_grpSshConfiguration.top = new FormAttachment(0, 10);
fd_grpSshConfiguration.bottom = new FormAttachment(0, 250);
fd_grpSshConfiguration.left = new FormAttachment(0, 300);
fd_grpSshConfiguration.right = new FormAttachment(100, -10);
grpSshConfiguration.setLayoutData(fd_grpSshConfiguration);

Group mongoGroup = new Group(composite, SWT.NONE);
mongoGroup.setText("MongoDB");
mongoGroup.setLayout(new FormLayout());
FormData fd_grpMongodb = new FormData();
fd_grpMongodb.top = new FormAttachment(connGroup, 16);
fd_grpMongodb.bottom = new FormAttachment(0, 250);
fd_grpMongodb.right = new FormAttachment(grpSshConfiguration, -17);
Label SSHLabel1 = new Label(grpSshConfiguration, SWT.NONE);
FormData fd_SSHLabel1 = new FormData();
fd_SSHLabel1.right = new FormAttachment(0, 236);
fd_SSHLabel1.top = new FormAttachment(0, 10);
fd_SSHLabel1.left = new FormAttachment(0, 18);
SSHLabel1.setLayoutData(fd_SSHLabel1);
SSHLabel1.setText("SSH \"user@host(:port)\"");
sshText = new Text(grpSshConfiguration, SWT.BORDER);
FormData fd_sshText = new FormData();
fd_sshText.right = new FormAttachment(100, -20);
fd_sshText.top = new FormAttachment(0, 30);
fd_sshText.left = new FormAttachment(0, 18);
sshText.setLayoutData(fd_sshText);
Label lblNewLabel = new Label(grpSshConfiguration, SWT.NONE);
FormData fd_lblNewLabel = new FormData();
fd_lblNewLabel.right = new FormAttachment(0, 218);
fd_lblNewLabel.top = new FormAttachment(0, 62);
fd_lblNewLabel.left = new FormAttachment(0, 18);
lblNewLabel.setLayoutData(fd_lblNewLabel);
lblNewLabel.setText("SSH Private Key File");
keyText = new Text(grpSshConfiguration, SWT.BORDER);
FormData fd_keyText = new FormData();
fd_keyText.right = new FormAttachment(sshText, -40, SWT.RIGHT);
fd_keyText.top = new FormAttachment(lblNewLabel, 6);
fd_keyText.left = new FormAttachment(SSHLabel1, 0, SWT.LEFT);
keyText.setLayoutData(fd_keyText);
Button btnBrowse = new Button(grpSshConfiguration, SWT.NONE);
btnBrowse.addSelectionListener(new SelectionAdapter() {
	@Override
	public void widgetSelected(SelectionEvent e) {
	String keyFileName = loadDialog.open();
	if( keyFileName != null )
		{
		keyText.setText( keyFileName );
		}
	}
});
btnBrowse.setToolTipText("Browse");
FormData fd_btnBrowse = new FormData();
fd_btnBrowse.left = new FormAttachment(keyText, 5);
fd_btnBrowse.bottom = new FormAttachment(lblNewLabel, 30, SWT.BOTTOM);
fd_btnBrowse.top = new FormAttachment(lblNewLabel, 3);
fd_btnBrowse.right = new FormAttachment(100, -6);
btnBrowse.setLayoutData(fd_btnBrowse);
btnBrowse.setText("...");
fd_grpMongodb.left = new FormAttachment(0, 10);
mongoGroup.setLayoutData(fd_grpMongodb);
FormData d2 = new FormData();
d2.top = new FormAttachment( 0, 15 );
d2.left = new FormAttachment( 0, 8 );

Label hostLabel = new Label( mongoGroup, SWT.NONE );
FormData fd_hostLabel = new FormData();

fd_hostLabel.top = new FormAttachment(0, 15);
fd_hostLabel.left = new FormAttachment(0, 10);
hostLabel.setLayoutData(fd_hostLabel);
hostLabel.setText( "Host :" );

hostText = new Text(mongoGroup, SWT.BORDER);
FormData fd_portText = new FormData();
fd_portText.right = new FormAttachment(100, -20);
fd_portText.left = new FormAttachment(hostLabel, 9);
fd_portText.top = new FormAttachment(hostLabel, -2, SWT.TOP);
hostText.setLayoutData(fd_portText);
Label lblPort = new Label(mongoGroup, SWT.NONE);
FormData fd_lblPort1 = new FormData();
fd_lblPort1.top = new FormAttachment(hostLabel, 17);
fd_lblPort1.left = new FormAttachment(0, 10);
lblPort.setLayoutData(fd_lblPort1);
lblPort.setText("Port :");

portText = new Text(mongoGroup, SWT.BORDER);
FormData fd_portText2 = new FormData();
fd_portText2.top = new FormAttachment(0, 44);
fd_portText2.right = new FormAttachment(100, -82);
fd_portText2.left = new FormAttachment(lblPort, 12);
portText.setLayoutData(fd_portText2);
Label lblDatabase = new Label(mongoGroup, SWT.NONE);
FormData fd_lblDatabase = new FormData();
fd_lblDatabase.top = new FormAttachment(portText, 12);
fd_lblDatabase.left = new FormAttachment(hostLabel, 0, SWT.LEFT);
lblDatabase.setLayoutData(fd_lblDatabase);
lblDatabase.setText("Database :");
dbText = new Text(mongoGroup, SWT.BORDER);
FormData fd_databaseText = new FormData();
fd_databaseText.right = new FormAttachment(100, -50 );
fd_databaseText.top = new FormAttachment(portText, 9);
fd_databaseText.left = new FormAttachment(lblDatabase, 13);
dbText.setLayoutData(fd_databaseText);

hostText.setText( prop.getProperty( CONNECT_DIALOG_HOST, "127.0.0.1" ) );
dbText.setText( prop.getProperty( CONNECT_DIALOG_DB, "test" ) );
portText.setText( prop.getProperty( CONNECT_DIALOG_PORT, "27017" ) );
sshText.setText( prop.getProperty( CONNECT_DIALOG_SSH, "" ) );
keyText.setText( prop.getProperty( CONNECT_DIALOG_SSH_KEY, getDefaultKeyLocation() ) );
if( prop.getBooleanProperty( CONNECT_DIALOG_NORMAL_CONNECTION, true ) )
	{
	connectionRadio1.setSelection( true );
	}
else
	{
	connectionRadio2.setSelection( true );	
	}

loadDialog = new FileDialog( parent.getShell(), SWT.OPEN );
loadDialog.setFilterPath( prop.getProperty( "loadDialogPath" ) );

return composite;
}
//--------------------------------------------------------------------------------
private String getDefaultKeyLocation()
{
String s = ( System.getProperty( "user.home" ) + "/.ssh/id_dsa" ).replaceAll( "//", "/" );
if( ( new File( s ) ).exists() )
	{
	return s;
	}
else
	{
	return System.getProperty( "user.home" );
	}
}
//--------------------------------------------------------------------------------
}
