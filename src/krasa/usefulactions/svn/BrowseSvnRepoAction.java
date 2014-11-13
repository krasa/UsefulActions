package krasa.usefulactions.svn;

import java.awt.*;
import java.util.Enumeration;

import javax.swing.*;

import krasa.usefulactions.plugin.UsefulActionsApplicationComponent;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.SvnApplicationSettings;
import org.jetbrains.idea.svn.actions.BrowseRepositoryAction;
import org.jetbrains.idea.svn.dialogs.RepositoryBrowserDialog;
import org.jetbrains.idea.svn.dialogs.RepositoryTreeNode;
import org.jetbrains.idea.svn.dialogs.RepositoryTreeRootNode;

import com.intellij.ide.actions.ContextHelpAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class BrowseSvnRepoAction extends AnAction {
    public static final String REPOSITORY_BROWSER_TOOLWINDOW = "SVN Repositories";

    public BrowseSvnRepoAction() {
    }

    public BrowseSvnRepoAction(Icon icon) {
        super(icon);
    }

    public BrowseSvnRepoAction(String text) {
        super(text);
    }

    public BrowseSvnRepoAction(String text, String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        boolean b = UsefulActionsApplicationComponent.openSettingsIfNotConfigured(project);
        if (!b) {
            return;
        }
        addUrlIfNotExists();

        if (project == null) {
            RepositoryBrowserDialog dialog = new RepositoryBrowserDialog(
                    ProjectManager.getInstance().getDefaultProject());
            dialog.show();
        } else {
            ToolWindowManager manager = ToolWindowManager.getInstance(project);
            ToolWindow w = manager.getToolWindow(REPOSITORY_BROWSER_TOOLWINDOW);
            RepositoryToolWindowPanel component = null;
            if (w == null) {
                component = new RepositoryToolWindowPanel(project);
                w = addToolwindow(project, manager, component);
            } else {
                JComponent component1 = w.getContentManager().getContent(0).getComponent();
                if (component1 instanceof RepositoryToolWindowPanel) {
                    component = (RepositoryToolWindowPanel) component1;
                } else { // when there is already opened IntelliJ browse
                    w.getContentManager().removeContent(w.getContentManager().getContent(0), true);
                    component = new RepositoryToolWindowPanel(project);
                    w = addToolwindow(project, manager, component);
                }

            }
            w.show(null);
            w.activate(null);
            if (component != null) {
                component.setSelectedNode();
            }
        }
    }

    private ToolWindow addToolwindow(Project project, ToolWindowManager manager, RepositoryToolWindowPanel component) {
        ToolWindow w;
        w = manager.registerToolWindow(REPOSITORY_BROWSER_TOOLWINDOW, true, ToolWindowAnchor.BOTTOM,
                project, true);
        final Content content = ContentFactory.SERVICE.getInstance().createContent(component, "", false);
        Disposer.register(content, component);
        w.getContentManager().addContent(content);
        return w;
    }

    private void addUrlIfNotExists() {
        String svnAddress = getSvnAddress();
        if (!StringUtil.isEmpty(svnAddress)) {
            SvnApplicationSettings settings = SvnApplicationSettings.getInstance();
            settings.addTypedUrl(svnAddress);
            settings.addCheckoutURL(svnAddress);
        }
    }

    private static class RepositoryToolWindowPanel extends JPanel implements Disposable {
        private final MyRepositoryBrowserDialog myDialog;
        private final Project myProject;

        private RepositoryToolWindowPanel(final Project project) {
            super(new BorderLayout());
            myProject = project;

            myDialog = new MyRepositoryBrowserDialog(project);
            JComponent component = myDialog.createBrowserComponent(true);

            add(component, BorderLayout.CENTER);
            add(myDialog.createToolbar(false, new ContextHelpAction("reference.svn.repository")), BorderLayout.WEST);
        }

        public void setSelectedNode() {
            myDialog.setSelectedNode(getSvnAddress());
        }

        public void dispose() {
            myDialog.disposeRepositoryBrowser();
            ToolWindowManager.getInstance(myProject).unregisterToolWindow(
                    BrowseRepositoryAction.REPOSITORY_BROWSER_TOOLWINDOW);
        }
    }

    private static String getSvnAddress() {
        return UsefulActionsApplicationComponent.getInstance().getState().getSvnAddress();
    }

    static class MyRepositoryBrowserDialog extends RepositoryBrowserDialog {

        public MyRepositoryBrowserDialog(Project project) {
            super(project);
        }

        public MyRepositoryBrowserDialog(Project project, boolean showFiles, @Nullable String repositoriesLabelText) {
            super(project, showFiles, repositoriesLabelText);
        }

        public void setSelectedNode(String path) {
            JTree repositoryTree = getRepositoryBrowser().getRepositoryTree();
            RepositoryTreeRootNode root = (RepositoryTreeRootNode) repositoryTree.getModel().getRoot();
            Enumeration children = root.children();
            while (children.hasMoreElements()) {
                Object o = children.nextElement();
                if (o instanceof RepositoryTreeNode) {
                    RepositoryTreeNode next = (RepositoryTreeNode) o;
                    if (path.equals(next.getURL().toString())) {
                        getRepositoryBrowser().setSelectedNode(next);
                        getRepositoryBrowser().expandNode(next);
                        repositoryTree.scrollPathToVisible(repositoryTree.getSelectionPath());
                    }
                }

            }
        }

    }
}
