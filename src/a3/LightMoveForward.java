package a3;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class LightMoveForward extends AbstractAction {
	private Starter instance;
	
	public LightMoveForward (Starter ref) {
		super("Light Forward");
		this.instance = ref;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		instance.lightMoveForward();
	}
}