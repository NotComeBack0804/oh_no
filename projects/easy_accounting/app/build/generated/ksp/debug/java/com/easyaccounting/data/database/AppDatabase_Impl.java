package com.easyaccounting.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.easyaccounting.data.dao.AccountDao;
import com.easyaccounting.data.dao.AccountDao_Impl;
import com.easyaccounting.data.dao.BillDao;
import com.easyaccounting.data.dao.BillDao_Impl;
import com.easyaccounting.data.dao.CategoryDao;
import com.easyaccounting.data.dao.CategoryDao_Impl;
import com.easyaccounting.data.dao.IncomeDao;
import com.easyaccounting.data.dao.IncomeDao_Impl;
import com.easyaccounting.data.dao.PendingRecordDao;
import com.easyaccounting.data.dao.PendingRecordDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile BillDao _billDao;

  private volatile IncomeDao _incomeDao;

  private volatile CategoryDao _categoryDao;

  private volatile AccountDao _accountDao;

  private volatile PendingRecordDao _pendingRecordDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `bills` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `categoryId` INTEGER, `date` INTEGER NOT NULL, `remark` TEXT, `accountId` INTEGER, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bills_categoryId` ON `bills` (`categoryId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bills_accountId` ON `bills` (`accountId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bills_date` ON `bills` (`date`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bills_remark` ON `bills` (`remark`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `incomes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `source` TEXT NOT NULL, `date` INTEGER NOT NULL, `remark` TEXT, `accountId` INTEGER, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_incomes_accountId` ON `incomes` (`accountId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_incomes_date` ON `incomes` (`date`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_incomes_remark` ON `incomes` (`remark`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, `type` TEXT NOT NULL, `isCustom` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `balance` REAL NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pending_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL NOT NULL, `source` TEXT NOT NULL, `date` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `status` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'bcc4dc63269f7d698dafd89080be2410')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `bills`");
        db.execSQL("DROP TABLE IF EXISTS `incomes`");
        db.execSQL("DROP TABLE IF EXISTS `categories`");
        db.execSQL("DROP TABLE IF EXISTS `accounts`");
        db.execSQL("DROP TABLE IF EXISTS `pending_records`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsBills = new HashMap<String, TableInfo.Column>(7);
        _columnsBills.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBills.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBills.put("categoryId", new TableInfo.Column("categoryId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBills.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBills.put("remark", new TableInfo.Column("remark", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBills.put("accountId", new TableInfo.Column("accountId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBills.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBills = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysBills.add(new TableInfo.ForeignKey("categories", "SET NULL", "NO ACTION", Arrays.asList("categoryId"), Arrays.asList("id")));
        _foreignKeysBills.add(new TableInfo.ForeignKey("accounts", "SET NULL", "NO ACTION", Arrays.asList("accountId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesBills = new HashSet<TableInfo.Index>(4);
        _indicesBills.add(new TableInfo.Index("index_bills_categoryId", false, Arrays.asList("categoryId"), Arrays.asList("ASC")));
        _indicesBills.add(new TableInfo.Index("index_bills_accountId", false, Arrays.asList("accountId"), Arrays.asList("ASC")));
        _indicesBills.add(new TableInfo.Index("index_bills_date", false, Arrays.asList("date"), Arrays.asList("ASC")));
        _indicesBills.add(new TableInfo.Index("index_bills_remark", false, Arrays.asList("remark"), Arrays.asList("ASC")));
        final TableInfo _infoBills = new TableInfo("bills", _columnsBills, _foreignKeysBills, _indicesBills);
        final TableInfo _existingBills = TableInfo.read(db, "bills");
        if (!_infoBills.equals(_existingBills)) {
          return new RoomOpenHelper.ValidationResult(false, "bills(com.easyaccounting.data.entity.Bill).\n"
                  + " Expected:\n" + _infoBills + "\n"
                  + " Found:\n" + _existingBills);
        }
        final HashMap<String, TableInfo.Column> _columnsIncomes = new HashMap<String, TableInfo.Column>(7);
        _columnsIncomes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("source", new TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("remark", new TableInfo.Column("remark", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("accountId", new TableInfo.Column("accountId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncomes.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysIncomes = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysIncomes.add(new TableInfo.ForeignKey("accounts", "SET NULL", "NO ACTION", Arrays.asList("accountId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesIncomes = new HashSet<TableInfo.Index>(3);
        _indicesIncomes.add(new TableInfo.Index("index_incomes_accountId", false, Arrays.asList("accountId"), Arrays.asList("ASC")));
        _indicesIncomes.add(new TableInfo.Index("index_incomes_date", false, Arrays.asList("date"), Arrays.asList("ASC")));
        _indicesIncomes.add(new TableInfo.Index("index_incomes_remark", false, Arrays.asList("remark"), Arrays.asList("ASC")));
        final TableInfo _infoIncomes = new TableInfo("incomes", _columnsIncomes, _foreignKeysIncomes, _indicesIncomes);
        final TableInfo _existingIncomes = TableInfo.read(db, "incomes");
        if (!_infoIncomes.equals(_existingIncomes)) {
          return new RoomOpenHelper.ValidationResult(false, "incomes(com.easyaccounting.data.entity.Income).\n"
                  + " Expected:\n" + _infoIncomes + "\n"
                  + " Found:\n" + _existingIncomes);
        }
        final HashMap<String, TableInfo.Column> _columnsCategories = new HashMap<String, TableInfo.Column>(5);
        _columnsCategories.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("icon", new TableInfo.Column("icon", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("isCustom", new TableInfo.Column("isCustom", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategories = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCategories = new TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories);
        final TableInfo _existingCategories = TableInfo.read(db, "categories");
        if (!_infoCategories.equals(_existingCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "categories(com.easyaccounting.data.entity.Category).\n"
                  + " Expected:\n" + _infoCategories + "\n"
                  + " Found:\n" + _existingCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsAccounts = new HashMap<String, TableInfo.Column>(4);
        _columnsAccounts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("balance", new TableInfo.Column("balance", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAccounts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAccounts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAccounts = new TableInfo("accounts", _columnsAccounts, _foreignKeysAccounts, _indicesAccounts);
        final TableInfo _existingAccounts = TableInfo.read(db, "accounts");
        if (!_infoAccounts.equals(_existingAccounts)) {
          return new RoomOpenHelper.ValidationResult(false, "accounts(com.easyaccounting.data.entity.Account).\n"
                  + " Expected:\n" + _infoAccounts + "\n"
                  + " Found:\n" + _existingAccounts);
        }
        final HashMap<String, TableInfo.Column> _columnsPendingRecords = new HashMap<String, TableInfo.Column>(6);
        _columnsPendingRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRecords.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRecords.put("source", new TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRecords.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRecords.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingRecords.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPendingRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPendingRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPendingRecords = new TableInfo("pending_records", _columnsPendingRecords, _foreignKeysPendingRecords, _indicesPendingRecords);
        final TableInfo _existingPendingRecords = TableInfo.read(db, "pending_records");
        if (!_infoPendingRecords.equals(_existingPendingRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "pending_records(com.easyaccounting.data.entity.PendingRecord).\n"
                  + " Expected:\n" + _infoPendingRecords + "\n"
                  + " Found:\n" + _existingPendingRecords);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "bcc4dc63269f7d698dafd89080be2410", "94d87baf2aa24807e4f3aa4ca63516cf");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "bills","incomes","categories","accounts","pending_records");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `bills`");
      _db.execSQL("DELETE FROM `incomes`");
      _db.execSQL("DELETE FROM `categories`");
      _db.execSQL("DELETE FROM `accounts`");
      _db.execSQL("DELETE FROM `pending_records`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(BillDao.class, BillDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(IncomeDao.class, IncomeDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CategoryDao.class, CategoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AccountDao.class, AccountDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PendingRecordDao.class, PendingRecordDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public BillDao billDao() {
    if (_billDao != null) {
      return _billDao;
    } else {
      synchronized(this) {
        if(_billDao == null) {
          _billDao = new BillDao_Impl(this);
        }
        return _billDao;
      }
    }
  }

  @Override
  public IncomeDao incomeDao() {
    if (_incomeDao != null) {
      return _incomeDao;
    } else {
      synchronized(this) {
        if(_incomeDao == null) {
          _incomeDao = new IncomeDao_Impl(this);
        }
        return _incomeDao;
      }
    }
  }

  @Override
  public CategoryDao categoryDao() {
    if (_categoryDao != null) {
      return _categoryDao;
    } else {
      synchronized(this) {
        if(_categoryDao == null) {
          _categoryDao = new CategoryDao_Impl(this);
        }
        return _categoryDao;
      }
    }
  }

  @Override
  public AccountDao accountDao() {
    if (_accountDao != null) {
      return _accountDao;
    } else {
      synchronized(this) {
        if(_accountDao == null) {
          _accountDao = new AccountDao_Impl(this);
        }
        return _accountDao;
      }
    }
  }

  @Override
  public PendingRecordDao pendingRecordDao() {
    if (_pendingRecordDao != null) {
      return _pendingRecordDao;
    } else {
      synchronized(this) {
        if(_pendingRecordDao == null) {
          _pendingRecordDao = new PendingRecordDao_Impl(this);
        }
        return _pendingRecordDao;
      }
    }
  }
}
