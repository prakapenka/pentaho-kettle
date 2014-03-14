package org.pentaho.di.ui.spoon.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.util.ImageUtil;
import org.pentaho.di.version.BuildVersion;

public class AboutDialog extends Dialog {

  // for i18n purposes
  Class<?> PKG;

  PropsUI props = PropsUI.getInstance();

  // default background color
  Color white;
  Shell shell;

  // load logo image
  Image image;

  // all display/functional elements
  Canvas logo;
  Label cpr;
  Label rls;
  Label sUrl;
  Label bVer;
  Label bDate;
  Button ok;

  Text tt;

  // dialog width
  final int maxWdt = 480;

  public AboutDialog( Shell shell, Class<?> klass ) {
    super( shell, SWT.APPLICATION_MODAL );
    this.PKG = klass;
    image = ImageUtil.getImage( shell.getDisplay(), "ui/images/pentaho_logo.png" );
  }

  public Object open() {
    // create shell
    final Shell parent = getParent();
    Display display = parent.getDisplay();
    white = new Color( display, 255, 255, 255 );

    shell = new Shell( parent, SWT.CLOSE );
    shell.setText( BaseMessages.getString( PKG, "Spoon.Application.Name" ) );
    shell.setBackground( white );

    // create UI elements wiht data
    this.createUiElements( shell );

    // OK, now place them altogether!
    FormLayout layout = new FormLayout();
    layout.marginWidth = 5;
    layout.marginHeight = 5;
    shell.setLayout( layout );

    FormData data = new FormData( logo.getClientArea().width, logo.getClientArea().height );
    data.top = new FormAttachment( shell, 10, SWT.TOP );
    logo.setLayoutData( data );

    // text under logo
    data = new FormData();
    data.top = new FormAttachment( logo, 40, SWT.BOTTOM );
    data.left = new FormAttachment( 0 );
    data.right = new FormAttachment( 100 );
    tt.setLayoutData( data );

    FormData okFormData = new FormData( 60, 30 );
    okFormData.top = new FormAttachment( tt, 40, SWT.BOTTOM );
    okFormData.right = new FormAttachment( 100, 0 );
    okFormData.bottom = new FormAttachment( 100, 0 );
    ok.setLayoutData( okFormData );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        dispose();
      }
    } );

    // place in center
    Rectangle screen = display.getMonitors()[0].getBounds();
    Point size = shell.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    shell.setBounds( ( screen.width - size.x ) / 2, ( screen.height - size.y ) / 2, size.x, size.y );

    shell.pack();
    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return Boolean.TRUE.toString();
  }

  void createUiElements( Shell shell ) {
    logo = new Canvas( shell, SWT.NO_BACKGROUND | SWT.NO_FOCUS );
    final Rectangle imageBounds = image.getBounds();
    logo.setSize( maxWdt, imageBounds.height );
    logo.setBackground( white );
    logo.addPaintListener( new PaintListener() {
      public void paintControl( PaintEvent e ) {
        e.gc.drawImage( image, ( ( maxWdt - imageBounds.width ) / 2 ) - 3, 0 );
      }
    } );

    // ok button will be used to close dialog
    ok = new Button( shell, SWT.PUSH );
    ok.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    ok.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        dispose();
      }
    } );

    tt = new Text( shell, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.CENTER );
    tt.setText( this.getInfo() );
    tt.setBackground( white );
    FontData fd = props.getDefaultFont();
    fd.setHeight( 11 );
    tt.setFont( new Font( shell.getDisplay(), fd ) );
  }

  public void dispose() {
    props.setScreen( new WindowProperty( getParent() ) );
    shell.dispose();
  }

  String getInfo() {
    // resolve the release text
    String releaseText = "";
    if ( Const.RELEASE.equals( Const.ReleaseType.PREVIEW ) ) {
      releaseText = BaseMessages.getString( PKG, "Spoon.PreviewRelease.HelpAboutText" );
    } else if ( Const.RELEASE.equals( Const.ReleaseType.RELEASE_CANDIDATE ) ) {
      releaseText = BaseMessages.getString( PKG, "Spoon.Candidate.HelpAboutText" );
    } else if ( Const.RELEASE.equals( Const.ReleaseType.MILESTONE ) ) {
      releaseText = BaseMessages.getString( PKG, "Spoon.Milestone.HelpAboutText" );
    } else if ( Const.RELEASE.equals( Const.ReleaseType.GA ) ) {
      releaseText = BaseMessages.getString( PKG, "Spoon.GA.HelpAboutText" );
    }

    // build a message
    StringBuilder messageBuilder = new StringBuilder();

    messageBuilder.append( BaseMessages.getString( PKG, "System.ProductInfo" ) );
    messageBuilder.append( releaseText );
    messageBuilder.append( " - " );
    messageBuilder.append( BuildVersion.getInstance().getVersion() );
    messageBuilder.append( Const.CR );
    messageBuilder.append( Const.CR );
    messageBuilder.append( Const.CR );
    messageBuilder.append( BaseMessages.getString( PKG, "System.CompanyInfo", Const.COPYRIGHT_YEAR ) );
    messageBuilder.append( Const.CR );
    messageBuilder.append( BaseMessages.getString( PKG, "System.ProductWebsiteUrl" ) );
    messageBuilder.append( Const.CR );
    messageBuilder.append( Const.CR );
    messageBuilder.append( Const.CR );
    messageBuilder.append( "Build version : " );
    messageBuilder.append( BuildVersion.getInstance().getVersion() );
    messageBuilder.append( Const.CR );
    messageBuilder.append( "Build date : " );
    messageBuilder.append( BuildVersion.getInstance().getBuildDate() ); // this should be the longest line of text

    return messageBuilder.toString();
  }
}
