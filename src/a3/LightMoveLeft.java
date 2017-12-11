package a3;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class LightMoveLeft extends AbstractAction {
	private Starter instance;
	
	public LightMoveLeft (Starter ref) {
		super("Light Left");
		this.instance = ref;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		instance.lightMoveLeft();
	}
}