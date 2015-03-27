package uk.me.viv.logmyride;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

public class MailWatch {

    private Properties getServerProperties(String protocol, String host, String port) {

        Properties properties = new Properties();
        properties.put(String.format("mail.%s.host", protocol), host);
        properties.put(String.format("mail.%s.port", protocol), port);
        properties.setProperty(String.format("mail.%s.socketFactory.class", protocol), "javax.net.ssl.SSLSocketFactory");
        properties.setProperty(String.format("mail.%s.socketFactory.fallback", protocol), "false");
        properties.setProperty(String.format("mail.%s.socketFactory.port", protocol), String.valueOf(port));

        return properties;
    }

    public void getNewEmails(String protocol, String host, String port, String userName, String password) {

        Properties properties = getServerProperties(protocol, host, port);
        Session session = Session.getDefaultInstance(properties);

        try {
            Store store = session.getStore(protocol);
            store.connect(userName, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            int count = inbox.getMessageCount();
            Message[] messages = inbox.getMessages(1, count);
            for (Message message : messages) {
                if (!message.getFlags().contains(Flags.Flag.SEEN)) {
                    
                    String attachFiles = "";
                    String messageContent = "";
                    String contentType = message.getContentType();

                    if (contentType.contains("multipart")) {

                        System.out.println("Multipart message");

                        Multipart multiPart = (Multipart) message.getContent();
                        int numberOfParts = multiPart.getCount();
                        for (int partCount = 0; partCount < numberOfParts; partCount++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                // this part is an attachment
                                String fileName = part.getFileName();
                                attachFiles += fileName + ", ";
                                part.saveFile("/tmp/" + fileName);

                            } else {
                                // could be the message content
                                messageContent = part.getContent().toString();
                            }
                        }

                        if (attachFiles.length() > 1) {
                            attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                        }
                    } else if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                        Object content = message.getContent();
                        if (content != null) {
                            messageContent = content.toString();
                        }
                    }

                    Address[] fromAddresses = message.getFrom();
                    System.out.println("- From: " + fromAddresses[0].toString());
                    System.out.println("- Sent Date:" + message.getSentDate().toString());
                    System.out.println("- Subject: " + message.getSubject());
                    System.out.println("- Content:" + messageContent);
                    System.out.println("- Attachments:" + attachFiles);

                    System.out.println("Marking mail as unread while testing...");
                    message.setFlag(Flags.Flag.SEEN, false);
                    System.out.println("");
                }
            }

            inbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for protocol: " + protocol);
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(MailWatch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}