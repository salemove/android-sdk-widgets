package com.glia.widgets.permissions

import androidx.appcompat.app.AppCompatActivity

/**
 * Glia internal class.
 *
 * It will be automatically added to the integrator's manifest file by the manifest merger during compilation.
 *
 * This is a helper activity used to request permissions in case
 * the current activity does not support ActivityResultLauncher requests.
 */
internal class PermissionsSupportActivity : AppCompatActivity()
