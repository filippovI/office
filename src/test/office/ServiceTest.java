package test.office;

import office.Department;
import office.Employee;
import office.Service;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceTest {

    private Service testService;
    private final String DB_URL = "jdbc:h2:mem:test_office;DB_CLOSE_DELAY=-1";

    @BeforeEach
    void setUp(TestInfo testInfo) {
        if (testInfo.getTags().contains("skipSetup"))
            return;
        testService = new Service(DB_URL);
        testService.createDB();
    }

    @Test
    @DisplayName("Проверка создания БД")
    @Tag("skipSetup")
    public void testCreateDB() throws SQLException {
        List<String> columnsForDepartment = List.of("ID", "NAME");
        List<String> columnsForEmployee = List.of("ID", "NAME", "DepartmentID");

        //Проверяем, что при создании таблицы затираются старые таблицы и количество записей в таблицах
        createTestDb();
        testService = new Service(DB_URL);
        testService.createDB();
        assertEquals(3, getCount("Department", ""));
        assertEquals(5, getCount("Employee", ""));

        //Проверяем колонки в созданных таблицах
        assertEquals(columnsForDepartment.stream().map(String::toUpperCase).collect(Collectors.toList()),
                getColumns("Department"));
        assertEquals(columnsForEmployee.stream().map(String::toUpperCase).collect(Collectors.toList()),
                getColumns("Employee"));
    }

    @Test
    @DisplayName("Проверка добавления департамента")
    public void testAddDepartment() throws SQLException {
        Department dept = new Department(10, "Marketing");
        testService.addDepartment(dept);
        assertEquals(1, getCount("Department", "ID = 10 AND NAME = 'Marketing'"));
    }

    @Test
    @DisplayName("Проверка удаления департамента")
    public void testRemoveDepartment() throws SQLException {
        Department dept = new Department(2, "IT");
        testService.removeDepartment(dept);
        assertEquals(0, getCount("Department", "ID = 2"));
        assertEquals(0, getCount("Employee", "DepartmentID = 2"));
    }


    @Test
    @DisplayName("Проверка добавления сотрудника")
    public void testAddEmployee() throws SQLException {
        Employee emp = new Employee(100, "Ivan", 2);
        testService.addEmployee(emp);
        assertEquals(1, getCount("Employee", "ID = 100 AND NAME = 'Ivan'"));
    }

    @Test
    @DisplayName("Проверка удаления сотрудника")
    public void testRemoveEmployee() throws SQLException {
        Employee emp = new Employee(1, "Pete", 1);
        testService.removeEmployee(emp);
        assertEquals(0, getCount("Employee", "ID = 1"));
    }

    private int getCount(String tableName, String condition) throws SQLException {
        try (Connection con = DriverManager.getConnection(DB_URL);
             Statement st = con.createStatement()) {
            String query = "SELECT COUNT(*) FROM " + tableName + (condition.isEmpty() ? "" : " WHERE " + condition);
            ResultSet rs = st.executeQuery(query);
            rs.next();
            return rs.getInt(1);
        }
    }

    private List<String> getColumns(String tableName) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(DB_URL);
             Statement stm = con.createStatement()) {
            String sql = "SELECT * FROM " + tableName + " WHERE 1=0";
            ResultSet rs = stm.executeQuery(sql);
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                columnNames.add(resultSetMetaData.getColumnName(i));
            }
        }
        return columnNames;
    }

    private void createTestDb() throws SQLException {
        try (Connection con = DriverManager.getConnection(DB_URL);
             Statement stm = con.createStatement()) {
            String query = "CREATE TABLE Department (ID INT PRIMARY KEY)";
            stm.executeUpdate(query);
            stm.executeUpdate("INSERT INTO Department VALUES(1)");
            stm.executeUpdate("INSERT INTO Department VALUES(2)");
            stm.executeUpdate("INSERT INTO Department VALUES(3)");
            stm.executeUpdate("INSERT INTO Department VALUES(4)");
        }
    }
}