package a3;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class LightMoveDown extends AbstractAction {
	private Starter instance;
	
	public LightMoveDown (Starter ref) {
		super("Light Down");
		this.instance = ref;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		instance.lightMoveDown();
	}
}