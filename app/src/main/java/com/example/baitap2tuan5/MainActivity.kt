package com.example.baitap2tuan5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    // Dữ liệu mẫu khởi tạo 1 lần
    private val library = Library().apply {
        addStudent(Student("SV01", "Nguyen Van A"))
        addStudent(Student("SV02", "Tran Thi B"))
        addItem(Book("S01", "Sách 01", "Tác giả 1"))
        addItem(Book("S02", "Sách 02", "Tác giả 2"))
        // [TÍNH ĐA HÌNH] List<LibraryItem> chứa cả Book lẫn Magazine
        addItem(Magazine("TC01", "Tạp chí 01", 5))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { LibraryApp(library) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryApp(library: Library) {
    var tab by remember { mutableIntStateOf(0) }
    var studentIndex by remember { mutableIntStateOf(0) }
    var tick by remember { mutableIntStateOf(0) } // đổi giá trị để ép UI vẽ lại
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }

    @Suppress("UNUSED_EXPRESSION") tick // đọc tick để Compose theo dõi

    val currentStudent = library.students[studentIndex]

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Home, null) }, label = { Text("Quản lý") })
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) }, label = { Text("DS Sách") })
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Person, null) }, label = { Text("Sinh viên") })
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                "Hệ thống\nQuản lý Thư viện",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(24.dp))

            when (tab) {
                // ---------------- Tab 0: Quản lý (giống ảnh đề bài) ----------------
                0 -> {
                    Text("Sinh viên", fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = currentStudent.name,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            // đổi sang sinh viên kế tiếp
                            studentIndex = (studentIndex + 1) % library.students.size
                        }) { Text("Thay đổi") }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Danh sách sách", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(library.items) { item ->
                            // Tích = đang được sinh viên HIỆN TẠI mượn
                            val checked = item.getBorrowerName() == currentStudent.name
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(24.dp))
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { want ->
                                        if (want) library.borrowItem(item, currentStudent)
                                        else if (checked) library.returnItem(item, currentStudent)
                                        tick++
                                    }
                                )
                                // [TÍNH ĐA HÌNH] getInfo() hiển thị khác nhau tùy Book/Magazine
                                Text(item.getInfo(), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) { Text("Thêm") }
                }

                // ---------------- Tab 1: Danh sách sách ----------------
                1 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(library.showAllInfo()) { info ->
                        Card(Modifier.fillMaxWidth()) {
                            Text(info, Modifier.padding(12.dp))
                        }
                    }
                }

                // ---------------- Tab 2: Danh sách sinh viên ----------------
                2 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(library.students) { s ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                // [TÍNH ĐA HÌNH] getRole() là hàm override từ Person
                                Text("${s.id} - ${s.name} (${s.getRole()})", fontWeight = FontWeight.Bold)
                                Text("Đang mượn: ${s.borrowed.size} cuốn")
                            }
                        }
                    }
                }
            }
        }
    }

    // Hộp thoại thêm sách mới
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Thêm sách") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Tên sách") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) {
                        val id = "S%02d".format(library.items.size + 1)
                        library.addItem(Book(id, newTitle.trim(), "Chưa rõ"))
                        newTitle = ""
                        tick++
                    }
                    showAddDialog = false
                }) { Text("Thêm") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Hủy") }
            }
        )
    }
}