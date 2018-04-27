import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.util.regex.*;

public class ChatUPD extends JFrame {  //JFrame наследование от него позволяет делать приложение оконным
    private JTextArea taMain; //текстовое окно
    private JTextField tfMsg; //поле для ввода

    private final String FRM_TITLE = "Our Tiny Chat";
    private final int FRM_LOC_X = 200;
    private final int FRM_LOC_Y = 200;
    private final int FRM_WIDTH = 400;
    private final int FRM_HEIGHT = 400;

    private final int PORT = 9876;

    private final String IP_BROADCAST = "192.168.0.255"; // ??????????????? InetAddres.getLocalHost()

    private class thdReceiver extends Thread{ //создание класса-потока (наследующего от обобщённого потока)
        @Override
        public void start(){
            super.start();
            try {
                customize();
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        private void customize() throws Exception {
            DatagramSocket receiveSocket = new DatagramSocket(PORT);
            Pattern regex = Pattern.compile("[\u0020-\uFFFF]");  //регулярка для отсечения лишних нулов и невидимых знаков. Нужно разбираться в выбранной кодировке
            while (true){
                byte[] receiveData = new byte[2048];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                receiveSocket.receive(receivePacket);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String sentence = new String(receivePacket.getData());
                Matcher m = regex.matcher(sentence); //выявляет и выдает все символы

                taMain.append(IPAddress.toString() + ":" + port + ": ");
                while (m.find())
                    taMain.append(sentence.substring(m.start(), m.end()));
                taMain.append("\r\n"); //перевод каретки
                taMain.setCaretPosition(taMain.getText().length());
            }
        }
    }

    private void btnSend_Handler() throws Exception {
        DatagramSocket sendSoket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(IP_BROADCAST);
        byte[] sendData;
        String sentence = tfMsg.getText();
        tfMsg.setText("");
        sendData = sentence.getBytes("UTF-8");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, PORT);
        sendSoket.send(sendPacket);
    }

    private void frameDraw(JFrame frame){
         tfMsg = new JTextField();
         taMain = new JTextArea(FRM_HEIGHT/19, 50);
         JScrollPane spMain = new JScrollPane(taMain);
         spMain.setLocation(0,0);
         taMain.setLineWrap(true);
         taMain.setEditable(false);

         JButton btnSend = new JButton();
         btnSend.setText("Send");
         btnSend.setToolTipText("Broadcast a massage");
         btnSend.addActionListener(e -> {           //лямбда, как сделать без нее, см в первой версии
             try {
                 btnSend_Handler();
             }catch (Exception ex) {
                 ex.printStackTrace();
             }
         });
            // задаем параметры окна:
         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         frame.setTitle(FRM_TITLE);
         frame.setLocation(FRM_LOC_X, FRM_LOC_Y);
         frame.setSize(FRM_WIDTH, FRM_HEIGHT);
         frame.setResizable(false);
         frame.getContentPane().add(BorderLayout.NORTH, spMain);
         frame.getContentPane().add(BorderLayout.CENTER, tfMsg);
         frame.getContentPane().add(BorderLayout.EAST, btnSend);
         frame.setVisible(true);
    }

    private void antistatic (){     // является методом класса ChatUPD
        //System.out.println("Hello world!!");  шняжка на проверку работоспособности
        frameDraw(new ChatUPD()); //метод для отрисовки. метод класса вызывается из другого метода этого класса
        new thdReceiver().start(); //пытаемся запустить поток, должен быть вне статического окружения
    }

    public static void main (String[] args){
        new ChatUPD().antistatic(); // создаём экземпляр класса ChatUPD и вызываем его метод antistatic
    }
}