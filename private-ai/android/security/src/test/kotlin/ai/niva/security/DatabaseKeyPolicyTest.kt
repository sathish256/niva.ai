package ai.niva.security

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class DatabaseKeyPolicyTest {

    @Test
    fun `no hardcoded sqlcipher passphrase in android sources`() {
        var dir = File(System.getProperty("user.dir")!!)
        val androidRoot = generateSequence(dir) { it.parentFile }
            .firstOrNull { it.resolve("app").isDirectory && it.resolve("settings.gradle.kts").isFile }
            ?: error("android root not found from $dir")
        val forbidden = listOf("hardcoded-db-key", "passphrase = \"abcdef")
        androidRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { !it.path.contains("/build/") }
            .filter { !it.path.contains("/src/test/") }
            .forEach { file ->
                val text = file.readText()
                forbidden.forEach { needle ->
                    assertFalse("Forbidden pattern in ${file.path}", text.contains(needle))
                }
            }
    }
}
