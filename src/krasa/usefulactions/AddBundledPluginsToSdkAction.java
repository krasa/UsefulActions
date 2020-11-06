package krasa.usefulactions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

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
		new AddBundledPluginsToSdk(eventProject, addAll()).invoke();
	}

	protected boolean addAll() {
		return false;
	}


	private class AddBundledPluginsToSdk {
		private Project eventProject;
		private final boolean addAll;
		private Set<String> dependenciesToAdd;
		private Set<String> added = new HashSet<>();
		private List<PluginPojo> indexedPlugins = new ArrayList<>();

		public AddBundledPluginsToSdk(Project eventProject, boolean addAll) {
			this.eventProject = eventProject;
			this.addAll = addAll;
		}

		public void invoke() {
			work(eventProject);
		}

		private void work(Project eventProject) {
			dependenciesToAdd = getDependsPluginIds(eventProject);
			Sdk projectSdk = ProjectRootManager.getInstance(eventProject).getProjectSdk();
			if (projectSdk.getSdkType().getName().equals("IDEA JDK")) {
				SdkModificator sdkModificator = projectSdk.getSdkModificator();
				String homePath = sdkModificator.getHomePath();


				if (addAll) {
					addAllPlugins(sdkModificator, homePath);
				} else {
					indexFolders(homePath);
					processDependencies(sdkModificator);
				}

				sdkModificator.commitChanges();
				LOG.info("SDK updated");
			}
		}


		private void processDependencies(SdkModificator sdkModificator) {
			Map<String, PluginPojo> pluginPojoMap = indexedPlugins.stream().collect(Collectors.toMap(PluginPojo::getPluginId, Function.identity()));
			while (!dependenciesToAdd.isEmpty()) {
				for (String pluginId : new HashSet<>(dependenciesToAdd)) {
					dependenciesToAdd.remove(pluginId);

					if (added.contains(pluginId)) {
						continue;
					}


					PluginPojo pluginPojo = pluginPojoMap.get(pluginId);
					if (pluginPojo != null) {
						LOG.info("adding: " + pluginId);
						added.add(pluginId);
						addJarsToSdk(sdkModificator, pluginPojo.dir);
						dependenciesToAdd.addAll(pluginPojo.dependencies);
					} else {
						LOG.warn("plugin not found: " + pluginId);
					}
				}
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

		private void indexFolders(String home) {
			String plugins = home + File.separator + PLUGINS_DIR + File.separator;
			final File lib = new File(plugins);
			if (lib.isDirectory()) {
				File[] dirs = lib.listFiles();
				if (dirs != null) {
					for (File dir : dirs) {
						indexIdeaLibrary(plugins + dir.getName());
					}
				}
			}
		}

		private void addAllPlugins(SdkModificator sdkModificator, String homePath) {
			final File ideaPlugins = new File(homePath, PLUGINS_DIR);
			if (ideaPlugins.isDirectory()) {
				File[] ideaPlugin = ideaPlugins.listFiles();
				if (ideaPlugin != null) {
					for (File pluginDir : ideaPlugin) {
						final File pluginLibDir = new File(pluginDir, LIB_DIR_NAME);
						if (pluginLibDir.isDirectory()) {
							addJarsToSdk(sdkModificator, pluginLibDir);
						}
					}
				}
			}
		}

		private void indexIdeaLibrary(@NotNull String libDirPath, @NonNls final String... forbidden) {
			Arrays.sort(forbidden);
			final String path = libDirPath + File.separator + LIB_DIR_NAME;
			final File lib = new File(path);
			if (lib.isDirectory()) {
				File[] jars = lib.listFiles();
				if (jars != null) {
					PluginPojo pluginPojo = indexPlugins(lib, jars);
					indexedPlugins.add(pluginPojo);
				}
			}
		}

		protected PluginPojo indexPlugins(File lib, File[] jars) {
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
									String pluginId = StringUtils.substringBetween(s, "<id>", "</id>");
									PluginPojo pluginPojo = new PluginPojo(lib, pluginId);

									String[] depends = StringUtils.substringsBetween(s, "<depends", "</depends>");
									if (depends != null) {
										for (String depend : depends) {
											depend = StringUtils.substringAfterLast(depend, ">");
											pluginPojo.add(depend);
										}
									}


									return pluginPojo;
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
			return null;
		}

		private class PluginPojo {
			private final File dir;
			private final String pluginId;
			private final Set<String> dependencies = new HashSet<>();

			public PluginPojo(File dir, String pluginId) {
				this.dir = dir;
				this.pluginId = pluginId;
			}

			public String getPluginId() {
				return pluginId;
			}

			public void add(String depend) {
				dependencies.add(depend);
			}
		}

		private void addJarsToSdk(SdkModificator sdkModificator, File pluginLibDir) {
			final JarFileSystem jfs = JarFileSystem.getInstance();
			File[] files = pluginLibDir.listFiles();
			if (files != null) {
				for (File jar : files) {
					@NonNls
					String name = jar.getName();
					if (jar.isFile() && (name.endsWith(".jar") || name.endsWith(".zip"))) {
						VirtualFile file = jfs.findFileByPath(jar.getPath() + JarFileSystem.JAR_SEPARATOR);
						LOG.assertTrue(file != null, jar.getPath() + " not found");

						sdkModificator.addRoot(file, OrderRootType.CLASSES);
					}
				}
			}
		}
	}
}
