package krasa.usefulactions.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import krasa.usefulactions.svn.UsefulActionsApplicationSettings;

import org.jetbrains.annotations.NotNull;

public class SettingsForm {
	private JPanel rootComponent;
	private JTextField address;
	// private JButton browseButton;
	private JCheckBox showSvnBrowseButton;
	private JLabel svnAddressLabel;
	private JTextField recentProjectsSize;
	private JLabel recentProjectsSizeLabel;
	private JTextField rebuildDelay;
	private JLabel rebuildDelayLabel;

	public SettingsForm() {
		// browseButton.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// }
		// });
		JToggleButton[] modifiableButtons = { showSvnBrowseButton };
		for (JToggleButton button : modifiableButtons) {
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateComponents();
				}
			});
		}

	}

	private void updateComponents() {
		enabledBy(new JComponent[] { address, svnAddressLabel }, showSvnBrowseButton);
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
		address.setText(data.getSvnAddress());
		showSvnBrowseButton.setSelected(data.isShowSvnBrowseButton());
		recentProjectsSize.setText(data.getRecentProjectsSize());
		rebuildDelay.setText(data.getRebuildDelay());
	}

	public void getData(UsefulActionsApplicationSettings data) {
		data.setSvnAddress(address.getText());
		data.setShowSvnBrowseButton(showSvnBrowseButton.isSelected());
		data.setRecentProjectsSize(recentProjectsSize.getText());
		data.setRebuildDelay(rebuildDelay.getText());
	}

	public boolean isModified(UsefulActionsApplicationSettings data) {
		if (address.getText() != null ? !address.getText().equals(data.getSvnAddress()) : data.getSvnAddress() != null)
			return true;
		if (showSvnBrowseButton.isSelected() != data.isShowSvnBrowseButton())
			return true;
		if (recentProjectsSize.getText() != null ? !recentProjectsSize.getText().equals(data.getRecentProjectsSize())
				: data.getRecentProjectsSize() != null)
			return true;
		if (rebuildDelay.getText() != null ? !rebuildDelay.getText().equals(data.getRebuildDelay())
				: data.getRebuildDelay() != null)
			return true;
		return false;
	}
}
