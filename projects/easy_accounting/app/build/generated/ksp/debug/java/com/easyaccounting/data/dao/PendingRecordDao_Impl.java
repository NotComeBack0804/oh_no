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
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.easyaccounting.data.entity.PaySource;
import com.easyaccounting.data.entity.PendingRecord;
import com.easyaccounting.data.entity.PendingStatus;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
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
public final class PendingRecordDao_Impl implements PendingRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PendingRecord> __insertionAdapterOfPendingRecord;

  private final EntityDeletionOrUpdateAdapter<PendingRecord> __deletionAdapterOfPendingRecord;

  private final EntityDeletionOrUpdateAdapter<PendingRecord> __updateAdapterOfPendingRecord;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByStatus;

  public PendingRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPendingRecord = new EntityInsertionAdapter<PendingRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `pending_records` (`id`,`amount`,`source`,`date`,`createdAt`,`status`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PendingRecord entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getAmount());
        statement.bindString(3, __PaySource_enumToString(entity.getSource()));
        statement.bindLong(4, entity.getDate());
        statement.bindLong(5, entity.getCreatedAt());
        statement.bindString(6, __PendingStatus_enumToString(entity.getStatus()));
      }
    };
    this.__deletionAdapterOfPendingRecord = new EntityDeletionOrUpdateAdapter<PendingRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `pending_records` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PendingRecord entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPendingRecord = new EntityDeletionOrUpdateAdapter<PendingRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `pending_records` SET `id` = ?,`amount` = ?,`source` = ?,`date` = ?,`createdAt` = ?,`status` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PendingRecord entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getAmount());
        statement.bindString(3, __PaySource_enumToString(entity.getSource()));
        statement.bindLong(4, entity.getDate());
        statement.bindLong(5, entity.getCreatedAt());
        statement.bindString(6, __PendingStatus_enumToString(entity.getStatus()));
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pending_records SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM pending_records WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteByStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM pending_records WHERE status = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final PendingRecord pendingRecord,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPendingRecord.insertAndReturnId(pendingRecord);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final PendingRecord pendingRecord,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPendingRecord.handle(pendingRecord);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final PendingRecord pendingRecord,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPendingRecord.handle(pendingRecord);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatus(final long id, final PendingStatus status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __PendingStatus_enumToString(status));
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByStatus(final PendingStatus status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __PendingStatus_enumToString(status));
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteByStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PendingRecord>> getPendingRecordsByStatus(final PendingStatus status) {
    final String _sql = "SELECT * FROM pending_records WHERE status = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __PendingStatus_enumToString(status));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pending_records"}, new Callable<List<PendingRecord>>() {
      @Override
      @NonNull
      public List<PendingRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<PendingRecord> _result = new ArrayList<PendingRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PendingRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaySource _tmpSource;
            _tmpSource = __PaySource_stringToEnum(_cursor.getString(_cursorIndexOfSource));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final PendingStatus _tmpStatus;
            _tmpStatus = __PendingStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            _item = new PendingRecord(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpCreatedAt,_tmpStatus);
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
  public Flow<List<PendingRecord>> getAllPendingRecords() {
    final String _sql = "SELECT * FROM pending_records ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pending_records"}, new Callable<List<PendingRecord>>() {
      @Override
      @NonNull
      public List<PendingRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<PendingRecord> _result = new ArrayList<PendingRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PendingRecord _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaySource _tmpSource;
            _tmpSource = __PaySource_stringToEnum(_cursor.getString(_cursorIndexOfSource));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final PendingStatus _tmpStatus;
            _tmpStatus = __PendingStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            _item = new PendingRecord(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpCreatedAt,_tmpStatus);
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
  public Object getPendingRecordById(final long id,
      final Continuation<? super PendingRecord> $completion) {
    final String _sql = "SELECT * FROM pending_records WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PendingRecord>() {
      @Override
      @Nullable
      public PendingRecord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final PendingRecord _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaySource _tmpSource;
            _tmpSource = __PaySource_stringToEnum(_cursor.getString(_cursorIndexOfSource));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final PendingStatus _tmpStatus;
            _tmpStatus = __PendingStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            _result = new PendingRecord(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpCreatedAt,_tmpStatus);
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
  public Object getLatestPendingRecord(final PendingStatus status,
      final Continuation<? super PendingRecord> $completion) {
    final String _sql = "SELECT * FROM pending_records WHERE status = ? ORDER BY createdAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __PendingStatus_enumToString(status));
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PendingRecord>() {
      @Override
      @Nullable
      public PendingRecord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final PendingRecord _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final PaySource _tmpSource;
            _tmpSource = __PaySource_stringToEnum(_cursor.getString(_cursorIndexOfSource));
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final PendingStatus _tmpStatus;
            _tmpStatus = __PendingStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            _result = new PendingRecord(_tmpId,_tmpAmount,_tmpSource,_tmpDate,_tmpCreatedAt,_tmpStatus);
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
  public Flow<Integer> getPendingCount(final PendingStatus status) {
    final String _sql = "SELECT COUNT(*) FROM pending_records WHERE status = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __PendingStatus_enumToString(status));
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pending_records"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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

  private String __PaySource_enumToString(@NonNull final PaySource _value) {
    switch (_value) {
      case ALIPAY: return "ALIPAY";
      case WECHAT: return "WECHAT";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __PendingStatus_enumToString(@NonNull final PendingStatus _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case CONFIRMED: return "CONFIRMED";
      case IGNORED: return "IGNORED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private PaySource __PaySource_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "ALIPAY": return PaySource.ALIPAY;
      case "WECHAT": return PaySource.WECHAT;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private PendingStatus __PendingStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return PendingStatus.PENDING;
      case "CONFIRMED": return PendingStatus.CONFIRMED;
      case "IGNORED": return PendingStatus.IGNORED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
