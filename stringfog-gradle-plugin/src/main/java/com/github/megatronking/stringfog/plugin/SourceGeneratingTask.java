package com.github.megatronking.stringfog.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;

@CacheableTask
public abstract class SourceGeneratingTask extends DefaultTask {

    public static final String FOG_CLASS_NAME = "StringFog";

    @Input
    public abstract Property<File> getGenDir();

    @Input
    public abstract Property<String> getApplicationId();

    @Input
    public abstract Property<String> getImplementation();

    @Input
    public abstract Property<StringFogMode> getMode();

    @Inject
    public SourceGeneratingTask() {
    }

    @TaskAction
    public void injectSource() {
        if (!getGenDir().get().exists()) {
            getGenDir().get().mkdirs();
        }

        File outputFile = new File(getGenDir().get(), getApplicationId().get().replace('.', File.separatorChar) + File.separator + "StringFog.java");
        StringFogClassGenerator.generate(outputFile, getApplicationId().get(), FOG_CLASS_NAME,
                getImplementation().get(), getMode().get());
    }

}
