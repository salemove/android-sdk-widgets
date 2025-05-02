package com.glia.widgets.core.fileupload

import androidx.core.content.FileProvider

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * This custom `FileProvider` is used to prevent confusion with requests to the default `FileProvider` that may be used by the integrator.
 * In the SDK, it is specified by `Context.fileProviderAuthority` declared in `Files.kt`.
 */
internal class GliaFileProvider : FileProvider()
