package krasa.usefulactions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class RestartAction extends DumbAwareAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		ApplicationManager.getApplication().exit(true, true, true);
	}
}
