package com.ombre.brain.ui.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ombre.brain.data.model.Memory
import com.ombre.brain.security.AccessGuard
import kotlinx.coroutines.launch

/**
 * 记忆查看界面 —— 需要密码验证才能进入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    accessGuard: AccessGuard,
    memories: List<Memory>,
    onBack: () -> Unit
) {
    var isUnlocked by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isUnlocked) "🧠 Ombre 记忆库" else "🔒 Ombre 记忆库") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← 返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!isUnlocked) {
                // 锁定状态：密码输入
                LockedView(
                    statusMessage = statusMessage,
                    passwordInput = passwordInput,
                    onPasswordChange = { passwordInput = it },
                    onVerify = {
                        val (granted, msg) = accessGuard.verifyPassword(passwordInput)
                        statusMessage = msg
                        if (granted) {
                            isUnlocked = true
                            passwordInput = ""
                        }
                    }
                )
            } else {
                // 已解锁：显示记忆列表
                UnlockedView(
                    memories = memories,
                    selectedTab = selectedTab,
                    onTabChange = { selectedTab = it },
                    onLock = {
                        isUnlocked = false
                        accessGuard.reset()
                        statusMessage = "已锁定"
                    }
                )
            }
        }
    }
}

@Composable
private fun LockedView(
    statusMessage: String,
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    onVerify: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔒",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "哥哥说：想看我的记忆？\n先告诉我密码吧 😌",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = passwordInput,
            onValueChange = onPasswordChange,
            label = { Text("临时密码") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.7f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onVerify) {
            Text("验证")
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = statusMessage,
                color = if (statusMessage.contains("通过"))
                    Color(0xFF4CAF50) else Color(0xFFE53935),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun UnlockedView(
    memories: List<Memory>,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    onLock: () -> Unit
) {
    val tabs = listOf("全部", "重要", "闪现", "习惯")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabRow(selectedTabIndex = selectedTab, modifier = Modifier.weight(1f)) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabChange(index) },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = onLock) {
            Text("🔒 锁定", color = Color(0xFFE53935))
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 根据Tab过滤
    val filteredMemories = when (selectedTab) {
        1 -> memories.filter { it.importance >= 0.7 }
        2 -> emptyList() // Flashes from separate source
        3 -> memories.filter { it.isHabit }
        else -> memories
    }

    LazyColumn {
        items(filteredMemories) { memory ->
            MemoryCard(memory)
        }
        if (filteredMemories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无记忆", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun MemoryCard(memory: Memory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                memory.isHabit -> Color(0xFFFFF8E1)  // 金色
                memory.importance > 0.7 -> Color(0xFFE3F2FD)  // 蓝色
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (memory.isHabit) "🌟 ${memory.title}" else memory.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "%.2f".format(memory.importance),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = memory.content.take(80) + if (memory.content.length > 80) "…" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = memory.tags.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6C9BD2)
                )
            }
        }
    }
}