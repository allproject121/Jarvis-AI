package com.example

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.example.ui.MainViewModel
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainViewModelTest {

    @Test
    fun testAndroidViewModelFactoryInstantiation() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        val vm = factory.create(MainViewModel::class.java)
        assertNotNull(vm)
        assertNotNull(vm.repository)
        assertNotNull(vm.commandRepository)
        assertNotNull(vm.patternRepository)
        assertNotNull(vm.entityRepository)
    }

    @Test
    fun testDirectApplicationConstructorReflection() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val constructor = MainViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(constructor)
        val vm = constructor.newInstance(app)
        assertNotNull(vm)
    }
}
