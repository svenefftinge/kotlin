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

import com.google.protobuf.ByteString
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.builtins.BuiltInsSerializationUtil
import org.jetbrains.kotlin.builtins.createBuiltInPackageFragmentProvider
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentProvider
import org.jetbrains.kotlin.load.kotlin.PackageClassUtils
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.serialization.DescriptorSerializer
import org.jetbrains.kotlin.serialization.ProtoBuf
import org.jetbrains.kotlin.serialization.SerializationUtil
import org.jetbrains.kotlin.serialization.builtins.BuiltInsSerializerExtension
import org.jetbrains.kotlin.serialization.deserialization.FlexibleTypeCapabilitiesDeserializer
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.utils.KotlinJavascriptMetadataUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.platform.platformStatic

public object KotlinJavascriptSerializationUtil {
    public val META_EXT: String = "kjsm"
    public val DOT_META_EXT: String = "." + META_EXT

    private val CLASS_METADATA_FILE_EXTENSION = ".kotlin_class"

    platformStatic
    public fun createPackageFragmentProvider(moduleDescriptor: ModuleDescriptor, metadata: ByteArray, storageManager: StorageManager): PackageFragmentProvider? {
        val gzipInputStream = GZIPInputStream(ByteArrayInputStream(metadata))
        val content = JsProtoBuf.Library.parseFrom(gzipInputStream)
        gzipInputStream.close()

        val contentMap: MutableMap<String, ByteArray> = hashMapOf()
        content.getEntryList().forEach { entry -> contentMap[entry.getPath()] = entry.getContent().toByteArray() }

        val packageFqNames = getPackages(contentMap).map { FqName(it) }.toSet()
        if (packageFqNames.isEmpty()) return null

        return createBuiltInPackageFragmentProvider(
                storageManager, moduleDescriptor, packageFqNames, FlexibleTypeCapabilitiesDeserializer.Dynamic
        ) {
            path ->
            if (!contentMap.containsKey(path)) null else ByteArrayInputStream(contentMap.get(path))
        }
    }

    public fun contentMapToByteArray(contentMap: Map<String, ByteArray>): ByteArray {
        val contentBuilder = JsProtoBuf.Library.newBuilder()
        contentMap forEach {
            val entry = JsProtoBuf.Library.FileEntry.newBuilder().setPath(it.getKey()).setContent(ByteString.copyFrom(it.getValue())).build()
            contentBuilder.addEntry(entry)
        }

        val byteStream = ByteArrayOutputStream()
        val gzipOutputStream = GZIPOutputStream(byteStream)
        contentBuilder.build().writeTo(gzipOutputStream)
        gzipOutputStream.close()

        return byteStream.toByteArray()
    }

    public fun metadataAsString(moduleName: String, moduleDescriptor: ModuleDescriptor): String =
        KotlinJavascriptMetadataUtils.formatMetadataAsString(moduleName, moduleDescriptor.toBinaryMetadata())

    platformStatic
    public fun writeMetadataFiles(moduleName: String, moduleDescriptor: ModuleDescriptor, outputDir: File) {
        val contentMap = moduleDescriptor.toContentMap()
        val outDir = File(outputDir, moduleName)
        writeFiles(contentMap, outDir)
    }

    fun serializePackage(module: ModuleDescriptor, fqName: FqName, writeFun: (String, ByteArrayOutputStream) -> Unit) {
        val packageView = module.getPackage(fqName) ?: error("No package resolved in $module")

        val skip: (DeclarationDescriptor) -> Boolean = { DescriptorUtils.getContainingModule(it) != module }

        val serializer = DescriptorSerializer.createTopLevel(BuiltInsSerializerExtension)

        val classifierDescriptors = DescriptorSerializer.sort(packageView.getMemberScope().getDescriptors(DescriptorKindFilter.CLASSIFIERS))

        ClassSerializationUtil.serializeClasses(classifierDescriptors, serializer, object : ClassSerializationUtil.Sink {
            override fun writeClass(classDescriptor: ClassDescriptor, classProto: ProtoBuf.Class) {
                val stream = ByteArrayOutputStream()
                classProto.writeTo(stream)
                writeFun(getFileName(classDescriptor), stream)
            }
        }, skip)

        val packageStream = ByteArrayOutputStream()
        val fragments = module.getPackageFragmentProvider().getPackageFragments(fqName)
        val packageProto = serializer.packageProto(fragments, skip).build() ?: error("Package fragments not serialized: $fragments")
        packageProto.writeTo(packageStream)
        writeFun(BuiltInsSerializationUtil.getPackageFilePath(fqName), packageStream)

        val nameStream = ByteArrayOutputStream()
        val strings = serializer.getStringTable()
        SerializationUtil.serializeStringTable(nameStream, strings.serializeSimpleNames(), strings.serializeQualifiedNames())
        writeFun(BuiltInsSerializationUtil.getStringTableFilePath(fqName), nameStream)
    }

    fun getFileName(classDescriptor: ClassDescriptor): String {
        return BuiltInsSerializationUtil.getClassMetadataPath(classDescriptor.classId)
    }

    private fun ModuleDescriptor.toContentMap(): Map<String, ByteArray> {
        val contentMap = hashMapOf<String, ByteArray>()

        DescriptorUtils.getPackagesFqNames(this).forEach {
            serializePackage(this, it) {
                fileName, stream -> contentMap[fileName] = stream.toByteArray()
            }
        }

        return contentMap
    }

    private fun ModuleDescriptor.toBinaryMetadata(): ByteArray =
            KotlinJavascriptSerializationUtil.contentMapToByteArray(this.toContentMap())

    private fun writeFiles(contentMap: Map<String, ByteArray>, outputDir: File) {
        contentMap.keySet()
                .filter { isPackageMetadataFile(it) }
                .filter { contentMap[it]!!.size() > 0 }
                .forEach {
                    FileUtil.writeToFile(File(outputDir, replacePackageFileName(it)), contentMap[it]!!)
                    val stringsFileName = replaceWithStringTableFileName(it)
                    FileUtil.writeToFile(File(outputDir, stringsFileName), contentMap[stringsFileName]!!)
                }
        contentMap.keySet()
                .filter { it.endsWith(CLASS_METADATA_FILE_EXTENSION) }
                .forEach {
                    FileUtil.writeToFile(File(outputDir, replaceClassFileName(it)), contentMap[it]!!)
                }
    }

    private fun replaceWithStringTableFileName(fileName: String): String =
            BuiltInsSerializationUtil.getStringTableFilePath(getPackageFqName(fileName))

    private fun replacePackageFileName(fileName: String): String {
        val fqName = PackageClassUtils.getPackageClassFqName(getPackageFqName(fileName))
        return packageFqNameToPath(fqName) + DOT_META_EXT
    }

    private fun isPackageMetadataFile(fileName: String): Boolean =
            BuiltInsSerializationUtil.getPackageFilePath(getPackageFqName(fileName)) == fileName

    private fun getPackageFqName(fileName: String): FqName = FqName(getPackageName(fileName))

    private fun replaceClassFileName(fileName: String): String = fileName.substringBeforeLast(CLASS_METADATA_FILE_EXTENSION) + DOT_META_EXT

    private fun getPackageName(filePath: String): String = filePath.substringBeforeLast('/').replace('/', '.')

    private fun packageFqNameToPath(fqName: FqName): String = fqName.asString().replace('.', '/')

    private fun getPackages(contentMap: Map<String, ByteArray>): List<String> =
            contentMap.keySet().filter { isPackageMetadataFile(it) }.map { getPackageName(it) }
}