package sample.dataclasses;

public class RuExchanger extends Thread {

    public RuExchanger(){
        start();
    }

    @Override
    public void run(){
        System.out.println(this.getName());
        for(int i = 0; i < 10; i ++){
            System.out.println(i);
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
