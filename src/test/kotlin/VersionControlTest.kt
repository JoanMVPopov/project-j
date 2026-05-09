import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.ide.starter.config.ConfigurationStorage
import com.intellij.ide.starter.config.splitMode
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.junit5.hyphenateWithClass
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.GitHubProject
import com.intellij.ide.starter.runner.CurrentTestMethod
import com.intellij.ide.starter.runner.Starter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class VersionControlTest {
    @ParameterizedTest(name = "split-mode={0}")
    @ValueSource(booleans = [false, true])
    fun sampleTest(splitMode: Boolean) {
        ConfigurationStorage.splitMode(splitMode)

        // derive test name from the class name
        val testName = CurrentTestMethod.hyphenateWithClass()

        // set ide version explicitly to match dependencies
        // build version is 252.28539.54
        val ideVersion = "2025.2.6.2"

        val ide = IdeInfo(
            productCode = "IC",
            platformPrefix = "Idea",
            executableFileName = "idea",
            fullName = "IntelliJ IDEA Community Edition",
            version = ideVersion
        )

        val testContext = Starter
            .newContext(testName, TestCase(ide, GitHubProject.fromGithub(
                branchName = "main",
                repoRelativeUrl = "TeamPraxidike/CAIT.git",
                commitHash = "55f28ffb3dcc340679fa3eec4e87412b4446abce"
            )).useRelease(ideVersion))
            .prepareProjectCleanImport()

        testContext.runIdeWithDriver().useDriverAndCloseIde {
            ideFrame {
                assertNotEquals("ok", "not ok") { "Impossible condition" }
            }
        }
    }
}