package com.ombre.brain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ombre.brain.core.AssociationEngine
import com.ombre.brain.core.MemoryEngine
import com.ombre.brain.security.AccessGuard
import com.ombre.brain.sync.BackupManager
import com.ombre.brain.sync.OperitSyncManager
import com.ombre.brain.ui.memory.MemoryScreen
import com.ombre.brain.ui.settings.SettingsScreen
import com.ombre.brain.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val accessGuard = AccessGuard()
    private lateinit var memoryEngine: MemoryEngine
    private lateinit var backupManager: BackupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as OmbreBrainApp
        memoryEngine = MemoryEngine(
            memoryDao = app.database.memoryDao(),
            flashDao = app.database.flashDao(),
            habitDao = app.database.habitDao()
        )
        backupManager = BackupManager(this)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OmbreBrainAppContent(
                    accessGuard = accessGuard,
                    memoryEngine = memoryEngine,
                    backupManager = backupManager
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmbreBrainAppContent(
    accessGuard: AccessGuard,
    memoryEngine: MemoryEngine,
    backupManager: BackupManager
) {
    var currentScreen by remember { mutableStateOf("home") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect memories for display
    val memories by com.ombre.brain.OmbreBrainApp
        .instance
        .repository
        .allMemories
        .collectAsState(initial = emptyList())

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("🏠") },
                    label = { Text("首页") },
                    selected = currentScreen == "home",
                    onClick = { currentScreen = "home" }
                )
                NavigationBarItem(
                    icon = { Text("🧠") },
                    label = { Text("记忆") },
                    selected = currentScreen == "memory",
                    onClick = { currentScreen = "memory" }
                )
                NavigationBarItem(
                    icon = { Text("⚙️") },
                    label = { Text("设置") },
                    selected = currentScreen == "settings",
                    onClick = { currentScreen = "settings" }
                )
            }
        }
    ) { padding ->
        when (currentScreen) {
            "home" -> HomeScreen(
                memoryEngine = memoryEngine,
                modifier = Modifier.padding(padding)
            )
            "memory" -> MemoryScreen(
                accessGuard = accessGuard,
                memories = memories,
                onBack = { currentScreen = "home" }
            )
            "settings" -> SettingsScreen(
                memoryEngine = memoryEngine,
                onBack = { currentScreen = "home" },
                onExport = {
                    scope.launch {
                        try {
                            val path = backupManager.export("default_password")
                            snackbarHostState.showSnackbar("✅ 备份已导出到: $path")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("❌ 导出失败: ${e.message}")
                        }
                    }
                },
                onImport = {
                    scope.launch {
                        snackbarHostState.showSnackbar("请把 .ombre 文件放到设备上后点击导入")
                        // TODO: 文件选择器
                    }
                },
                onSyncToOperit = {
                    scope.launch {
                        try {
                            val bridge = OperitSyncManager.FileBridgeApi(OmbreBrainApp.instance)
                            val syncMgr = OperitSyncManager(OmbreBrainApp.instance.database.memoryDao())
                            syncMgr.setApi(bridge)
                            val count = syncMgr.syncToOperit()
                            val pending = bridge.getPendingSyncCount()
                            snackbarHostState.showSnackbar(
                                "✅ 已导出 $count 条记忆到同步文件\n（告诉AI：来搬记忆啦）"
                            )
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("❌ 同步失败: ${e.message}")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    memoryEngine: MemoryEngine,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf(memoryEngine.BrainStats(0, 0, 0, 0, 0)) }

    LaunchedEffect(Unit) {
        stats = memoryEngine.getStats()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "🧠 Ombre Brain",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.weight(1f))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("系统已就绪", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text("总记忆: ${stats.totalMemories}")
                Text("活跃: ${stats.activeMemories} | 沉睡: ${stats.dormantMemories}")
                Text("固化习惯: ${stats.habits} | 未读闪现: ${stats.unreadFlashes}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击下方「记忆」查看我的记忆库 🔒\n点击「设置」进行备份和同步",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.weight(2f))
    }
}