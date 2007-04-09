package org.thenesis.midpath.test;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.*;

public class TextFieldTest extends MIDlet implements CommandListener {

	private Command exitCommand = new Command("Exit", Command.EXIT, 1);

	private Form form;

	protected void startApp() {

		form = new Form("Text Field");
		form.append(new TextField("Any Character", "", 20, TextField.ANY));
		form.append(new TextField("E-Mail", "", 20, TextField.EMAILADDR));

		form.addCommand(exitCommand);
		form.setCommandListener(this);

		Display.getDisplay(this).setCurrent(form);
	}

	public void commandAction(Command c, Displayable s) {
		if (c == exitCommand) {
			destroyApp(false);
			notifyDestroyed();
		}
	}

	protected void destroyApp(boolean unconditional) {
	}

	protected void pauseApp() {
	}
}
