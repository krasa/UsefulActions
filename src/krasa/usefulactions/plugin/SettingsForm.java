package krasa.usefulactions.plugin;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import krasa.usefulactions.svn.UsefulActionsApplicationSettings;

public class SettingsForm {
	private JPanel rootComponent;

	public SettingsForm() {
		// browseButton.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// }
		// });

	}

	private void updateComponents() {
	}

	private void enabledBy(@NotNull JComponent[] targets, @NotNull JToggleButton... control) {
		boolean b = true;
		for (JToggleButton jToggleButton : control) {
			b = b && (jToggleButton.isEnabled() && jToggleButton.isSelected());
		}
		for (JComponent target : targets) {
			target.setEnabled(b);
		}
	}

	private void enabledByAny(@NotNull JComponent[] targets, @NotNull JToggleButton... control) {
		boolean b = false;
		for (JToggleButton jToggleButton : control) {
			b = b || (jToggleButton.isEnabled() && jToggleButton.isSelected());
		}
		for (JComponent target : targets) {
			target.setEnabled(b);
		}
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
	}

	public void setData(UsefulActionsApplicationSettings data) {
	}

	public void getData(UsefulActionsApplicationSettings data) {
	}

	public boolean isModified(UsefulActionsApplicationSettings data) {
		return false;
	}
}
