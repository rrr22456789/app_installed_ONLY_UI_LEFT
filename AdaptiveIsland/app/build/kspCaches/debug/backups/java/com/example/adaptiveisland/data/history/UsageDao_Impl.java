package com.example.adaptiveisland.data.history;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class UsageDao_Impl implements UsageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AppUsageEntity> __insertionAdapterOfAppUsageEntity;

  private final EntityInsertionAdapter<DailyUsageEntity> __insertionAdapterOfDailyUsageEntity;

  public UsageDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAppUsageEntity = new EntityInsertionAdapter<AppUsageEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `app_usage_history` (`date`,`packageName`,`appName`,`totalTimeMs`,`lastUpdatedTimestamp`) VALUES (?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AppUsageEntity value) {
        if (value.getDate() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getDate());
        }
        if (value.getPackageName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getPackageName());
        }
        if (value.getAppName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getAppName());
        }
        stmt.bindLong(4, value.getTotalTimeMs());
        stmt.bindLong(5, value.getLastUpdatedTimestamp());
      }
    };
    this.__insertionAdapterOfDailyUsageEntity = new EntityInsertionAdapter<DailyUsageEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `daily_usage_history` (`date`,`totalScreenTimeMs`,`lastUpdatedTimestamp`) VALUES (?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, DailyUsageEntity value) {
        if (value.getDate() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getDate());
        }
        stmt.bindLong(2, value.getTotalScreenTimeMs());
        stmt.bindLong(3, value.getLastUpdatedTimestamp());
      }
    };
  }

  @Override
  public Object insertOrReplaceAppUsage(final AppUsageEntity appUsage,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAppUsageEntity.insert(appUsage);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object insertOrReplaceDailyUsage(final DailyUsageEntity dailyUsage,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDailyUsageEntity.insert(dailyUsage);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object getAppUsageByDateAndPackage(final String date, final String packageName,
      final Continuation<? super AppUsageEntity> continuation) {
    final String _sql = "SELECT * FROM app_usage_history WHERE date = ? AND packageName = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (date == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, date);
    }
    _argIndex = 2;
    if (packageName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, packageName);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppUsageEntity>() {
      @Override
      public AppUsageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfTotalTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTimeMs");
          final int _cursorIndexOfLastUpdatedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedTimestamp");
          final AppUsageEntity _result;
          if(_cursor.moveToFirst()) {
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final String _tmpPackageName;
            if (_cursor.isNull(_cursorIndexOfPackageName)) {
              _tmpPackageName = null;
            } else {
              _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            }
            final String _tmpAppName;
            if (_cursor.isNull(_cursorIndexOfAppName)) {
              _tmpAppName = null;
            } else {
              _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            }
            final long _tmpTotalTimeMs;
            _tmpTotalTimeMs = _cursor.getLong(_cursorIndexOfTotalTimeMs);
            final long _tmpLastUpdatedTimestamp;
            _tmpLastUpdatedTimestamp = _cursor.getLong(_cursorIndexOfLastUpdatedTimestamp);
            _result = new AppUsageEntity(_tmpDate,_tmpPackageName,_tmpAppName,_tmpTotalTimeMs,_tmpLastUpdatedTimestamp);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Flow<List<AppUsageEntity>> observeAppUsageForDate(final String date) {
    final String _sql = "SELECT * FROM app_usage_history WHERE date = ? ORDER BY totalTimeMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (date == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, date);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[]{"app_usage_history"}, new Callable<List<AppUsageEntity>>() {
      @Override
      public List<AppUsageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfTotalTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTimeMs");
          final int _cursorIndexOfLastUpdatedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedTimestamp");
          final List<AppUsageEntity> _result = new ArrayList<AppUsageEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AppUsageEntity _item;
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final String _tmpPackageName;
            if (_cursor.isNull(_cursorIndexOfPackageName)) {
              _tmpPackageName = null;
            } else {
              _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            }
            final String _tmpAppName;
            if (_cursor.isNull(_cursorIndexOfAppName)) {
              _tmpAppName = null;
            } else {
              _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            }
            final long _tmpTotalTimeMs;
            _tmpTotalTimeMs = _cursor.getLong(_cursorIndexOfTotalTimeMs);
            final long _tmpLastUpdatedTimestamp;
            _tmpLastUpdatedTimestamp = _cursor.getLong(_cursorIndexOfLastUpdatedTimestamp);
            _item = new AppUsageEntity(_tmpDate,_tmpPackageName,_tmpAppName,_tmpTotalTimeMs,_tmpLastUpdatedTimestamp);
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
  public Object getAppUsageForDate(final String date,
      final Continuation<? super List<AppUsageEntity>> continuation) {
    final String _sql = "SELECT * FROM app_usage_history WHERE date = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (date == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, date);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AppUsageEntity>>() {
      @Override
      public List<AppUsageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfPackageName = CursorUtil.getColumnIndexOrThrow(_cursor, "packageName");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfTotalTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTimeMs");
          final int _cursorIndexOfLastUpdatedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedTimestamp");
          final List<AppUsageEntity> _result = new ArrayList<AppUsageEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AppUsageEntity _item;
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final String _tmpPackageName;
            if (_cursor.isNull(_cursorIndexOfPackageName)) {
              _tmpPackageName = null;
            } else {
              _tmpPackageName = _cursor.getString(_cursorIndexOfPackageName);
            }
            final String _tmpAppName;
            if (_cursor.isNull(_cursorIndexOfAppName)) {
              _tmpAppName = null;
            } else {
              _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            }
            final long _tmpTotalTimeMs;
            _tmpTotalTimeMs = _cursor.getLong(_cursorIndexOfTotalTimeMs);
            final long _tmpLastUpdatedTimestamp;
            _tmpLastUpdatedTimestamp = _cursor.getLong(_cursorIndexOfLastUpdatedTimestamp);
            _item = new AppUsageEntity(_tmpDate,_tmpPackageName,_tmpAppName,_tmpTotalTimeMs,_tmpLastUpdatedTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getDailyUsageByDate(final String date,
      final Continuation<? super DailyUsageEntity> continuation) {
    final String _sql = "SELECT * FROM daily_usage_history WHERE date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (date == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, date);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DailyUsageEntity>() {
      @Override
      public DailyUsageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTotalScreenTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "totalScreenTimeMs");
          final int _cursorIndexOfLastUpdatedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedTimestamp");
          final DailyUsageEntity _result;
          if(_cursor.moveToFirst()) {
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final long _tmpTotalScreenTimeMs;
            _tmpTotalScreenTimeMs = _cursor.getLong(_cursorIndexOfTotalScreenTimeMs);
            final long _tmpLastUpdatedTimestamp;
            _tmpLastUpdatedTimestamp = _cursor.getLong(_cursorIndexOfLastUpdatedTimestamp);
            _result = new DailyUsageEntity(_tmpDate,_tmpTotalScreenTimeMs,_tmpLastUpdatedTimestamp);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Flow<DailyUsageEntity> observeDailyUsageForDate(final String date) {
    final String _sql = "SELECT * FROM daily_usage_history WHERE date = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (date == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, date);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[]{"daily_usage_history"}, new Callable<DailyUsageEntity>() {
      @Override
      public DailyUsageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTotalScreenTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "totalScreenTimeMs");
          final int _cursorIndexOfLastUpdatedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedTimestamp");
          final DailyUsageEntity _result;
          if(_cursor.moveToFirst()) {
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final long _tmpTotalScreenTimeMs;
            _tmpTotalScreenTimeMs = _cursor.getLong(_cursorIndexOfTotalScreenTimeMs);
            final long _tmpLastUpdatedTimestamp;
            _tmpLastUpdatedTimestamp = _cursor.getLong(_cursorIndexOfLastUpdatedTimestamp);
            _result = new DailyUsageEntity(_tmpDate,_tmpTotalScreenTimeMs,_tmpLastUpdatedTimestamp);
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
  public Flow<List<DailyUsageEntity>> observeAllDailyUsages() {
    final String _sql = "SELECT * FROM daily_usage_history ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"daily_usage_history"}, new Callable<List<DailyUsageEntity>>() {
      @Override
      public List<DailyUsageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfTotalScreenTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "totalScreenTimeMs");
          final int _cursorIndexOfLastUpdatedTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdatedTimestamp");
          final List<DailyUsageEntity> _result = new ArrayList<DailyUsageEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final DailyUsageEntity _item;
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final long _tmpTotalScreenTimeMs;
            _tmpTotalScreenTimeMs = _cursor.getLong(_cursorIndexOfTotalScreenTimeMs);
            final long _tmpLastUpdatedTimestamp;
            _tmpLastUpdatedTimestamp = _cursor.getLong(_cursorIndexOfLastUpdatedTimestamp);
            _item = new DailyUsageEntity(_tmpDate,_tmpTotalScreenTimeMs,_tmpLastUpdatedTimestamp);
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

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
