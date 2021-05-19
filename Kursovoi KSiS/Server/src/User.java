
import database.DataBaseWorker;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class User extends Thread {
    private Socket socket;
    private String name;
    private byte[] icon;
    private DataBaseWorker dataBaseWorker;
    private BufferedReader br;
    private BufferedWriter bw;

    private String status = "Inactive";

    public User(Socket socket) {
        this.socket = socket;
        start();
    }

    @Override
    public void run() {
        this.socket = socket;
        dataBaseWorker = new DataBaseWorker();
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Ожидаем логина и пароля либо данных для регистрации
        while (true) {
            boolean breakFlag = false; //флаг завершения цикла
            try {
                //Читаем сообщение
                String[] params = getParams(br.readLine());
                if (params[0].equals("log")) {                               //ВХОД В АККАУНТ
                    String name = params[1];
                    String password = params[2];
                    //Аутентифицируем аккаунт и формируем ответ
                    boolean alreadyLoggedFlag = false;
                    //Проверка или данный пользователь уже вошёл
                    for (int i = 0; i < Main.userList.size(); i++) {
                        if (Main.userList.get(i).name == null)
                            continue;
                        if (Main.userList.get(i).name.equals(name)) {
                            alreadyLoggedFlag = true;
                            break;
                        }
                    }
                    if (alreadyLoggedFlag) {
                        bw.write("UserAlreadyLogged\n");
                        bw.flush();
                        continue;
                    }
                    String response = dataBaseWorker.getUserInfo(name, password);
                    if (response.equals("")) {
                        bw.write("UserNotFound\n");
                        bw.flush();
                    } else {
                        this.name = name;
                        bw.write(response + "\n");
                        bw.flush();
                        FileInputStream fis = new FileInputStream("files/icons/" + name);
                        int size = fis.available();
                        icon = new byte[size];
                        fis.read(icon, 0, size);
                        socket.getOutputStream().write(icon);
                        socket.getOutputStream().flush();
                        fis.close();
                        break;
                    }
                } else if (params[0].equals("reg")) {                   //РЕГИСТРАЦИЯ НОВОГО ПОЛЬЗОВАТЕЛЯ
                    String name = params[1];
                    String password = params[2];
                    //status = true - новый пользователь создан успешно
                    //status == false - пользователь с данным именем уже существует
                    boolean status = dataBaseWorker.registerUser(name, password);
                    while (socket.getInputStream().available() == 0) ;
                    int size = socket.getInputStream().available();
                    icon = new byte[size];
                    socket.getInputStream().read(icon, 0, size);
                    FileOutputStream fos = new FileOutputStream("files/icons/" + name);
                    fos.write(icon);
                    fos.close();
                    String response = "";
                    if (status == true) {
                        this.name = name;
                        response = dataBaseWorker.getUserInfo(name, password) + "\n";
                        bw.write(response);
                        bw.flush();
                        breakFlag = true;
                    } else {
                        response = "UserAlreadyExist\n";
                        bw.write(response);
                        bw.flush();
                    }
                }
            } catch (SocketException e) {
                System.err.println("Соединение с пользователем " + socket.getInetAddress() + " потеряно");
                closeSocket();
                int index = Main.userList.indexOf(this);
                Main.userList.remove(index);
                System.out.println(Main.userList.size());
                breakFlag = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (breakFlag)
                break;
        }
        new SearchWaiter(this);
    }


    private void closeSocket() {
        try {
            br.close();
            bw.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String[] getParams(String line) {
        String[] params = line.split("\\|");
        return params;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getUserName() {
        return name;
    }

    public void setUserName(String name) {
        this.name = name;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public BufferedReader getBr() {
        return br;
    }

    public void setBr(BufferedReader br) {
        this.br = br;
    }

    public BufferedWriter getBw() {
        return bw;
    }

    public void setBw(BufferedWriter bw) {
        this.bw = bw;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
