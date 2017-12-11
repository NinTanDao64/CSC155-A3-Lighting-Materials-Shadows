package a3;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class LightMoveRight extends AbstractAction {
	private Starter instance;
	
	public LightMoveRight (Starter ref) {
		super("Light Right");
		this.instance = ref;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		instance.lightMoveRight();
	}
}