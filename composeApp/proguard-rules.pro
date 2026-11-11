# Project-specific R8/ProGuard rules.

# Preserve Kotlin reflection metadata, annotations, and generic signatures
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, AnnotationDefault
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# Coroutines & Flow (required for reflection on Flow<T> types in RPC)
-keep class kotlinx.coroutines.flow.Flow { *; }
-keep interface kotlinx.coroutines.flow.Flow { *; }
-keep class kotlinx.coroutines.flow.** { *; }

# kotlinx-rpc
-keep @interface kotlinx.rpc.annotations.Rpc
-keep @kotlinx.rpc.annotations.Rpc interface * { *; }
-keep class * implements kotlinx.rpc.RemoteService { *; }
-keep class **Descriptor { *; }
-keep class **Stub { *; }
-keep class **__Rpc*Descriptor { *; }
-keep class **__Rpc*Stub { *; }
-keep class **$$rpcServiceStub { *; }
-keep class **$$rpcServiceStub* { *; }
-keep class **$$rpcServiceDescriptor { *; }
-keep class **$$rpcServiceDescriptor* { *; }

# Shared contracts, models, and RPC services
-keep class com.ensodai.shared.avalon.** { *; }
-keep class kotlinx.rpc.** { *; }
-dontwarn kotlinx.rpc.**

# kotlinx.serialization
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepnames class kotlinx.serialization.json.** { *; }




