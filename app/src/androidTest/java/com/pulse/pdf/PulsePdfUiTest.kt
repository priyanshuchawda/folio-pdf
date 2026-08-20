package com.pulse.pdf

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.pulse.pdf.ui.LibraryActivity
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PulsePdfUiTest {

    private lateinit var device: UiDevice
    private lateinit var context: Context

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = ApplicationProvider.getApplicationContext()
        device.pressHome()
    }

    @Test
    fun openSampleAndTurnPages() {
        val launch = Intent(context, LibraryActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(launch)
        assertTrue(device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), TIMEOUT))

        // Tap Sample
        val sample = device.wait(Until.findObject(By.text("Sample")), TIMEOUT)
        assertTrue("Sample button missing", sample != null)
        sample!!.click()

        // Reader should show page indicator
        val page1 = device.wait(Until.findObject(By.textContains("/")), TIMEOUT)
        assertTrue("Page label missing", page1 != null)

        // Next page via button
        val next = device.wait(Until.findObject(By.text("Next")), TIMEOUT)
        assertTrue(next != null)
        next!!.click()
        Thread.sleep(800)

        // Swipe to next page
        device.swipe(
            device.displayWidth * 3 / 4,
            device.displayHeight / 2,
            device.displayWidth / 4,
            device.displayHeight / 2,
            20,
        )
        Thread.sleep(800)

        // Prev
        val prev = device.wait(Until.findObject(By.text("Prev")), TIMEOUT)
        prev?.click()
        Thread.sleep(500)

        assertTrue(device.findObject(By.pkg(PACKAGE)) != null)
    }

    @Test
    fun openViaFileProviderIntent() {
        val out = File(context.cacheDir, "sample.pdf")
        context.assets.open("sample.pdf").use { input ->
            out.outputStream().use { input.copyTo(it) }
        }
        val uri = FileProvider.getUriForFile(context, "$PACKAGE.files", out)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage(PACKAGE)
        }
        context.startActivity(intent)
        assertTrue(device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), TIMEOUT))
        assertTrue(device.wait(Until.findObject(By.textContains("/")), TIMEOUT) != null)
    }

    companion object {
        private const val PACKAGE = "com.pulse.pdf"
        private const val TIMEOUT = 8_000L
    }
}
