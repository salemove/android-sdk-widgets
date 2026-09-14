# Theming migration guide

> How to move an integration from the legacy `gliaChatStyle` chain to the `GliaTheme` style
> introduced in Widgets SDK 4.0.0.

## TL;DR

Replace the four-style legacy chain with one style:

```xml
<!-- app/src/main/res/values/glia_theme.xml -->
<style name="GliaTheme">
    <item name="gliaBrandPrimaryColor">@color/brand</item>
    <item name="gliaIconAppBarBack">@drawable/back</item>
</style>
```

That is the whole contract. No activity theme to override, no `gliaChatStyle`, no
`materialThemeOverlay`, and nothing to declare for the attributes you leave alone.

**The legacy chain still works.** Nothing about it changed, so an integration that does not touch its
resources keeps rendering exactly as before. It is *deprecated*, not removed: `gliaChatStyle`,
`Application.Glia.Activity.Style`, `Application.Glia.Chat`, `ThemeOverlay.Glia.Chat` and the four
`Application.Glia.*.Activity` themes are all marked `@deprecated`, and the SDK logs a single warning
naming this guide the first time it detects a `gliaChatStyle`. Migrating is worthwhile because the
new form is one file with no inheritance to get wrong — but it is not urgent.

## What changed and why

Every `glia*` attribute the SDK reads now lives in a real theme, `Theme.Glia.Internal`, which every
Glia activity, service and wrapped view resolves against. Layouts read `?attr/glia*` directly; there
is no longer a parallel code path that parses the same values into an object and re-applies them.

`GliaTheme` is an **empty** style the SDK declares and you redefine. Because Android resource merging
replaces a library style wholesale when the app declares one of the same name, and because every
default lives in `Theme.Glia.Internal` rather than in `GliaTheme`, a partial redefinition is safe:
your style contributes exactly the items you write and nothing else.

One screen gains customization along the way: the image preview (files an operator sends) never
participated in the legacy mechanism, but it does resolve `GliaTheme` — setting `gliaIconAppBarBack`
or `gliaBrandPrimaryColor` now reaches it too.

## Precedence

Lowest to highest:

```
Theme.Glia.Internal                     SDK defaults for every glia* attribute
  └─ legacy gliaChatStyle → materialThemeOverlay    only the items that overlay declares
       └─ GliaTheme                                 only the items you declare
            └─ Remote JSON configuration            applied in code, always last
```

So if you set an attribute in both mechanisms, `GliaTheme` wins; remote JSON
(`GliaWidgetsConfig.setUiJsonRemoteConfig`) still wins over everything.

Mixing the two is supported and is the recommended way to migrate incrementally: add `GliaTheme`,
move attributes into it a few at a time, and delete the legacy styles once it is empty.

## Migrating, step by step

The old chain looked like this:

```xml
<!-- 1. override the SDK's activity-style resource … -->
<style name="Application.Glia.Activity.Style" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <item name="gliaChatStyle">@style/MyApp.Glia.Chat</item>              <!-- 2. … to point at a style … -->
</style>

<style name="MyApp.Glia.Chat" parent="Application.Glia.Chat">
    <item name="materialThemeOverlay">@style/MyApp.Glia.Overlay</item>    <!-- 3. … that points at an overlay … -->
</style>

<style name="MyApp.Glia.Overlay" parent="ThemeOverlay.Glia.Chat">          <!-- 4. … that carries the values -->
    <item name="gliaBrandPrimaryColor">@color/brand</item>
    <item name="gliaIconAppBarBack">@drawable/back</item>
</style>
```

**Move the items from step 4 into `GliaTheme` verbatim and delete steps 1–3.** Attribute names are
identical — there is no renaming, no reformatting and no reordering:

```xml
<style name="GliaTheme">
    <item name="gliaBrandPrimaryColor">@color/brand</item>
    <item name="gliaIconAppBarBack">@drawable/back</item>
</style>
```

One thing to check while you do it: **drop `parent`.** `GliaTheme` is a single-segment name, so
Android infers no implicit parent and you need none. Do not parent it to `ThemeOverlay.Glia.Chat`;
that style is a blank backward-compatibility placeholder.

### Verifying the migration

- Run the app and open any Glia screen. The one-time
  `Deprecated gliaChatStyle theming detected` warning must no longer appear in Logcat — it only
  fires while a `gliaChatStyle` is still declared somewhere.
- Spot-check the surfaces your overlay customized (brand colour, icons, dialog button alignment).
  Attribute names are 1:1, so anything that looked right before must look identical after.

### React Native, Flutter and other wrappers

`GliaTheme` is readable without any Activity or theme context, so a wrapper can ship theming as one
XML file dropped into `android/app/src/main/res/values/`. Nothing needs to touch the host app's
`AppTheme` or `LaunchTheme`, and no native code runs to apply it.

## Surveys now follow your theme

Survey question widgets — the single-choice radio tint, the boolean and scale selected states,
titles, borders and error states — now resolve the theme (`gliaBrandPrimaryColor`,
`gliaBaseDarkColor`, `gliaBaseNormalColor`, `gliaSystemNegativeColor`). Previously they kept the
SDK's default blue regardless of any customization, so **an integration with a custom brand colour
will see its surveys pick it up** starting with 4.0.0. No action is needed; if the new rendering is
unwanted, remote JSON (`unifiedTheme.surveyTheme`) styles surveys explicitly and is unaffected by
this change.

## Reference

- Attribute names and formats: `widgetssdk/src/main/res/values/attrs.xml`
- Defaults and composition order: `widgetssdk/src/main/res/values/themes.xml` (`Theme.Glia.Internal`)
- The composition itself: `widgetssdk/src/main/java/com/glia/widgets/helper/GliaThemeOverlays.kt`
- Worked example, both mechanisms side by side: `app/src/main/res/values/themes.xml`
- Remote JSON (Unified) customization: [Glia docs — Android Customization](https://docs.glia.com/developer-portal/android-customization)
