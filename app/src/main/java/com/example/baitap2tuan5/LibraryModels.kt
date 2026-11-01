package com.example.baitap2tuan5



// =====================================================================
// TỪ KHÓA TÌM KIẾM (Ctrl+Shift+F trong Android Studio):
//   [TÍNH TRỪU TƯỢNG]  -> Abstraction
//   [TÍNH ĐÓNG GÓI]    -> Encapsulation
//   [TÍNH KẾ THỪA]     -> Inheritance
//   [TÍNH ĐA HÌNH]     -> Polymorphism
// =====================================================================

// ---------------------------------------------------------------------
// [TÍNH TRỪU TƯỢNG] (Abstraction) - interface
// Chỉ khai báo "có thể mượn / trả", không nói cách làm cụ thể.
// ---------------------------------------------------------------------
interface Borrowable {
    fun borrow(student: Student): Boolean
    fun giveBack()
}

// ---------------------------------------------------------------------
// [TÍNH TRỪU TƯỢNG] (Abstraction) - abstract class
// LibraryItem là khái niệm chung, không tạo trực tiếp đối tượng được.
// Có hàm abstract getInfo() bắt lớp con tự hiện thực.
// ---------------------------------------------------------------------
abstract class LibraryItem(
    val id: String,
    title: String
) : Borrowable {

    // [TÍNH ĐÓNG GÓI] (Encapsulation)
    // title: đọc được từ ngoài, nhưng chỉ sửa được qua rename() (private set)
    var title: String = title
        private set

    // [TÍNH ĐÓNG GÓI] borrowedBy là private, bên ngoài không truy cập trực tiếp
    private var borrowedBy: Student? = null

    // [TÍNH ĐÓNG GÓI] chỉ cung cấp các hàm/thuộc tính "đọc" an toàn
    val isBorrowed: Boolean get() = borrowedBy != null
    fun getBorrowerName(): String? = borrowedBy?.name

    // [TÍNH ĐÓNG GÓI] kiểm tra dữ liệu hợp lệ trước khi thay đổi
    fun rename(newTitle: String) {
        if (newTitle.isNotBlank()) title = newTitle.trim()
    }

    override fun borrow(student: Student): Boolean {
        if (borrowedBy != null) return false // đã có người mượn
        borrowedBy = student
        return true
    }

    override fun giveBack() {
        borrowedBy = null
    }

    // [TÍNH TRỪU TƯỢNG] hàm abstract - lớp con bắt buộc override
    abstract fun getInfo(): String
}

// ---------------------------------------------------------------------
// [TÍNH KẾ THỪA] (Inheritance)
// Book kế thừa LibraryItem: dùng lại id, title, borrow(), giveBack()...
// ---------------------------------------------------------------------
class Book(
    id: String,
    title: String,
    val author: String
) : LibraryItem(id, title) {

    // [TÍNH ĐA HÌNH] (Polymorphism) - override: cùng tên hàm getInfo()
    // nhưng Book hiển thị theo cách riêng.
    override fun getInfo(): String =
        "Sách [$id] - $title - Tác giả: $author" +
                (getBorrowerName()?.let { " (đang mượn: $it)" } ?: "")
}

// [TÍNH KẾ THỪA] Magazine cũng kế thừa LibraryItem
class Magazine(
    id: String,
    title: String,
    val issue: Int
) : LibraryItem(id, title) {

    // [TÍNH ĐA HÌNH] cùng getInfo() nhưng kết quả khác Book
    override fun getInfo(): String =
        "Tạp chí [$id] - $title - Số: $issue" +
                (getBorrowerName()?.let { " (đang mượn: $it)" } ?: "")
}

// ---------------------------------------------------------------------
// [TÍNH TRỪU TƯỢNG] Person là lớp trừu tượng cho con người trong hệ thống
// ---------------------------------------------------------------------
abstract class Person(val id: String, name: String) {

    // [TÍNH ĐÓNG GÓI] name có setter kiểm tra hợp lệ
    var name: String = name
        set(value) {
            if (value.isNotBlank()) field = value.trim()
        }

    abstract fun getRole(): String
}

// [TÍNH KẾ THỪA] Student kế thừa Person
class Student(id: String, name: String) : Person(id, name) {

    // [TÍNH ĐÓNG GÓI] danh sách mượn là private, chỉ cho xem bản sao (read-only)
    private val borrowedItems = mutableListOf<LibraryItem>()
    val borrowed: List<LibraryItem> get() = borrowedItems.toList()

    fun addBorrowed(item: LibraryItem) { borrowedItems.add(item) }
    fun removeBorrowed(item: LibraryItem) { borrowedItems.remove(item) }

    // [TÍNH ĐA HÌNH] override hàm abstract của Person
    override fun getRole(): String = "Sinh viên"
}

// ---------------------------------------------------------------------
// Lớp quản lý thư viện
// ---------------------------------------------------------------------
class Library {

    // [TÍNH ĐÓNG GÓI] danh sách thật là private, bên ngoài chỉ nhận bản sao
    private val _items = mutableListOf<LibraryItem>()
    private val _students = mutableListOf<Student>()

    val items: List<LibraryItem> get() = _items.toList()
    val students: List<Student> get() = _students.toList()

    fun addItem(item: LibraryItem) { _items.add(item) }
    fun addStudent(student: Student) { _students.add(student) }

    // [TÍNH ĐA HÌNH] tham số là LibraryItem: truyền Book hay Magazine đều được,
    // gọi borrow() sẽ chạy đúng theo kiểu thực tế của đối tượng.
    fun borrowItem(item: LibraryItem, student: Student): Boolean {
        val ok = item.borrow(student)
        if (ok) student.addBorrowed(item)
        return ok
    }

    fun returnItem(item: LibraryItem, student: Student) {
        item.giveBack()
        student.removeBorrowed(item)
    }

    // [TÍNH ĐA HÌNH] duyệt list LibraryItem, mỗi phần tử gọi getInfo() riêng
    fun showAllInfo(): List<String> = _items.map { it.getInfo() }
}