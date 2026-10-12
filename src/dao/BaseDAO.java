package dao;

import connectDB.ConnectDB;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseDAO<T> {
    private final String tableName;
    private final Class<T> entityType;
    private final List<String> primaryKeys;
    private final List<Field> fields;

    protected BaseDAO(Class<T> entityType, String tableName, String... primaryKeys) {
        this.entityType = entityType;
        this.tableName = tableName;
        this.primaryKeys = Arrays.asList(primaryKeys);
        this.fields = Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .collect(Collectors.toList());
    }

    public List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + quote(tableName);
        try (Connection connection = openConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapRow(resultSet));
            }
            return results;
        }
    }

    public T findById(Object... keyValues) throws SQLException {
        validateKeyValues(keyValues);
        String whereClause = primaryKeys.stream()
                .map(key -> quote(key) + " = ?")
                .collect(Collectors.joining(" AND "));
        String sql = "SELECT * FROM " + quote(tableName) + " WHERE " + whereClause;

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindValues(statement, keyValues, 1);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    public boolean insert(T entity) throws SQLException {
        List<Field> insertFields = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Field field : fields) {
            Object value = getFieldValue(field, entity);
            if (value != null) {
                insertFields.add(field);
                values.add(value);
            }
        }
        if (insertFields.isEmpty()) {
            throw new SQLException("Không có dữ liệu để thêm vào " + tableName);
        }

        String columns = insertFields.stream()
                .map(field -> quote(field.getName()))
                .collect(Collectors.joining(", "));
        String placeholders = insertFields.stream()
                .map(field -> "?")
                .collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + quote(tableName) + " (" + columns + ") VALUES (" + placeholders + ")";

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindValues(statement, values.toArray(), 1);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(T entity) throws SQLException {
        List<Field> updateFields = fields.stream()
                .filter(field -> !primaryKeys.contains(field.getName()))
                .collect(Collectors.toList());
        String assignments = updateFields.stream()
                .map(field -> quote(field.getName()) + " = ?")
                .collect(Collectors.joining(", "));
        String whereClause = primaryKeys.stream()
                .map(key -> quote(key) + " = ?")
                .collect(Collectors.joining(" AND "));
        String sql = "UPDATE " + quote(tableName) + " SET " + assignments + " WHERE " + whereClause;
        List<Object> values = new ArrayList<>();
        for (Field field : updateFields) {
            values.add(getFieldValue(field, entity));
        }
        for (String key : primaryKeys) {
            values.add(getFieldValue(findField(key), entity));
        }

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindValues(statement, values.toArray(), 1);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteById(Object... keyValues) throws SQLException {
        validateKeyValues(keyValues);
        String whereClause = primaryKeys.stream()
                .map(key -> quote(key) + " = ?")
                .collect(Collectors.joining(" AND "));
        String sql = "DELETE FROM " + quote(tableName) + " WHERE " + whereClause;

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindValues(statement, keyValues, 1);
            return statement.executeUpdate() > 0;
        }
    }

    private Connection openConnection() throws SQLException {
        ConnectDB.getInstance().connect();
        Connection connection = ConnectDB.getConnection();
        if (connection == null) {
            throw new SQLException("Không thể kết nối cơ sở dữ liệu.");
        }
        return connection;
    }

    private T mapRow(ResultSet resultSet) throws SQLException {
        try {
            T entity = entityType.getDeclaredConstructor().newInstance();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(entity, resultSet.getObject(field.getName()));
            }
            return entity;
        } catch (ReflectiveOperationException exception) {
            throw new SQLException("Không thể ánh xạ dữ liệu bảng " + tableName, exception);
        }
    }

    private Object getFieldValue(Field field, T entity) throws SQLException {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException exception) {
            throw new SQLException("Không thể đọc thuộc tính " + field.getName(), exception);
        }
    }

    private Field findField(String name) throws SQLException {
        return fields.stream()
                .filter(field -> field.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new SQLException("Không tìm thấy khóa " + name + " trong " + entityType.getName()));
    }

    private void validateKeyValues(Object[] keyValues) throws SQLException {
        if (keyValues == null || keyValues.length != primaryKeys.size()) {
            throw new SQLException("Bảng " + tableName + " cần " + primaryKeys.size() + " giá trị khóa chính.");
        }
    }

    private void bindValues(PreparedStatement statement, Object[] values, int startIndex) throws SQLException {
        for (int index = 0; index < values.length; index++) {
            statement.setObject(startIndex + index, values[index]);
        }
    }

    private String quote(String identifier) {
        return "[" + identifier + "]";
    }
}