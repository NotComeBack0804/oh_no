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
import com.easyaccounting.data.entity.Income;
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
public final class IncomeDao_Impl implements IncomeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Income> __insertionAdapterOfIncome;

  private final EntityDeletionOrUpdateAdapter<Income> __deletionAdapterOfIncome;

  private final EntityDeletionOrUpdateAdapter<Income> __updateAdapterOfIncome;

  public IncomeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfIncome = new EntityInsertionAdapter<Income>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `incomes` (`id`,`amount`,`source`,`date`,`remark`,`accountId`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Income entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getAmount());
        statement.bindString(3, entity.getSource());
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
    this.__deletionAdapterOfIncome = new EntityDeletionOrUpdateAdapter<Income>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `incomes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Income entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfIncome = new EntityDeletionOrUpdateAdapter<Income>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `incomes` SET `id` = ?,`amount` = ?,`source` = ?,`date` = ?,`remark` = ?,`accountId` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Income entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getAmount());
        statement.bindString(3, entity.getSource());
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
  public Object insert(final Income income, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfIncome.insertAndReturnId(income);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Income income, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfIncome.handle(income);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Income income, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfIncome.handle(income);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Income>> getAllIncomes() {
    final String _sql = "SELECT * FROM incomes ORDER BY date DESC, createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"incomes"}, new Callable<List<Income>>() {
      @Override
      @NonNull
      public List<Income> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Income> _result = new ArrayList<Income>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Income _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
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
            _item = new Income(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
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
  public Object getIncomeById(final long id, final Continuation<? super Income> $completion) {
    final String _sql = "SELECT * FROM incomes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Income>() {
      @Override
      @Nullable
      public Income call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Income _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
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
            _result = new Income(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
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
  public Flow<Income> getIncomeByIdFlow(final long id) {
    final String _sql = "SELECT * FROM incomes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"incomes"}, new Callable<Income>() {
      @Override
      @Nullable
      public Income call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final Income _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
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
            _result = new Income(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
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
  public Flow<List<Income>> getIncomesByDateRange(final long startDate, final long endDate) {
    final String _sql = "SELECT * FROM incomes WHERE date >= ? AND date <= ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"incomes"}, new Callable<List<Income>>() {
      @Override
      @NonNull
      public List<Income> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Income> _result = new ArrayList<Income>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Income _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
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
            _item = new Income(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
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
  public Flow<List<Income>> searchIncomes(final String keyword) {
    final String _sql = "SELECT * FROM incomes WHERE remark LIKE '%' || ? || '%' ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, keyword);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"incomes"}, new Callable<List<Income>>() {
      @Override
      @NonNull
      public List<Income> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Income> _result = new ArrayList<Income>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Income _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
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
            _item = new Income(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
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
  public Object getIncomesByDateRangeSync(final long startDate, final long endDate,
      final Continuation<? super List<Income>> $completion) {
    final String _sql = "SELECT * FROM incomes WHERE date >= ? AND date <= ? ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Income>>() {
      @Override
      @NonNull
      public List<Income> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Income> _result = new ArrayList<Income>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Income _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
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
            _item = new Income(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
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
  public Flow<Double> getTotalIncomeByDateRange(final long startDate, final long endDate) {
    final String _sql = "SELECT SUM(amount) FROM incomes WHERE date >= ? AND date <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"incomes"}, new Callable<Double>() {
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
  public Flow<List<Income>> getTopIncomesByAmount(final long startDate, final long endDate,
      final int limit) {
    final String _sql = "SELECT * FROM incomes WHERE date >= ? AND date <= ? ORDER BY amount DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDate);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endDate);
    _argIndex = 3;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"incomes"}, new Callable<List<Income>>() {
      @Override
      @NonNull
      public List<Income> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfRemark = CursorUtil.getColumnIndexOrThrow(_cursor, "remark");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<Income> _result = new ArrayList<Income>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Income _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpSource;
            _tmpSource = _cursor.getString(_cursorIndexOfSource);
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
            _item = new Income(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpRemark,_tmpAccountId,_tmpCreatedAt);
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
