package krasa.usefulactions.plugin;

import javax.swing.*;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;

import krasa.usefulactions.svn.UsefulActionsApplicationSettings;

@State(name = "UsefulActions", storages = { @Storage(file = "$APP_CONFIG$/UsefulActions.xml") })
public class UsefulActionsApplicationComponent implements Configurable,
		PersistentStateComponent<UsefulActionsApplicationSettings>, ProjectComponent, ApplicationComponent {

	public static final String REBUILD_DELAY = "ide.goto.rebuild.delay";
	private static final Logger LOG = Logger.getInstance(UsefulActionsApplicationComponent.class);

	private UsefulActionsApplicationSettings state;
	protected SettingsForm form;

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
		}
	}


	private void setVisibility() {
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
		if (state.getVersion() != 3) {
			state.setVersion(3);
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
