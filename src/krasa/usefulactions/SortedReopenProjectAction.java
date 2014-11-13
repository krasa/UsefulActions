package krasa.usefulactions;

import com.intellij.ide.ReopenProjectAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.impl.welcomeScreen.RecentProjectsAction;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class SortedReopenProjectAction extends RecentProjectsAction {
	protected void fillActions(final DefaultActionGroup group) {
		final AnAction[] recentProjectActions = Utils.getRecentProjectsManagerBase().getRecentProjectsActions(false);

		ArrayList<AnAction> list = new ArrayList<AnAction>(Arrays.asList(recentProjectActions));
		sort(list);

		for (AnAction action : list) {
			group.add(action);
		}
	}

	private void sort(ArrayList<AnAction> list) {
		Collections.sort(list, new Comparator<AnAction>() {
			@Override
			public int compare(AnAction o1, AnAction o2) {

				ReopenProjectAction reopenProjectAction = (ReopenProjectAction) o1;
				ReopenProjectAction reopenProjectAction2 = (ReopenProjectAction) o2;

				if (reopenProjectAction != null && reopenProjectAction2 != null) {
					return reopenProjectAction.getProjectName().compareToIgnoreCase(
							reopenProjectAction2.getProjectName());
				}
				return 0;
			}
		});
	}

	public void actionPerformed(final AnActionEvent e) {
		showPopup(e);
	}

	private void showPopup(final AnActionEvent e) {
		final DefaultActionGroup group = new DefaultActionGroup();
		fillActions(group);

		if (group.getChildrenCount() == 1 && isSilentlyChooseSingleOption()) {
			final AnAction[] children = group.getChildren(null);
			children[0].actionPerformed(e);
			return;
		}

		if (group.getChildrenCount() == 0) {
			group.add(new AnAction(getTextForEmpty()) {
				public void actionPerformed(AnActionEvent e) {
					group.setPopup(false);
				}
			});
		}

		final DataContext context = e.getDataContext();
		final ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(getCaption(), group, context,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);

		JComponent contextComponent = null;
		InputEvent inputEvent = e.getInputEvent();
		if (inputEvent instanceof MouseEvent) {
			if (inputEvent.getSource() instanceof JComponent) {
				contextComponent = (JComponent) inputEvent.getSource();
			}
		}

		showPopup(context, popup, contextComponent);
	}
}
