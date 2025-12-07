package com.github.megatronking.stringfog.plugin;

import com.github.megatronking.stringfog.IKeyGenerator;
import com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator;

public class StringFogExtension {

    public static final StringFogMode base64 = StringFogMode.base64;
    public static final StringFogMode bytes = StringFogMode.bytes;

    /** The package name of the application or the library. */
    private String packageName;

    /** The algorithm implementation for String encryption and decryption. It is required. */
    private String implementation;

    /** Key generator. Default is RandomKeyGenerator. */
    private IKeyGenerator kg = new RandomKeyGenerator();

    /** How the encrypted string presents in java class, default is base64. */
    private StringFogMode mode = base64;

    /** Enable or disable the StringFog plugin. Default is enabled. */
    private boolean enable = true;

    /** Enable or disable the StringFog debug message print. Default is disabled. */
    private boolean debug = false;

    /** The java packages will be applied. Default is effect on all packages. */
    private String[] fogPackages = new String[0];

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getImplementation() { return implementation; }
    public void setImplementation(String implementation) { this.implementation = implementation; }

    public IKeyGenerator getKg() { return kg; }
    public void setKg(IKeyGenerator kg) { this.kg = kg; }

    public StringFogMode getMode() { return mode; }
    public void setMode(StringFogMode mode) { this.mode = mode; }

    public boolean isEnable() { return enable; }
    public void setEnable(boolean enable) { this.enable = enable; }

    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    public String[] getFogPackages() { return fogPackages; }
    public void setFogPackages(String[] fogPackages) { this.fogPackages = fogPackages; }

}
