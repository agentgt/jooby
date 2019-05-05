package io.jooby.run;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JoobyRun extends DefaultTask {

  static {
    System.setProperty("jooby.useShutdownHook", "false");
  }

  private String mainClass;

  private String executionMode = "DEFAULT";

  private List<String> restartExtensions = Arrays.asList("conf", "properties", "class");

  private List<String> compileExtensions = Arrays.asList("java", "kt");

  private ProjectConnection connection;

  @TaskAction
  public void run() throws Exception {
    Project current = getProject();
    List<Project> projects = Arrays.asList(current);

    if (mainClass == null) {
      mainClass = projects.stream()
          .filter(it -> it.getProperties().containsKey("mainClassName"))
          .map(it -> it.getProperties().get("mainClassName").toString())
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException(
              "Application class not found. Did you forget to set `mainClassName`?"));
    }

    HotSwap hotSwap = new HotSwap(current.getName(), mainClass, executionMode);

    connection = GradleConnector.newConnector()
        .useInstallation(current.getGradle().getGradleHomeDir())
        .forProjectDirectory(current.getRootDir())
        .connect();

    Runtime.getRuntime().addShutdownHook(new Thread(hotSwap::shutdown));

    BiConsumer<String, Path> onFileChanged = (event, path) -> {
      if (isCompileExtension(path)) {
        connection.newBuild()
            .setStandardError(System.err)
            .setStandardOutput(System.out)
            .forTasks("classes")
            .run();
        getLogger().debug("compilation done");
        getLogger().debug("Restarting application on file change: " + path);
        hotSwap.restart();
      } else if (isRestartExtension(path)) {
        getLogger().debug("Restarting application on file change: " + path);
        hotSwap.restart();
      } else {
        getLogger().debug("Ignoring file change: " + path);
      }
    };

    for (Project project : projects) {
      getLogger().debug("Adding project: " + project.getName());

      SourceSet sourceSet = sourceSet(project);
      // main/resources
      sourceSet.getResources().getSrcDirs().stream()
          .map(File::toPath)
          .forEach(file -> {
            hotSwap.addResource(file);
            hotSwap.addWatch(file, onFileChanged);
          });
      // conf directory
      Path conf = project.getProjectDir().toPath().resolve("conf");
      hotSwap.addResource(conf);
      hotSwap.addWatch(conf, onFileChanged);

      // build classes
      binDirectories(project, sourceSet).forEach(hotSwap::addResource);

      Set<Path> src = sourceDirectories(project, sourceSet);
      if (src.isEmpty()) {
        getLogger().debug("Compiler is off in favor of Eclipse compiler.");
        binDirectories(project, sourceSet).forEach(path -> hotSwap.addWatch(path, onFileChanged));
      } else {
        src.forEach(path -> hotSwap.addWatch(path, onFileChanged));
      }

      dependencies(project, sourceSet).forEach(hotSwap::addDependency);
    }

    // Block current thread.
    hotSwap.start();
  }

  private Set<Path> binDirectories(Project project, SourceSet sourceSet) {
    return classpath(project, sourceSet, it -> Files.exists(it) && Files.isDirectory(it));
  }

  private Set<Path> dependencies(Project project, SourceSet sourceSet) {
    return classpath(project, sourceSet, it -> Files.exists(it) && it.toString().endsWith(".jar"));
  }

  private Set<Path> classpath(Project project, SourceSet sourceSet, Predicate<Path> predicate) {
    Set<Path> result = new LinkedHashSet<>();
    // classes/main, resources/main + jars
    sourceSet.getRuntimeClasspath().getFiles().stream()
        .map(File::toPath)
        .filter(predicate)
        .forEach(result::add);

    // provided?
    Optional.ofNullable(project.getConfigurations().findByName("provided"))
        .map(Configuration::getFiles)
        .ifPresent(
            files -> files.stream().map(File::toPath).filter(predicate).forEach(result::add));

    return result;
  }

  private Set<Path> sourceDirectories(Project project, SourceSet sourceSet) {
    Path eclipse = project.getProjectDir().toPath().resolve(".classpath");
    if (Files.exists(eclipse)) {
      // let eclipse to do the incremental compilation
      return Collections.emptySet();
    }
    // main/java
    return sourceSet.getAllSource().getSrcDirs().stream()
        .map(File::toPath)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private SourceSet sourceSet(final Project project) {
    return getJavaConvention(project).getSourceSets()
        .getByName(SourceSet.MAIN_SOURCE_SET_NAME);
  }

  public JavaPluginConvention getJavaConvention(final Project project) {
    return project.getConvention().getPlugin(JavaPluginConvention.class);
  }

  private boolean isCompileExtension(Path path) {
    return containsExtension(compileExtensions, path);
  }

  private boolean isRestartExtension(Path path) {
    return containsExtension(restartExtensions, path);
  }

  private boolean containsExtension(List<String> extensions, Path path) {
    String filename = path.getFileName().toString();
    return extensions.stream().anyMatch(ext -> filename.endsWith("." + ext));
  }

  public String getMainClass() {
    return mainClass;
  }

  public void setMainClass(String mainClass) {
    this.mainClass = mainClass;
  }

  public String getExecutionMode() {
    return executionMode;
  }

  public void setExecutionMode(String executionMode) {
    this.executionMode = executionMode;
  }

  public List<String> getRestartExtensions() {
    return restartExtensions;
  }

  public void setRestartExtensions(List<String> restartExtensions) {
    this.restartExtensions = restartExtensions;
  }

  public List<String> getCompileExtensions() {
    return compileExtensions;
  }

  public void setCompileExtensions(List<String> compileExtensions) {
    this.compileExtensions = compileExtensions;
  }
}