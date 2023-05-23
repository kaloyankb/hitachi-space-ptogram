package launchDateApplication;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class JavaMailUtil {
    public static void sendMail (String sender, String password, String recipient) throws MessagingException {
        System.out.println("Preparing to sent an email");
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.ssl.trust", "smtp-mail.outlook.com");
        properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.host", "smtp-mail.outlook.com");
        properties.put("mail.smtp.port", "587");


        String myEmail = sender;
        String pass = password;

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myEmail, pass);
            }
        });

        Message message = prepareMessage(session, myEmail, recipient);
        Transport.send(message);
        System.out.println("Email sent successfully!");
    }

    private static Message prepareMessage(Session session, String myEmail, String recipient) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject("Weather Report");
            message.setText("Hey There, \n This is the weather report!\n Greetings!");


            BodyPart messageBodyPart1 = new MimeBodyPart();
            messageBodyPart1.setText("This is message body");
            MimeBodyPart messageBodyPart2 = new MimeBodyPart();

            String filename = ".\\new_CSV_files\\WeatherReport.csv";
            DataSource source = new FileDataSource(filename);
            messageBodyPart2.setDataHandler(new DataHandler(source));
            messageBodyPart2.setFileName(filename);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart1);
            multipart.addBodyPart(messageBodyPart2);

            message.setContent(multipart);

            return message;
        } catch (Exception ex) {
            System.out.println("Error while preparing a message for the email");
            ex.printStackTrace();
        }
        return null;
    }
}
