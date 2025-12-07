package com.github.megatronking.stringfog.plugin;

import com.android.build.api.instrumentation.AsmClassVisitorFactory;
import com.android.build.api.instrumentation.ClassContext;
import com.android.build.api.instrumentation.ClassData;
import org.objectweb.asm.ClassVisitor;

public abstract class StringFogTransform implements AsmClassVisitorFactory<StringFogInstrumentationParams> {

    @Override
    public ClassVisitor createClassVisitor(ClassContext classContext, ClassVisitor nextClassVisitor) {
        StringFogInstrumentationParams params = getParameters().get();
        return ClassVisitorFactory.create(
                params.getImplementationWrapper(),
                params.getLogs(),
                params.getExtension().getFogPackages(),
                params.getExtension().getKg(),
                params.getClassName().get(),
                classContext.getCurrentClassData().getClassName(),
                params.getExtension().getMode(),
                nextClassVisitor
        );
    }

    @Override
    public boolean isInstrumentable(ClassData classData) {
        return true;
    }

}
