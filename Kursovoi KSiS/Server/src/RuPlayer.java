public class RuPlayer extends Thread {

    public void RuPlayer() {
        start();
    }

    @Override
    public void run() {
        try {
            System.out.println("RuPlayer");
            while (true) {
                sleep(1000);
                int size = Main.userList.size();
                User user1 = null;
                User user2 = null;
                int counter = 0;
                for (int i = 0; i < size; i++) {
                    if (Main.userList.get(i).getStatus().equals("RuSearching")) {
                        if (counter == 0) {
                            user1 = Main.userList.get(i);
                            counter++;
                        } else {
                            user2 = Main.userList.get(i);
                            user1.setStatus("Playing");
                            user2.setStatus("Playing");
                            new RuRoom(user1, user2);
                            break;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("/RuPlayer");
    }
}
