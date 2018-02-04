package com.fractalwrench.json2kotlin

import com.google.gson.JsonParser
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.ByteArrayOutputStream
import java.io.File

fun String.standardiseNewline(): String {
    return this.replace("\r\n", "\n")
}

@RunWith(Parameterized::class)
class JsonConverterTest(val expectedFilename: String, val jsonFilename: String) {

    private val fileReader = ResourceFileReader()
    private val jsonConverter = KotlinJsonConverter(JsonParser())

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "File {0}")
        fun filenamePairs(): Collection<Array<String>> {
            val dir = File(JsonConverterTest::class.java.classLoader.getResource("valid").file)
//            val dir = File(resourcePath).parentFile
            val arrayList = ArrayList<Array<String>>()
            findTestCaseFiles(dir, dir, arrayList)
            return arrayList
        }
        /**
         * Recurses through the resources directory and add test case files to the collection
         */
        private fun findTestCaseFiles(baseDir: File, currentDir: File, testCases: MutableCollection<Array<String>>) {
            currentDir.listFiles().forEach {
                if (it.extension == "json") {
                    val name = it.nameWithoutExtension
                    val json = File(currentDir, "$name.json")
                    val kt = File(currentDir, "${name}Example.kt")

                    Assert.assertTrue("Expected to find test case file " + json, json.exists())
                    Assert.assertTrue("Expected to find test case file " + kt, kt.exists())
                    val element = arrayOf(normalisePath(kt, baseDir), normalisePath(json, baseDir))
                    testCases.add(element)

                } else if (it.isDirectory) {
                    findTestCaseFiles(baseDir, it, testCases)
                }
            }
        }

        private fun normalisePath(json: File, baseDir: File) = json.relativeTo(baseDir.parentFile).path
    }


    /**
     * Takes a JSON file and converts it into the equivalent Kotlin class, then compares to expected output.
     */
    @Test
    fun testJsonToKotlinConversion() {
        val json = fileReader.readContents(jsonFilename)
        val outputStream = ByteArrayOutputStream()
        val rootClassName = expectedFilename.replace(".kt", "").substringAfterLast(File.separator)
        jsonConverter.convert(json, outputStream, rootClassName)

        val generatedSource = String(outputStream.toByteArray()).standardiseNewline()
        val expectedContents = fileReader.readContents(expectedFilename).standardiseNewline()
        val msg = "Generated file doesn't match expected file \'$expectedFilename\'"
        Assert.assertEquals(msg, expectedContents, generatedSource)
    }
}