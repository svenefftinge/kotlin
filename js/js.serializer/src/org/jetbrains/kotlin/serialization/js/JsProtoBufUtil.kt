/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.serialization.js

import com.google.protobuf.ExtensionRegistryLite
import org.jetbrains.kotlin.serialization.ClassData
import org.jetbrains.kotlin.serialization.PackageData
import org.jetbrains.kotlin.serialization.ProtoBuf
import org.jetbrains.kotlin.serialization.builtins.BuiltInsProtoBuf
import org.jetbrains.kotlin.serialization.deserialization.NameResolver
import java.io.ByteArrayInputStream

public object JsProtoBufUtil {
    kotlin.platform.platformStatic
    public fun getPackageData(nameResolver: NameResolver, content: ByteArray): PackageData {
        val registry = getExtensionRegistry()
        val packageProto = ProtoBuf.Package.parseFrom(ByteArrayInputStream(content), registry)

        return PackageData(nameResolver, packageProto)
    }

    kotlin.platform.platformStatic
    public fun getClassData(nameResolver: NameResolver, content: ByteArray): ClassData {
        val registry = getExtensionRegistry()
        val classProto = ProtoBuf.Class.parseFrom(ByteArrayInputStream(content), registry)

        return ClassData(nameResolver, classProto)
    }

    private fun getExtensionRegistry(): ExtensionRegistryLite {
        val registry = ExtensionRegistryLite.newInstance()
        BuiltInsProtoBuf.registerAllExtensions(registry)
        JsProtoBuf.registerAllExtensions(registry)

        return registry
    }
}