# 简易记账 APP - API 文档

> 本应用为原生 Android 应用，无后端服务。以下为内部 Room Database API 说明。

## 数据库概览

- **数据库名称**: `easy_accounting.db`
- **加密**: SQLCipher 加密存储
- **版本**: 1

---

## 表结构

### Bill (支出表)

| 字段 | 类型 | 描述 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| amount | Double | 金额 |
| categoryId | Long (nullable) | 分类ID |
| date | Long | 日期时间戳 |
| remark | String (nullable) | 备注 |
| accountId | Long (nullable) | 账户ID |
| createdAt | Long | 创建时间戳 |

### Income (收入表)

| 字段 | 类型 | 描述 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| amount | Double | 金额 |
| source | String | 收入来源 |
| date | Long | 日期时间戳 |
| remark | String (nullable) | 备注 |
| accountId | Long (nullable) | 账户ID |
| createdAt | Long | 创建时间戳 |

### Category (分类表)

| 字段 | 类型 | 描述 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| name | String | 分类名称 |
| icon | String | 图标标识 |
| type | CategoryType | 类型 (EXPENSE/INCOME) |
| isCustom | Boolean | 是否自定义 |

### Account (账户表)

| 字段 | 类型 | 描述 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| name | String | 账户名称 |
| type | AccountType | 账户类型 |
| balance | Double | 余额 |

---

## Room DAO 接口

### BillDao

```kotlin
// 插入支出
suspend fun insert(bill: Bill): Long

// 更新支出
suspend fun update(bill: Bill)

// 删除支出
suspend fun delete(bill: Bill)

// 获取所有支出（按日期倒序）
fun getAllBills(): Flow<List<Bill>>

// 按ID获取
suspend fun getBillById(id: Long): Bill?

// 按日期范围查询
fun getBillsByDateRange(startDate: Long, endDate: Long): Flow<List<Bill>>

// 按分类查询
fun getBillsByCategory(categoryId: Long): Flow<List<Bill>>

// 搜索（按备注）
fun searchBills(keyword: String): Flow<List<Bill>>

// 获取指定日期范围总支出
fun getTotalExpenseByDateRange(startDate: Long, endDate: Long): Flow<Double?>

// 获取金额最高的N条支出
fun getTopBillsByAmount(startDate: Long, endDate: Long, limit: Int): Flow<List<Bill>>
```

### IncomeDao

```kotlin
// 插入收入
suspend fun insert(income: Income): Long

// 更新收入
suspend fun update(income: Income)

// 删除收入
suspend fun delete(income: Income)

// 获取所有收入（按日期倒序）
fun getAllIncomes(): Flow<List<Income>>

// 按ID获取
suspend fun getIncomeById(id: Long): Income?

// 按日期范围查询
fun getIncomesByDateRange(startDate: Long, endDate: Long): Flow<List<Income>>

// 搜索（按备注）
fun searchIncomes(keyword: String): Flow<List<Income>>

// 获取指定日期范围总收入
fun getTotalIncomeByDateRange(startDate: Long, endDate: Long): Flow<Double?>

// 获取金额最高的N条收入
fun getTopIncomesByAmount(startDate: Long, endDate: Long, limit: Int): Flow<List<Income>>
```

### CategoryDao

```kotlin
// 插入分类
suspend fun insert(category: Category): Long

// 批量插入
suspend fun insertAll(categories: List<Category>)

// 更新分类
suspend fun update(category: Category)

// 删除分类
suspend fun delete(category: Category)

// 获取所有分类
fun getAllCategories(): Flow<List<Category>>

// 按类型获取
fun getCategoriesByType(type: CategoryType): Flow<List<Category>>

// 按ID获取
suspend fun getCategoryById(id: Long): Category?

// 获取自定义分类
fun getCustomCategories(): Flow<List<Category>>
```

### AccountDao

```kotlin
// 插入账户
suspend fun insert(account: Account): Long

// 批量插入
suspend fun insertAll(accounts: List<Category>)

// 更新账户
suspend fun update(account: Account)

// 删除账户
suspend fun delete(account: Account)

// 获取所有账户
fun getAllAccounts(): Flow<List<Account>>

// 按ID获取
suspend fun getAccountById(id: Long): Account?

// 更新余额
suspend fun updateBalance(accountId: Long, amount: Double)
```

---

## 枚举类型

### CategoryType

```kotlin
enum class CategoryType {
    EXPENSE, // 支出
    INCOME   // 收入
}
```

### AccountType

```kotlin
enum class AccountType {
    ALIPAY,    // 支付宝
    WECHAT,    // 微信
    BANK_CARD, // 银行卡
    CASH       // 现金
}
```

---

## 工具类

### DateUtils

```kotlin
// 获取指定日期的开始时间戳
fun getStartOfDay(timeMillis: Long): Long

// 获取指定日期的结束时间戳
fun getEndOfDay(timeMillis: Long): Long

// 获取指定月份的起止时间戳
fun getStartOfMonth(year: Int, month: Int): Long
fun getEndOfMonth(year: Int, month: Int): Long

// 获取当月起止时间戳
fun getTodayStart(): Long
fun getTodayEnd(): Long

// 格式化
fun formatDate(timeMillis: Long): String  // "yyyy-MM-dd"
fun formatMonth(year: Int, month: Int): String  // "yyyy年MM月"
```

### FormatUtils

```kotlin
// 格式化金额
fun formatAmount(amount: Double): String  // "#,##0.00"

// 解析金额
fun parseAmount(amountStr: String): Double
```

---

## Intent Extra

### AddRecordActivity

无特殊 Intent Extra，从 MainActivity 启动。

### RecordDetailActivity

```kotlin
// 支出详情
const val EXTRA_BILL_ID = "extra_bill_id"

// 收入详情
const val EXTRA_INCOME_ID = "extra_income_id"
```

---

## 数据流向 (MVVM)

```
UI Layer (Activities/ViewModels)
        ↓ collect Flow
Repository Layer
        ↓ call suspend functions
DAO Layer (Room)
        ↓
SQLite Database (Encrypted)
```

---

## 默认数据

### 支出分类 (初始7个)
- 餐饮 (ic_food)
- 交通 (ic_transport)
- 购物 (ic_shopping)
- 娱乐 (ic_entertainment)
- 医疗 (ic_medical)
- 教育 (ic_education)
- 其他 (ic_other)

### 收入分类 (初始4个)
- 工资 (ic_salary)
- 奖金 (ic_bonus)
- 投资收益 (ic_investment)
- 其他 (ic_other)

### 默认账户
- 支付宝 (ALIPAY)
- 微信 (WECHAT)
- 银行卡 (BANK_CARD)
- 现金 (CASH)
