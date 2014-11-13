package krasa.usefulactions.plugin;

import javax.swing.*;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.file.exclude.EnforcedPlainTextFileTypeFactory;
import krasa.usefulactions.svn.BrowseSvnRepoAction;
import krasa.usefulactions.svn.UsefulActionsApplicationSettings;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.registry.Registry;

@State(name = "UsefulActions", storages = { @Storage(id = "UsefulActions", file = "$APP_CONFIG$/UsefulActions.xml") })
public class UsefulActionsApplicationComponent implements Configurable,
		PersistentStateComponent<UsefulActionsApplicationSettings>, ProjectComponent, ApplicationComponent {

	public static final Icon ICON = IconLoader.getIcon("/krasa/usefulactions/plugin/svnBrowse.gif");
	public static final String IDE_MAX_RECENT_PROJECTS = "ide.max.recent.projects";
	public static final String REBUILD_DELAY = "ide.goto.rebuild.delay";
	private UsefulActionsApplicationSettings state;
	protected SettingsForm form;
	protected BrowseSvnRepoAction action;

	public static boolean openSettingsIfNotConfigured(Project project) {
		UsefulActionsApplicationComponent instance = getInstance();
		UsefulActionsApplicationSettings state = instance.getState();
		boolean b = true;
		if (StringUtils.isEmpty(state.getSvnAddress())) {
			b = ShowSettingsUtil.getInstance().editConfigurable(project, instance);
		}
		return b;
	}

	public void initComponent() {
		if (state == null) {
			state = new UsefulActionsApplicationSettings();
			state.setRecentProjectsSize(Registry.get("ide.max.recent.projects").asString());
		}
		createAndUpdateBrowseSvnAction();
	}

	private void createAndUpdateBrowseSvnAction() {
		if (action == null) {
			action = new BrowseSvnRepoAction(ICON) {

				@Override
				public void actionPerformed(AnActionEvent e) {
					try {
						super.actionPerformed(e);
					} catch (NoClassDefFoundError ex) {
						Notifier.notify(e.getProject(), "SVN plugin is required.", NotificationType.WARNING);
					}
				}
			};

		}
		setVisibility();
	}

	private void setVisibility() {
		if (action != null) {
			boolean showSvnBrowseButton = state.isShowSvnBrowseButton();
			DefaultActionGroup mainToolBar = (DefaultActionGroup) ActionManager.getInstance().getAction(
					IdeActions.GROUP_MAIN_TOOLBAR);
			mainToolBar.remove(action);
			if (showSvnBrowseButton) {
				mainToolBar.addAction(action);
			}
		}
	}

	public void disposeComponent() {
		// TODO: insert component disposal logic here
	}

	@NotNull
	public String getComponentName() {
		return "UsefulActions";
	}

	@NotNull
	@Override
	public UsefulActionsApplicationSettings getState() {
		return state;
	}

	@Override
	public void loadState(UsefulActionsApplicationSettings state) {
		this.state = state;
		setMaxRecentProjectsToRegistry(state);
		if (state.getVersion() == 1) {
			state.setVersion(2);
			state.setRebuildDelay("0");
		}
		setRebuildDelayToRegistry(state);
	}

	private void setMaxRecentProjectsToRegistry(UsefulActionsApplicationSettings state) {
		if (!StringUtils.isBlank(state.getRecentProjectsSize())) {
			Registry.get(IDE_MAX_RECENT_PROJECTS).setValue(Integer.parseInt(state.getRecentProjectsSize()));
		} else {
			Registry.get(IDE_MAX_RECENT_PROJECTS).resetToDefault();
		}
	}

	private void setRebuildDelayToRegistry(UsefulActionsApplicationSettings state) {
		if (!StringUtils.isBlank(state.getRebuildDelay())) {
			Registry.get(REBUILD_DELAY).setValue(Integer.parseInt(state.getRebuildDelay()));
		} else {
			Registry.get(REBUILD_DELAY).resetToDefault();
		}
	}

	@Nls
	@Override
	public String getDisplayName() {
		return "Useful Actions";
	}

	@Nullable
	@Override
	public String getHelpTopic() {
		return null;
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		if (form == null) {
			form = new SettingsForm();
		}
		return form.getRootComponent();
	}

	@Override
	public boolean isModified() {
		return form.isModified(state);
	}

	@Override
	public void apply() throws ConfigurationException {
		if (form != null) {
			form.getData(state);
			createAndUpdateBrowseSvnAction();

			setMaxRecentProjectsToRegistry(state);
			setRebuildDelayToRegistry(state);
		}
	}

	@Override
	public void reset() {
		if (form != null) {
			form.setData(state);
		}
	}

	@Override
	public void disposeUIResources() {
		form = null;
	}

	public static UsefulActionsApplicationComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(UsefulActionsApplicationComponent.class);
	}

	@Override
	public void projectOpened() {

	}

	@Override
	public void projectClosed() {
	}
}
