package a3;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class LightMoveBackward extends AbstractAction {
	private Starter instance;
	
	public LightMoveBackward (Starter ref) {
		super("Light Backward");
		this.instance = ref;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		instance.lightMoveBackward();
	}
}