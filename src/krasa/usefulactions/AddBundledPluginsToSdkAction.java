package krasa.usefulactions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;

public class AddBundledPluginsToSdkAction extends AnAction {
	private static final Logger LOG = Logger.getInstance(AddBundledPluginsToSdkAction.class);

	@NonNls
	private static final String PLUGINS_DIR = "plugins";
	@NonNls
	private static final String LIB_DIR_NAME = "lib";

	@java.lang.Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		Project eventProject = getEventProject(anActionEvent);
		if (eventProject == null) {
			return;
		}
		Sdk projectSdk = ProjectRootManager.getInstance(eventProject).getProjectSdk();
		if (projectSdk.getSdkType().getName().equals("IDEA JDK")) {
			SdkModificator sdkModificator = projectSdk.getSdkModificator();
			String homePath = sdkModificator.getHomePath();

			VirtualFile[] ideaLibrary = getIdeaLibrary(homePath);

			for (VirtualFile aIdeaLib : ideaLibrary) {
				sdkModificator.addRoot(aIdeaLib, OrderRootType.CLASSES);
			}
			sdkModificator.commitChanges();
			LOG.info("SDK updated");
		}
	}

	private static VirtualFile[] getIdeaLibrary(String home) {
		List<VirtualFile> result = new ArrayList<>();
		String plugins = home + File.separator + PLUGINS_DIR + File.separator;
		final File lib = new File(plugins);
		if (lib.isDirectory()) {
			File[] dirs = lib.listFiles();
			if (dirs != null) {
				for (File dir : dirs) {
					appendIdeaLibrary(plugins + dir.getName(), result);
				}
			}
		}
		return VfsUtilCore.toVirtualFileArray(result);
	}

	private static void appendIdeaLibrary(@NotNull String libDirPath, @NotNull List<VirtualFile> result, @NonNls final String... forbidden) {
		Arrays.sort(forbidden);
		final String path = libDirPath + File.separator + LIB_DIR_NAME;
		final JarFileSystem jfs = JarFileSystem.getInstance();
		final File lib = new File(path);
		if (lib.isDirectory()) {
			File[] jars = lib.listFiles();
			if (jars != null) {
				for (File jar : jars) {
					@NonNls
					String name = jar.getName();
					if (jar.isFile() && Arrays.binarySearch(forbidden, name) < 0 && (name.endsWith(".jar") || name.endsWith(".zip"))) {
						VirtualFile file = jfs.findFileByPath(jar.getPath() + JarFileSystem.JAR_SEPARATOR);
						LOG.assertTrue(file != null, jar.getPath() + " not found");
						result.add(file);
					}
				}
			}
		}
	}
}
