package krasa.usefulactions;

import com.intellij.ide.actions.QuickSwitchSchemeAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.HttpConfigurable;

/**
 * @author Vojtech Krasa
 */
public class ChangeProxyAction extends QuickSwitchSchemeAction {

    protected void fillActions(final Project project, DefaultActionGroup group, DataContext dataContext) {
        HttpConfigurable state = HttpConfigurable.getInstance();
        group.add(new DumbAwareAction("Turn On", "", state.USE_HTTP_PROXY ? ourCurrentAction : ourNotCurrentAction) {
            public void actionPerformed(AnActionEvent e) {
                HttpConfigurable state = HttpConfigurable.getInstance();
                state.USE_HTTP_PROXY = true;
                state.USE_PROXY_PAC = false;
                ApplicationManager.getApplication().saveAll();

            }
        });
        group.add(new DumbAwareAction("Auto-Detect proxy", "", state.USE_PROXY_PAC ? ourCurrentAction : ourNotCurrentAction) {
            public void actionPerformed(AnActionEvent e) {
                HttpConfigurable state = HttpConfigurable.getInstance();
                state.USE_HTTP_PROXY = false;
                state.USE_PROXY_PAC = true;
                ApplicationManager.getApplication().saveAll();

            }
        });
        group.add(new DumbAwareAction("Turn Off", "", !state.USE_HTTP_PROXY && !state.USE_PROXY_PAC ? ourCurrentAction : ourNotCurrentAction) {
            public void actionPerformed(AnActionEvent e) {
                HttpConfigurable state = HttpConfigurable.getInstance();
                state.USE_PROXY_PAC = false;
                state.USE_HTTP_PROXY = false;
                ApplicationManager.getApplication().saveAll();
            }
        });
    }


    protected boolean isEnabled() {
        return true;
    }
}
