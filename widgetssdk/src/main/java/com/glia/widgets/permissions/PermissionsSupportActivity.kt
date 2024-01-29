package com.glia.widgets.permissions

import androidx.appcompat.app.AppCompatActivity

/**
 * Glia internal class.
 *
 * Will be automatically added to integrator Manifest by Manifest merger during compilation.
 *
 * This is a helper activity used to request permissions in case
 * the current activity does not support ActivityResultLauncher requests.
 */
internal class PermissionsSupportActivity : AppCompatActivity()
