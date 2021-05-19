package database;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;

public class DataBaseWorker {

    public void updateRuStats(String name, String speedC, String speedW, String mistakes) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/usersDB?serverTimezone=Europe/Minsk&useSSL=false", "root", "FrodoBagins21")) {
           Statement statement = connection.createStatement();
           String sql = "UPDATE ru_stats SET" +
                   " SpeedC = (Plays * SpeedC + " + speedC + ") / (Plays + 1)," +
                   " SpeedW = (Plays * SpeedW + " + speedW + ") / (Plays + 1)," +
                   " Mistakes = (Plays * Mistakes + " + mistakes + ") / (Plays + 1)," +
                   " Plays = Plays + 1" +
                   " WHERE User = " + name;
           statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String getUserInfo(String name, String password) {
        String result = "";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/usersDB?serverTimezone=Europe/Minsk&useSSL=false", "root", "FrodoBagins21");) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT Name FROM users WHERE Name='" + name + "' AND Password='" + password + "'");
            //Если запись найдена то возвращаем её, иначе возвращаем пустую строку
            if (resultSet.next()) {
                System.out.println("Запись найдена");
                statement = connection.createStatement();
                ResultSet resultSet1 = statement.executeQuery("SELECT * FROM ru_stats WHERE User='" + name + "';");
                resultSet1.next();
                result += resultSet1.getString(1) + "|";
                result += resultSet1.getInt(2) + "|";
                result += resultSet1.getFloat(3) + "|";
                result += resultSet1.getFloat(4) + "|";
                result += resultSet1.getFloat(5) + "|";
                statement = connection.createStatement();
                ResultSet resultSet2 = statement.executeQuery("SELECT * FROM en_stats WHERE User='" + name + "';");
                resultSet2.next();
                result += resultSet2.getInt(2) + "|";
                result += resultSet2.getFloat(3) + "|";
                result += resultSet2.getFloat(4) + "|";
                result += resultSet2.getFloat(5);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    //Returns: true - новый пользователь успешно добавлен, false - пользователь с данным именем уже существует
    public boolean registerUser(String login, String password) {
        boolean result = true;
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/usersDB?serverTimezone=Europe/Minsk&useSSL=false", "root", "FrodoBagins21")) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO users(Name, Password) VALUES (?, ?)");
            statement.setString(1, login);
            statement.setString(2, password);
            int status = statement.executeUpdate();
            statement = connection.prepareStatement("INSERT INTO ru_stats(User, Plays, SpeedC, SpeedW, Mistakes) VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, login);
            statement.setInt(2, 0);
            statement.setFloat(3, 0);
            statement.setFloat(4, 0);
            statement.setFloat(5, 0);
            statement.executeUpdate();
            statement = connection.prepareStatement("INSERT INTO en_stats(User, Plays, SpeedC, SpeedW, Mistakes) VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, login);
            statement.setInt(2, 0);
            statement.setFloat(3, 0);
            statement.setFloat(4, 0);
            statement.setFloat(5, 0);
            statement.executeUpdate();


        } catch (SQLException throwables) {
            result = false;
            System.err.println(throwables.getMessage());
        }
        return result;
    }

    public DataBaseWorker() {
        try {
            Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

