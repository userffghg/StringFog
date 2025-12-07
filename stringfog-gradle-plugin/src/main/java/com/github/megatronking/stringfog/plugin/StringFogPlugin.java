package com.github.megatronking.stringfog.plugin;

import com.android.build.api.instrumentation.FramesComputationMode;
import com.android.build.api.instrumentation.InstrumentationScope;
import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import groovy.xml.XmlParser;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StringFogPlugin implements Plugin<Project> {

    private static final String PLUGIN_NAME = "stringfog";

    private void forEachVariant(BaseExtension extension, Action<? super com.android.build.gradle.api.BaseVariant> action) {
        if (extension instanceof AppExtension) {
            ((AppExtension) extension).getApplicationVariants().all(action);
        } else if (extension instanceof LibraryExtension) {
            ((LibraryExtension) extension).getLibraryVariants().all(action);
        } else {
            throw new GradleException("StringFog plugin must be used with android app," +
                    "library or feature plugin");
        }
    }

    @Override
    public void apply(Project project) {
        project.getExtensions().create(PLUGIN_NAME, StringFogExtension.class);
        BaseExtension extension = project.getExtensions().findByType(BaseExtension.class);
        if (extension == null) {
            throw new GradleException("StringFog plugin must be used with android plugin");
        }

        AndroidComponentsExtension androidComponents = project.getExtensions().getByType(AndroidComponentsExtension.class);
        androidComponents.onVariants(variant -> {
            StringFogExtension stringfog = project.getExtensions().getByType(StringFogExtension.class);
            if (stringfog.getImplementation() == null || stringfog.getImplementation().isEmpty()) {
                throw new IllegalArgumentException("Missing stringfog implementation config");
            }
            if (!stringfog.isEnable()) {
                return;
            }

            String applicationId = null;
            File manifestFile = project.file("src/main/AndroidManifest.xml");
            if (manifestFile.exists()) {
                try {
                    Object parsedManifest = new XmlParser().parse(new InputStreamReader(new FileInputStream(manifestFile), "utf-8"));
                    if (!manifestFile.exists()) {
                        throw new IllegalArgumentException("Failed to parse file " + manifestFile);
                    }
                    Object attr = parsedManifest.getClass().getMethod("attribute", String.class).invoke(parsedManifest, "package");
                    if (attr != null) {
                        applicationId = attr.toString();
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to parse file " + manifestFile, e);
                }
            }
            if (applicationId == null || applicationId.isEmpty()) {
                // try namespace from extension
                try {
                    Object ns = extension.getClass().getMethod("getNamespace").invoke(extension);
                    if (ns != null) applicationId = ns.toString();
                } catch (Exception ignored) {
                }
            }
            if (applicationId == null || applicationId.isEmpty()) {
                applicationId = stringfog.getPackageName();
            }
            if (applicationId == null || applicationId.isEmpty()) {
                throw new IllegalArgumentException("Unable to resolve applicationId");
            }

            List<String> logs = new ArrayList<>();
            variant.getInstrumentation().transformClassesWith(StringFogTransform.class, InstrumentationScope.PROJECT, params -> {
                ((StringFogInstrumentationParams) params).setParameters(
                        applicationId,
                        stringfog,
                        logs,
                        applicationId + "." + SourceGeneratingTask.FOG_CLASS_NAME
                );
            });
            variant.getInstrumentation().setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS);

            // Generate the source generation task per variant
            forEachVariant(extension, variantObj -> {
                String variantName = variantObj.getName();
                String generateTaskName = "generateStringFog" + (variantName.substring(0,1).toUpperCase() + variantName.substring(1));
                if (!project.getTasks().getByName(generateTaskName).getDependsOn().isEmpty()) {
                    return;
                }
                File stringfogDir = new File(project.getBuildDir(), "generated" + File.separatorChar + "source" + File.separatorChar + "stringFog" + File.separatorChar + variantName.substring(0,1).toUpperCase() + variantName.substring(1).toLowerCase());
                TaskProvider<?> provider = project.getTasks().register(generateTaskName, SourceGeneratingTask.class, task -> {
                    ((SourceGeneratingTask) task).getGenDir().set(stringfogDir);
                    ((SourceGeneratingTask) task).getApplicationId().set(applicationId);
                    ((SourceGeneratingTask) task).getImplementation().set(stringfog.getImplementation());
                    ((SourceGeneratingTask) task).getMode().set(stringfog.getMode());
                });
                variantObj.registerJavaGeneratingTask(provider, stringfogDir);
            });
        });
    }

}
