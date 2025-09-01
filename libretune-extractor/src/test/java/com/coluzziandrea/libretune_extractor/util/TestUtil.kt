package com.coluzziandrea.libretune_extractor.util

import java.io.InputStreamReader


class TestUtil {
    companion object {
        // Helper function to read a file from the test/resources folder
        fun readFileFromResources(fileName: String): String {
            val inputStream = TestUtil.javaClass.classLoader?.getResourceAsStream(fileName)
            val reader = InputStreamReader(inputStream)
            return reader.readText()
        }
    }


}