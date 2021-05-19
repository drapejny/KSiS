import java.io.BufferedReader;
import java.io.IOException;

public class SearchWaiter extends Thread {
    private User user;

    public SearchWaiter(User user){
        this.user = user;
        start();
    }

    @Override
    public void run(){
        BufferedReader br = user.getBr();
        try {
            System.out.println("SearchWaiter");
            while (true) {
                String message = br.readLine();
                System.out.println("Прочитали " + message);
                if(message.equals("StartRuSearch")){
                    user.setStatus("RuSearching");
                }
                if(message.equals("StopRuSearch")){
                    user.setStatus("Inactive");
                }
                if(message.equals("StartEnSearch")){
                    user.setStatus("EnSearching");
                }
                if(message.equals("StopEnSearch")){
                    user.setStatus("Inactive");
                }
                if(message.equals("StopSearchWaiter")){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("/SearchWaiter");
    }
}
