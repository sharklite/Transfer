package ids.minishark;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

abstract class TransferBase {

    DataSource dataSource;

    abstract void setDataSource(DataSource dataSource);
    
    public DataSource getDataSource() {
        return dataSource;
    }

    Connection getConnection() {
        Connection c = null;
        try {
            c = this.dataSource.getConnection();
            c.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }


    protected int executeUpdate(String preparedSql, Object... supportedSQLArg) {
        return TransferExecutor.executeUpdate(getConnection(), preparedSql, supportedSQLArg);
    }

    protected List<Object> firstColumnValues(String preparedSql, Object... supportedSQLArg) {
        return TransferExecutor.firstColumnValues(getConnection(), preparedSql, supportedSQLArg);
    }

    protected Object getObject(String preparedSql, Object... supportedSQLArg) {
        return TransferExecutor.getObject(getConnection(), preparedSql, supportedSQLArg);
    }

    protected String getString(String preparedSql, Object... supportedSQLArg) {
        Object o = this.getObject(preparedSql, supportedSQLArg);
        return o == null ? null : o.toString();
    }

    protected BigDecimal getBigDecimal(String preparedSql, Object... supportedSQLArg) {
        Object object = this.getObject(preparedSql, supportedSQLArg);
        if (object instanceof BigDecimal) {
            return (BigDecimal) object;
        }else if(object instanceof Number){
            return new BigDecimal(object.toString());
        }
        String s = String.valueOf(object);
        return _String_.isNumeric(s) ? new BigDecimal(s) : null;
    }

    protected boolean getBoolean(String preparedSql, Object... supportedSQLArg) {
        Object object = this.getObject(preparedSql, supportedSQLArg);
        if (object instanceof Boolean)
            return (boolean) object;
        String s = String.valueOf(object);
        if(_String_.isNumeric(s)){
            return !(new BigDecimal(s).intValue()==0);
        }
        return Boolean.parseBoolean(s);
    }

    protected byte getByte(String preparedSql, Object... supportedSQLArg) {
        Number number = this.getBigDecimal(preparedSql, supportedSQLArg);
        return number == null ? 0 : number.byteValue();
    }

    protected short getShort(String preparedSql, Object... supportedSQLArg) {
        Number number = this.getBigDecimal(preparedSql, supportedSQLArg);
        return number == null ? 0 : number.shortValue();
    }

    protected int getInt(String preparedSql, Object... supportedSQLArg) {
        Number number = this.getBigDecimal(preparedSql, supportedSQLArg);
        return number == null ? 0 : number.intValue();
    }

    protected long getLong(String preparedSql, Object... supportedSQLArg) {
        Number number = this.getBigDecimal(preparedSql, supportedSQLArg);
        return number == null ? 0 : number.longValue();
    }

    protected double getDouble(String preparedSql, Object... supportedSQLArg) {
        Number number = this.getBigDecimal(preparedSql, supportedSQLArg);
        return number == null ? 0 : number.doubleValue();
    }

    protected Date getDate(String preparedSql, Object... supportedSQLArg) {
        Object object = this.getObject(preparedSql, supportedSQLArg);
        if (object instanceof Date) {
            return (Date) object;
        }
        Number number = null;
        String s = String.valueOf(object);
        if (_String_.isNumeric(s))
            number = new BigDecimal(s);
        return number == null ? null : new Date(number.longValue());
    }

}
