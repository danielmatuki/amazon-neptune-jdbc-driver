/*
 * Copyright <2021> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package software.aws.neptune.common.gremlindatamodel.resultset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.aws.neptune.gremlin.resultset.GremlinResultSetMetadata;
import software.aws.neptune.jdbc.ResultSet;
import software.aws.neptune.jdbc.utilities.SqlError;
import software.aws.neptune.jdbc.utilities.SqlState;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.sql.DatabaseMetaData.typeNullable;
import static java.sql.DatabaseMetaData.typeSearchable;
import static software.aws.neptune.jdbc.ResultSetMetaData.getTypePrecision;

public abstract class ResultSetGetTypeInfo extends ResultSet {
    /**
     * TYPE_NAME String => Type name
     * DATA_TYPE int => SQL data type from java.sql.Types
     * PRECISION int => maximum precision
     * LITERAL_PREFIX String => prefix used to quote a literal (may be null)
     * LITERAL_SUFFIX String => suffix used to quote a literal (may be null)
     * CREATE_PARAMS String => parameters used in creating the type (may be null)
     * NULLABLE short => can you use NULL for this type
     * CASE_SENSITIVE boolean => is it case sensitive
     * SEARCHABLE short => can you use "WHERE" based on this type
     * UNSIGNED_ATTRIBUTE boolean => is it unsigned
     * FIXED_PREC_SCALE boolean => can it be a money value
     * AUTO_INCREMENT boolean => can it be used for an auto-increment value
     * LOCAL_TYPE_NAME String => localized version of type name (may be null)
     * MINIMUM_SCALE short => minimum scale supported
     * MAXIMUM_SCALE short => maximum scale supported
     * SQL_DATA_TYPE int => unused
     * SQL_DATETIME_SUB int => unused
     * NUM_PREC_RADIX int => usually 2 or 10
     */

    private static final String TYPE_NAME = "TYPE_NAME";
    private static final String DATA_TYPE = "DATA_TYPE";
    private static final String PRECISION = "PRECISION";
    private static final String LITERAL_PREFIX = "LITERAL_PREFIX";
    private static final String LITERAL_SUFFIX = "LITERAL_SUFFIX";
    private static final String CREATE_PARAMS = "CREATE_PARAMS";
    private static final String NULLABLE = "NULLABLE";
    private static final String CASE_SENSITIVE = "CASE_SENSITIVE";
    private static final String SEARCHABLE = "SEARCHABLE";
    private static final String UNSIGNED_ATTRIBUTE = "UNSIGNED_ATTRIBUTE";
    private static final String FIXED_PREC_SCALE = "FIXED_PREC_SCALE";
    private static final String AUTO_INCREMENT = "AUTO_INCREMENT";
    private static final String LOCAL_TYPE_NAME = "LOCAL_TYPE_NAME";
    private static final String MINIMUM_SCALE = "MINIMUM_SCALE";
    private static final String MAXIMUM_SCALE = "MAXIMUM_SCALE";
    private static final String SQL_DATA_TYPE = "SQL_DATA_TYPE";
    private static final String SQL_DATETIME_SUB = "SQL_DATETIME_SUB";
    private static final String NUM_PREC_RADIX = "NUM_PREC_RADIX";

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultSetGetTypeInfo.class);
    private static final List<String> ORDERED_COLUMNS = new ArrayList<>();
    private static final Map<String, Class<?>> COLUMN_TYPE_MAP = new HashMap<>();
    private boolean wasNull = false;
    private final List<Map<String, Object>> typeInformation;

    static {
        ORDERED_COLUMNS.add(TYPE_NAME);
        ORDERED_COLUMNS.add(DATA_TYPE);
        ORDERED_COLUMNS.add(PRECISION);
        ORDERED_COLUMNS.add(LITERAL_PREFIX);
        ORDERED_COLUMNS.add(LITERAL_SUFFIX);
        ORDERED_COLUMNS.add(CREATE_PARAMS);
        ORDERED_COLUMNS.add(NULLABLE);
        ORDERED_COLUMNS.add(CASE_SENSITIVE);
        ORDERED_COLUMNS.add(SEARCHABLE);
        ORDERED_COLUMNS.add(UNSIGNED_ATTRIBUTE);
        ORDERED_COLUMNS.add(FIXED_PREC_SCALE);
        ORDERED_COLUMNS.add(AUTO_INCREMENT);
        ORDERED_COLUMNS.add(LOCAL_TYPE_NAME);
        ORDERED_COLUMNS.add(MINIMUM_SCALE);
        ORDERED_COLUMNS.add(MAXIMUM_SCALE);
        ORDERED_COLUMNS.add(SQL_DATA_TYPE);
        ORDERED_COLUMNS.add(SQL_DATETIME_SUB);
        ORDERED_COLUMNS.add(NUM_PREC_RADIX);

        COLUMN_TYPE_MAP.put(TYPE_NAME, String.class);
        COLUMN_TYPE_MAP.put(DATA_TYPE, Types.class);
        COLUMN_TYPE_MAP.put(PRECISION, Integer.class);
        COLUMN_TYPE_MAP.put(LITERAL_PREFIX, String.class);
        COLUMN_TYPE_MAP.put(LITERAL_SUFFIX, String.class);
        COLUMN_TYPE_MAP.put(CREATE_PARAMS, String.class);
        COLUMN_TYPE_MAP.put(NULLABLE, Short.class);
        COLUMN_TYPE_MAP.put(CASE_SENSITIVE, Boolean.class);
        COLUMN_TYPE_MAP.put(SEARCHABLE, Short.class);
        COLUMN_TYPE_MAP.put(UNSIGNED_ATTRIBUTE, Boolean.class);
        COLUMN_TYPE_MAP.put(FIXED_PREC_SCALE, Boolean.class);
        COLUMN_TYPE_MAP.put(AUTO_INCREMENT, Boolean.class);
        COLUMN_TYPE_MAP.put(LOCAL_TYPE_NAME, String.class);
        COLUMN_TYPE_MAP.put(MINIMUM_SCALE, Short.class);
        COLUMN_TYPE_MAP.put(MAXIMUM_SCALE, Short.class);
        COLUMN_TYPE_MAP.put(SQL_DATA_TYPE, Integer.class);
        COLUMN_TYPE_MAP.put(SQL_DATETIME_SUB, Integer.class);
        COLUMN_TYPE_MAP.put(NUM_PREC_RADIX, Integer.class);
    }

    protected static void populateConstants(final List<Map<String, Object>> typeInfo) {
        for (Map<String, Object> info : typeInfo) {
            info.put("CREATE_PARAMS", null);
            info.put("NULLABLE", typeNullable);
            info.put("SEARCHABLE", typeSearchable);
            info.put("FIXED_PREC_SCALE", false);
            info.put("AUTO_INCREMENT", false);
            info.put("LOCAL_TYPE_NAME", null);
            info.put("SQL_DATA_TYPE", null);
            info.put("SQL_DATETIME_SUB", null);
        }
    }

    protected static void putInfo(final List<Map<String, Object>> typeInfo, final String typeName, final int dataType,
                                  final boolean isText, final boolean isNumeric, final boolean isUnsignedAttribute) {
        final Map<String, Object> info = new HashMap<>();
        info.put(TYPE_NAME, typeName);
        info.put("DATA_TYPE", dataType);
        info.put("PRECISION", getTypePrecision(dataType));
        info.put("UNSIGNED_ATTRIBUTE", isUnsignedAttribute);

        info.put("CASE_SENSITIVE", isText);
        info.put("LITERAL_PREFIX", isText ? "'" : null);
        info.put("LITERAL_SUFFIX", isText ? "'" : null);

        info.put("MINIMUM_SCALE", isNumeric ? 0 : null);
        info.put("MAXIMUM_SCALE", isNumeric ? 0 : null);
        info.put("NUM_PREC_RADIX", isNumeric ? 10 : null);

        typeInfo.add(info);
    }

    protected static void putInfo(final List<Map<String, Object>> typeInfo, final String typeName, final int dataType,
                                  final boolean isText, final boolean isNumeric) {
        putInfo(typeInfo, typeName, dataType, isText, isNumeric, false);
    }

    /**
     * ResultSetGetTypeInfo constructor, initializes super class.
     *
     * @param statement                Statement Object.
     * @param rows                     List of type information.
     */
    public ResultSetGetTypeInfo(final Statement statement, final List<Map<String, Object>> rows) {
        super(statement, ORDERED_COLUMNS, rows.size());
        this.typeInformation = rows;
    }

    @Override
    public Object getConvertedValue(final int columnIndex) throws SQLException {
        verifyOpen();
        final int index = getRowIndex();
        if ((index < 0) || (index >= this.typeInformation.size())) {
            throw SqlError
                    .createSQLException(LOGGER, SqlState.DATA_EXCEPTION, SqlError.INVALID_INDEX, getRowIndex() + 1,
                            this.typeInformation.size());
        }
        if ((columnIndex <= 0) || (columnIndex > ORDERED_COLUMNS.size())) {
            throw SqlError
                    .createSQLException(LOGGER, SqlState.DATA_EXCEPTION, SqlError.INVALID_COLUMN_INDEX, columnIndex,
                            ORDERED_COLUMNS.size());
        }

        final String key = ORDERED_COLUMNS.get(columnIndex - 1);
        if (this.typeInformation.get(index).containsKey(key)) {
            final Object data = this.typeInformation.get(index).get(key);
            this.wasNull = (data == null);
            return data;
        } else {
            final String errorMessage = "Could not get TypeInfo column: " + key;
            LOGGER.error(errorMessage);
            throw new SQLException(errorMessage);
        }
    }

    @Override
    protected ResultSetMetaData getResultMetadata() {
        final List<Class<?>> rowTypes = new ArrayList<>();
        for (final String column : ORDERED_COLUMNS) {
            rowTypes.add(COLUMN_TYPE_MAP.get(column));
        }
        return new GremlinResultSetMetadata(ORDERED_COLUMNS, rowTypes);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.wasNull;
    }

    @Override
    protected void doClose() throws SQLException {
    }
}
