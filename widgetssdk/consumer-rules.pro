# Android instantiates ContentProviders by reflection; under R8 strict full mode (AGP 9 default)
# `-keep class` no longer implies the default constructor, so keep it explicitly.
-keep class com.glia.widgets.core.fileupload.GliaFileProvider { <init>(); }
-keepclassmembers class com.glia.widgets.chat.adapter.holder.WebViewViewHolder$JavaScriptInterface {
   public *;
}
