package com.easyaccounting.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.easyaccounting.data.entity.Bill;
import com.easyaccounting.data.entity.BillWithCategory;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BillDao_Impl implements BillDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Bill> __insertionAdapterOfBill;

  private final EntityDeletionOrUpdateAdapter<Bill> __deletionAdapterOfBill;

  private final EntityDeletionOrUpdateAdapter<Bill> __updateAdapterOfBill;

  public BillDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBill = new EntityInsertionAdapter<Bill>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `bills` (`id`,`amount`,`categoryId`,`date`,`remark`,`accountId`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Bill entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getAmount());
        if (entity.getCategoryId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getCategoryId());
        }
        statement.bindLong(4, entity.getDate());
        if (entity.getRemark() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRemark());
        }
        if (entity.getAccountId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getAccountId());
        }
        statement.bindLong(7, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfBill = new EntityDeletionOrUpdateAdapter<Bill>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `bills` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Bill entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfBill = new EntityDeletionOrUpdateAdapter<Bill>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `bills` SET `id` = ?,`amount` = ?,`categoryId` = ?,`date` = ?,`remark` = ?,`accountId` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Bill entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getAmount());
        if (entity.getCategoryId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getCategoryId());
        }
        statement.bindLong(4, entity.getDate());
        if (entity.getRemark() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRemark());
        }
        if (entity.getAccountId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getAccountId());
        }
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final Bill bill, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfBill.insertAndReturnId(bill);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Bill bill, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBill.handle(bill);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Bill bill, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBill.handle(bill);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Bill>> getAllBills() {
    final String _sql = "SELECT * FROM bills ORDER BY date DESC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<List<Bill>>() {
      @Override
      @NonNull
      public List<Bill> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Bill> _result = new ArrayList<Bill>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Bill _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Bill(_tmpId,_tmpAmount,_tmpCategoryId,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getBillById(final long id, final Continuation<? super Bill> $completion) {
    final String _sql = "SELECT * FROM bills WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Bill>() {
      @Override
      @Nullable
      public Bill call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Bill _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new Bill(_tmpId,_tmpAmount,_tmpCategoryId,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Bill> getBillByIdFlow(final long id) {
    final String _sql = "SELECT * FROM bills WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<Bill>() {
      @Override
      @Nullable
      public Bill call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Bill _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new Bill(_tmpId,_tmpAmount,_tmpCategoryId,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Bill>> getBillsByDateRange(final long startDate, final long endDate) {
    final String _sql = "SELECT * FROM bills WHERE date >= ? AND date <= ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<List<Bill>>() {
      @Override
      @NonNull
      public List<Bill> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Bill> _result = new ArrayList<Bill>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Bill _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Bill(_tmpId,_tmpAmount,_tmpCategoryId,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Bill>> getBillsByCategory(final long categoryId) {
    final String _sql = "SELECT * FROM bills WHERE categoryId = ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, categoryId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<List<Bill>>() {
      @Override
      @NonNull
      public List<Bill> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Bill> _result = new ArrayList<Bill>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Bill _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Bill(_tmpId,_tmpAmount,_tmpCategoryId,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Bill>> searchBills(final String keyword) {
    final String _sql = "SELECT * FROM bills WHERE remark LIKE '%' || ? || '%' ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, keyword);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<List<Bill>>() {
      @Override
      @NonNull
      public List<Bill> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Bill> _result = new ArrayList<Bill>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Bill _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Bill(_tmpId,_tmpAmount,_tmpCategoryId,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getBillsByDateRangeSync(final long startDate, final long endDate,
      final Continuation<? super List<Bill>> $completion) {
    final String _sql = "SELECT * FROM bills WHERE date >= ? AND date <= ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Bill>>() {
      @Override
      @NonNull
      public List<Bill> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Bill> _result = new ArrayList<Bill>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Bill _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Bill(_tmpId,_tmpAmount,_tmpCategoryId,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Double> getTotalExpenseByDateRange(final long startDate, final long endDate) {
    final String _sql = "SELECT SUM(amount) FROM bills WHERE date >= ? AND date <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Bill>> getTopBillsByAmount(final long startDate, final long endDate,
      final int limit) {
    final String _sql = "SELECT * FROM bills WHERE date >= ? AND date <= ? ORDER BY amount DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    _argIndex = 3;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills"}, new Callable<List<Bill>>() {
      @Override
      @NonNull
      public List<Bill> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Bill> _result = new ArrayList<Bill>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Bill _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new Bill(_tmpId,_tmpAmount,_tmpCategoryId,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BillWithCategory>> getBillsWithCategoryByDateRange(final long startDate,
      final long endDate) {
    final String _sql = "\n"
            + "        SELECT b.id, b.amount, b.categoryId, c.name AS categoryName,\n"
            + "               b.date, b.remark, b.accountId, b.createdAt\n"
            + "        FROM bills b\n"
            + "        LEFT JOIN categories c ON b.categoryId = c.id\n"
            + "        WHERE b.date >= ? AND b.date <= ?\n"
            + "        ORDER BY b.date DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills",
        "categories"}, new Callable<List<BillWithCategory>>() {
      @Override
      @NonNull
      public List<BillWithCategory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfAmount = 1;
          final int _cursorIndexOfCategoryId = 2;
          final int _cursorIndexOfCategoryName = 3;
          final int _cursorIndexOfDate = 4;
          final int _cursorIndexOfRemark = 5;
          final int _cursorIndexOfAccountId = 6;
          final int _cursorIndexOfCreatedAt = 7;
          final List<BillWithCategory> _result = new ArrayList<BillWithCategory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BillWithCategory _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final String _tmpCategoryName;
            if (_cursor.isNull(_cursorIndexOfCategoryName)) {
              _tmpCategoryName = null;
            } else {
              _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new BillWithCategory(_tmpId,_tmpAmount,_tmpCategoryId,_tmpCategoryName,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BillWithCategory>> searchBillsWithCategory(final String keyword) {
    final String _sql = "\n"
            + "        SELECT b.id, b.amount, b.categoryId, c.name AS categoryName,\n"
            + "               b.date, b.remark, b.accountId, b.createdAt\n"
            + "        FROM bills b\n"
            + "        LEFT JOIN categories c ON b.categoryId = c.id\n"
            + "        WHERE b.remark LIKE '%' || ? || '%'\n"
            + "        ORDER BY b.date DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, keyword);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills",
        "categories"}, new Callable<List<BillWithCategory>>() {
      @Override
      @NonNull
      public List<BillWithCategory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfAmount = 1;
          final int _cursorIndexOfCategoryId = 2;
          final int _cursorIndexOfCategoryName = 3;
          final int _cursorIndexOfDate = 4;
          final int _cursorIndexOfRemark = 5;
          final int _cursorIndexOfAccountId = 6;
          final int _cursorIndexOfCreatedAt = 7;
          final List<BillWithCategory> _result = new ArrayList<BillWithCategory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BillWithCategory _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final String _tmpCategoryName;
            if (_cursor.isNull(_cursorIndexOfCategoryName)) {
              _tmpCategoryName = null;
            } else {
              _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new BillWithCategory(_tmpId,_tmpAmount,_tmpCategoryId,_tmpCategoryName,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BillWithCategory>> getAllBillsWithCategory() {
    final String _sql = "\n"
            + "        SELECT b.id, b.amount, b.categoryId, c.name AS categoryName,\n"
            + "               b.date, b.remark, b.accountId, b.createdAt\n"
            + "        FROM bills b\n"
            + "        LEFT JOIN categories c ON b.categoryId = c.id\n"
            + "        ORDER BY b.date DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"bills",
        "categories"}, new Callable<List<BillWithCategory>>() {
      @Override
      @NonNull
      public List<BillWithCategory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfAmount = 1;
          final int _cursorIndexOfCategoryId = 2;
          final int _cursorIndexOfCategoryName = 3;
          final int _cursorIndexOfDate = 4;
          final int _cursorIndexOfRemark = 5;
          final int _cursorIndexOfAccountId = 6;
          final int _cursorIndexOfCreatedAt = 7;
          final List<BillWithCategory> _result = new ArrayList<BillWithCategory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BillWithCategory _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Long _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getLong(_cursorIndexOfCategoryId);
            }
            final String _tmpCategoryName;
            if (_cursor.isNull(_cursorIndexOfCategoryName)) {
              _tmpCategoryName = null;
            } else {
              _tmpCategoryName = _cursor.getString(_cursorIndexOfCategoryName);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpRemark;
            if (_cursor.isNull(_cursorIndexOfRemark)) {
              _tmpRemark = null;
            } else {
              _tmpRemark = _cursor.getString(_cursorIndexOfRemark);
            }
            final Long _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getLong(_cursorIndexOfAccountId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new BillWithCategory(_tmpId,_tmpAmount,_tmpCategoryId,_tmpCategoryName,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
