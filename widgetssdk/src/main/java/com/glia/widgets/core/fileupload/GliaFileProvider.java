package com.glia.widgets.core.fileupload;

import androidx.core.content.FileProvider;

/**
 * Glia internal class.
 *
 * Will be automatically added to integrator Manifest by Manifest merger during compilation.
 *
 * This custom FileProvider is used to prevent confusion with request to the default FileProvider that might be used by the integrator.
 * In SDK it is being specified by the Context.fileProviderAuthority declared in Files.kt
 */
public class GliaFileProvider extends FileProvider {
}
