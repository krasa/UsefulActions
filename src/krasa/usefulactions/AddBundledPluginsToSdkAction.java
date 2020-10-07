package krasa.usefulactions;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

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
		Set<String> dependsOn = getDependsPluginIds(eventProject);
		Sdk projectSdk = ProjectRootManager.getInstance(eventProject).getProjectSdk();
		if (projectSdk.getSdkType().getName().equals("IDEA JDK")) {
			SdkModificator sdkModificator = projectSdk.getSdkModificator();
			String homePath = sdkModificator.getHomePath();

			VirtualFile[] ideaLibrary = getIdeaLibrary(homePath, dependsOn);

			for (VirtualFile aIdeaLib : ideaLibrary) {
				sdkModificator.addRoot(aIdeaLib, OrderRootType.CLASSES);
			}
			sdkModificator.commitChanges();
			LOG.info("SDK updated");
		}
	}

	@NotNull
	protected Set<String> getDependsPluginIds(Project eventProject) {
		Set<String> dependsOn = new HashSet<>();
		PsiFile[] filesByName = FilenameIndex.getFilesByName(eventProject, "plugin.xml", GlobalSearchScope.projectScope(eventProject));
		for (PsiFile psiFile : filesByName) {
			if (psiFile instanceof XmlFile) {
				XmlTag rootTag = ((XmlFile) psiFile).getRootTag();
				if (rootTag != null) {
					XmlTag[] depends = rootTag.findSubTags("depends");
					for (XmlTag depend : depends) {
						dependsOn.add(depend.getValue().getText());
					}
				}
			}
		}
		return dependsOn;
	}

	private VirtualFile[] getIdeaLibrary(String home, Set<String> pluginIds) {
		List<VirtualFile> result = new ArrayList<>();
		String plugins = home + File.separator + PLUGINS_DIR + File.separator;
		final File lib = new File(plugins);
		if (lib.isDirectory()) {
			File[] dirs = lib.listFiles();
			if (dirs != null) {
				for (File dir : dirs) {
					appendIdeaLibrary(plugins + dir.getName(), result, pluginIds);
				}
			}
		}
		return VfsUtilCore.toVirtualFileArray(result);
	}

	private void appendIdeaLibrary(@NotNull String libDirPath, @NotNull List<VirtualFile> result, Set<String> pluginIds, @NonNls final String... forbidden) {
		Arrays.sort(forbidden);
		final String path = libDirPath + File.separator + LIB_DIR_NAME;
		final JarFileSystem jfs = JarFileSystem.getInstance();
		final File lib = new File(path);
		if (lib.isDirectory()) {
			File[] jars = lib.listFiles();
			if (jars != null) {
				if (hasPluginId(jars, pluginIds)) {
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

	protected boolean hasPluginId(File[] jars, Set<String> pluginIds) {
		for (File jar : jars) {
			String name = jar.getName();

			if (name.endsWith(".jar") || name.endsWith(".zip")) {
				try {
					JarFile jarFile = null;
					try {
						jarFile = new JarFile(jar);
						Enumeration<JarEntry> enumOfJar = jarFile.entries();
						while (enumOfJar.hasMoreElements()) {
							JarEntry o = enumOfJar.nextElement();
							if (o.getName().equals("META-INF/plugin.xml")) {
								InputStream inputStream = jarFile.getInputStream(o);
								String s = IOUtils.toString(inputStream, "UTF-8");
								inputStream.close();
								String s1 = StringUtils.substringBetween(s, "<id>", "</id>");
								if (pluginIds.contains(s1)) {
									return true;
								}
							}
						}
					} finally {
						if (jarFile != null) {
							jarFile.close();
						}
					}
				} catch (Throwable ioe) {
					LOG.error(ioe);
				}
			}
		}
		return false;
	}
}
