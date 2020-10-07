package krasa.usefulactions;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.Project;

public class AddAllBundledPluginsToSdkAction extends AddBundledPluginsToSdkAction {
	@Override
	protected boolean hasPluginId(File[] jars, Set<String> pluginIds) {
		return true;
	}

	@NotNull
	@Override
	protected Set<String> getDependsPluginIds(Project eventProject) {
		return Collections.emptySet();
	}
}
