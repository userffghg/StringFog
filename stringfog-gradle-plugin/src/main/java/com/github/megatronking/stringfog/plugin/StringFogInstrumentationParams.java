package com.github.megatronking.stringfog.plugin;

import com.android.build.api.instrumentation.InstrumentationParameters;
import com.github.megatronking.stringfog.StringFogWrapper;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class StringFogInstrumentationParams implements InstrumentationParameters {

    @Input
    public abstract Property<String> getApplicationId();

    @Input
    public abstract Property<String> getClassName();

    private static class NonSerializableParams {
        final List<String> logs;
        final StringFogWrapper implementation;

        NonSerializableParams(List<String> logs, StringFogWrapper implementation) {
            this.logs = logs;
            this.implementation = implementation;
        }
    }

    private static final Map<String, WeakReference<StringFogExtension>> extensionForApplicationId = new HashMap<>();
    private static final WeakHashMap<StringFogExtension, NonSerializableParams> extensionNonSerializableParams = new WeakHashMap<>();

    public StringFogExtension getExtension() {
        WeakReference<StringFogExtension> ref = extensionForApplicationId.get(getApplicationId().get());
        if (ref == null || ref.get() == null) {
            throw new IllegalStateException("Extension has not been registered with setParameters");
        }
        return ref.get();
    }

    private NonSerializableParams getNonSerializableParameters() {
        StringFogExtension extension = getExtension();
        NonSerializableParams params = extensionNonSerializableParams.get(extension);
        if (params == null) {
            throw new IllegalStateException("runtimeParameters have not been registered with setParameters");
        }
        return params;
    }

    public List<String> getLogs() {
        return getNonSerializableParameters().logs;
    }

    public StringFogWrapper getImplementationWrapper() {
        return getNonSerializableParameters().implementation;
    }

    public void setParameters(String applicationId, StringFogExtension extension, List<String> logs, String className) {
        this.getApplicationId().set(applicationId);
        this.getClassName().set(className);
        extensionForApplicationId.put(applicationId, new WeakReference<>(extension));
        extensionNonSerializableParams.put(extension, new NonSerializableParams(
                logs,
                new StringFogWrapper(extension.getImplementation())
        ));
        logs.add("stringfog impl: " + extension.getImplementation());
        logs.add("stringfog mode: " + extension.getMode());
    }

}
