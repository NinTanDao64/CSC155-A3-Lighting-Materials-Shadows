package a3;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class LightMoveUp extends AbstractAction {
	private Starter instance;
	
	public LightMoveUp (Starter ref) {
		super("Light Up");
		this.instance = ref;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		instance.lightMoveUp();
	}
}