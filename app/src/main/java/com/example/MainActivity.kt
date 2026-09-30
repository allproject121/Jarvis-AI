package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AutomationsScreen
import com.example.ui.ChatScreen
import com.example.ui.ImageStudioScreen
import com.example.ui.JarvisMainScreen
import com.example.ui.JarvisTab
import com.example.ui.MainViewModel
import com.example.ui.SystemHudScreen
import com.example.ui.theme.ColorBackgroundDark
import com.example.ui.theme.ColorPrimaryCyan
import com.example.ui.theme.ColorSurfaceDark
import com.example.ui.theme.ColorTextMuted
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

                // BackHandler returns to HUD if inside sub-screens
                BackHandler(enabled = currentTab != JarvisTab.HUD_VOICE) {
                    viewModel.selectTab(JarvisTab.HUD_VOICE)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = JarvisBackground,
                    bottomBar = {
                        JarvisBottomNav(
                            currentTab = currentTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    when (currentTab) {
                        JarvisTab.HUD_VOICE -> JarvisMainScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        JarvisTab.AI_CHAT -> ChatScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        JarvisTab.AUTOMATIONS -> AutomationsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        JarvisTab.IMAGE_STUDIO -> ImageStudioScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                        JarvisTab.SYSTEM_DIAGNOSTICS -> SystemHudScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JarvisBottomNav(
    currentTab: JarvisTab,
    onTabSelected: (JarvisTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("jarvis_bottom_nav"),
        containerColor = ColorBackgroundDark,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(JarvisTab.HUD_VOICE, "Voice", Icons.Default.Mic),
            Triple(JarvisTab.AI_CHAT, "AI Chat", Icons.Default.ChatBubbleOutline),
            Triple(JarvisTab.AUTOMATIONS, "Routines", Icons.Default.AutoMode),
            Triple(JarvisTab.IMAGE_STUDIO, "Visuals", Icons.Default.Image),
            Triple(JarvisTab.SYSTEM_DIAGNOSTICS, "System", Icons.Default.SettingsSuggest)
        )

        items.forEach { (tab, label, icon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ColorPrimaryCyan,
                    selectedTextColor = ColorPrimaryCyan,
                    indicatorColor = ColorSurfaceDark,
                    unselectedIconColor = ColorTextMuted,
                    unselectedTextColor = ColorTextMuted
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
