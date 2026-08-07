# Theming migration guide

> How to move an integration from the legacy `gliaChatStyle` chain to `GliaTheme`, and what the
> major version removes.

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
resources keeps rendering exactly as before. Migrating is worthwhile because the new form is one file
with no inheritance to get wrong — but it is not urgent.

## What changed and why

Every `glia*` attribute the SDK reads now lives in a real theme, `Theme.Glia.Internal`, which every
Glia activity, service and wrapped view resolves against. Layouts read `?attr/glia*` directly; there
is no longer a parallel code path that parses the same values into an object and re-applies them.

`GliaTheme` is an **empty** style the SDK declares and you redefine. Because Android resource merging
replaces a library style wholesale when the app declares one of the same name, and because every
default lives in `Theme.Glia.Internal` rather than in `GliaTheme`, a partial redefinition is safe:
your style contributes exactly the items you write and nothing else.

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

Two things to check while you do it:

- **Drop `parent`.** `GliaTheme` is a single-segment name, so Android infers no implicit parent and
  you need none. Do not parent it to `ThemeOverlay.Glia.Chat`; that style is a blank
  backward-compatibility placeholder.
- **Un-prefixed item names are gone.** If your overlay set `<item name="brandPrimaryColor">` rather
  than `<item name="gliaBrandPrimaryColor">`, see [Removed](#removed-in-this-major-version) below —
  those now fail to compile, and the fix is to add the `glia` prefix.

### React Native, Flutter and other wrappers

`GliaTheme` is readable without any Activity or theme context, so a wrapper can ship theming as one
XML file dropped into `android/app/src/main/res/values/`. Nothing needs to touch the host app's
`AppTheme` or `LaunchTheme`, and no native code runs to apply it.

## Removed in this major version

### Public configuration classes

Survey and widget styling used to be expressed as Java configuration objects built at runtime. They
are replaced by the `glia*` attributes and remote JSON, and are deleted:

| Removed | Replacement |
|---|---|
| `view.configuration.survey.SurveyStyle` | `GliaTheme` attributes / `unifiedTheme.surveyTheme` |
| `view.configuration.survey.BooleanQuestionConfiguration` | as above |
| `view.configuration.survey.InputQuestionConfiguration` | as above |
| `view.configuration.survey.ScaleQuestionConfiguration` | as above |
| `view.configuration.survey.SingleQuestionConfiguration` | as above |
| `view.configuration.OptionButtonConfiguration` | as above |
| `view.configuration.ButtonConfiguration` | the button styles resolved from the theme |
| `view.configuration.TextConfiguration` | the text appearances resolved from the theme |
| `view.configuration.LayerConfiguration` | `gliaBaseLightColor` (the survey card fill) |
| `view.configuration.ChatHeadConfiguration` | `gliaBrandPrimaryColor`, `gliaIconPlaceholder`, `gliaBaseLightColor`, `gliaIconOnHold` |
| `view.OutlinedOptionView` (`@hide`) | — no inflation site existed |

### Behaviour change: survey widgets follow your brand colour

Survey question widgets — the single-choice radio tint, and the boolean and scale selected states —
used to render Glia blue regardless of the theme, because their defaults read colour resources
directly. They now resolve `?attr/gliaBrandPrimaryColor`, so **an integration with a custom brand
colour will see its survey change colour**. Titles, borders and error states likewise follow
`gliaBaseDarkColor`, `gliaBaseNormalColor` and `gliaSystemNegativeColor`.

Remote JSON (`unifiedTheme.surveyTheme`) already styled surveys correctly and is unaffected.

### Un-prefixed attribute names

The `GliaView` styleable and the ~45 un-prefixed `<attr>` declarations inside it
(`brandPrimaryColor`, `baseLightColor`, `iconAppBarBack`, …) are removed. They were never documented
and only ever worked as a side effect of that styleable being public: values set that way reached the
SDK's code-side theme object while every layout, which resolves `?attr/glia*`, ignored them.

Removing them is a **compile** break, never a silent one — AAPT reports
`resource attr/brandPrimaryColor not found`. The fix is to prefix the name: `brandPrimaryColor` →
`gliaBrandPrimaryColor`, `iconAppBarBack` → `gliaIconAppBarBack`, and so on for every item.

### Attributes with no effect

`gliaBotActionButtonSelectedBackgroundColor` and `gliaBotActionButtonSelectedTextColor` are removed.
Response-card option buttons have no selected state — a card is answered once and never re-rendered —
so these never rendered anything.

## Reference

- Attribute names and formats: `widgetssdk/src/main/res/values/attrs.xml`
- Defaults and composition order: `widgetssdk/src/main/res/values/themes.xml` (`Theme.Glia.Internal`)
- The composition itself: `widgetssdk/src/main/java/com/glia/widgets/helper/GliaThemeOverlays.kt`
- Worked example, both mechanisms side by side: `app/src/main/res/values/themes.xml`
- Remote JSON configuration: [view/unifiedui/CLAUDE.md](../widgetssdk/src/main/java/com/glia/widgets/view/unifiedui/CLAUDE.md)
