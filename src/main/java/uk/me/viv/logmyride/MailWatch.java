package uk.me.viv.logmyride;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
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

public class MailWatch implements Runnable {

    private final String protocol;
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final BlockingQueue queue;

    public MailWatch(String protocol, String host, String port, String username, String password, BlockingQueue queue) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.queue = queue;
    }

    @Override
    public void run() {
        this.getNewEmails();
    }

    private Properties getServerProperties() {
        Properties properties = new Properties();
        properties.put(String.format("mail.%s.host", this.protocol), this.host);
        properties.put(String.format("mail.%s.port", this.protocol), this.port);
        properties.setProperty(String.format("mail.%s.socketFactory.class", this.protocol), "javax.net.ssl.SSLSocketFactory");
        properties.setProperty(String.format("mail.%s.socketFactory.fallback", this.protocol), "false");
        properties.setProperty(String.format("mail.%s.socketFactory.port", this.protocol), String.valueOf(this.port));

        return properties;
    }

    private void getNewEmails() {
        Properties properties = getServerProperties();
        Session session = Session.getDefaultInstance(properties);

        try {
            Store store = session.getStore(this.protocol);
            store.connect(this.username, this.password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            int count = inbox.getMessageCount();
            System.out.println("Found " + count + " messages");
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

                                if (KMZFile.isKMZ(fileName)) {
                                    addToQueue(part, fileName);
                                }

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
            Logger.getLogger(MailWatch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MailWatch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addToQueue(MimeBodyPart attachment, String fileName) throws IOException, MessagingException {
        System.out.println("Saving " + fileName + " to temporary directory");
        attachment.saveFile("/tmp/" + fileName);

        FileInputStream tmpFile = new FileInputStream(new File("/tmp/" + fileName));

        KMZFile kmz = new KMZFile(tmpFile, fileName);

        System.out.println("Adding " + kmz + " to queue");
        this.queue.add(kmz);
    }
}