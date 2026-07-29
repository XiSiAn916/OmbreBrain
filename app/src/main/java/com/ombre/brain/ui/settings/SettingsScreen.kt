package com.ombre.brain.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ombre.brain.core.MemoryEngine
import kotlinx.coroutines.launch

/**
 * 设置页面 —— 备份/同步/统计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    memoryEngine: MemoryEngine,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onSyncToOperit: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf(MemoryEngine.BrainStats(0, 0, 0, 0, 0)) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        stats = memoryEngine.getStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Ombre 设置") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← 返回") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 统计信息
            SectionTitle("📊 统计")
            StatsCard(stats)

            Spacer(modifier = Modifier.height(24.dp))

            // 备份与同步
            SectionTitle("💾 备份与同步")
            SettingsButton("📤 导出 .ombre 备份", "加密导出所有记忆", onClick = onExport)
            SettingsButton("📥 从 .ombre 恢复", "导入备份文件恢复记忆", onClick = onImport)
            SettingsButton("🔄 同步到 Operit 记忆", "将重要记忆同步到 Operit", onClick = onSyncToOperit)

            Spacer(modifier = Modifier.height(24.dp))

            // 记忆参数
            SectionTitle("🧠 管理")
            SettingsButton("🗑️ 清理沉睡记忆", "删除所有沉睡记忆（不可恢复）", Color(0xFFE53935)) {
                // Confirm dialog would be better
            }
            SettingsButton("⚠️ 重置所有记忆", "清空整个记忆库", Color(0xFFD32F2F)) {
                // Confirm dialog
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 关于
            SectionTitle("ℹ️ 关于")
            Text(
                text = "Ombre Brain v0.1",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "仿人记忆系统 · 给 AI 用的本地记忆库",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun StatsCard(stats: MemoryEngine.BrainStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("总记忆", stats.totalMemories)
            StatItem("活跃", stats.activeMemories)
            StatItem("沉睡", stats.dormantMemories)
            StatItem("习惯", stats.habits)
            StatItem("闪现", stats.unreadFlashes)
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.headlineSmall.fontSize
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsButton(
    text: String,
    description: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}