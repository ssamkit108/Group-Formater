/**
 * 
 */
package com.dal.catmeclone.notification;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.dal.catmeclone.courses.Course;
import com.dal.catmeclone.model.User;

/**
 * @author Mayank
 *
 */
@Service
public class NotificationServiceImpl implements NotificationService {
	
	final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
	
	@Value("${from.email}")
	private String fromgmail;
	
	@Value("${from.password}")
	private String fromPassword;

	@Override
	public void sendNotificationToNewuser(User user, Course course) {
		// TODO Auto-generated method stub

		String subject = "Account Created - Your Acount Credential ";
		String body ="Hi "+user.getFirstName()+","+
				"\n\n" +
				"Your Acount has been created sucessfully. You have also been enrolled to subject: "+course.getCourseID()+
				"\nPlease find below your login credential: "+
				"\n\n\n"+
				"\nUsername: "+user.getBannerId()+
				"\nPassword: "+user.getPassword()+
				"\n\nYou are most welcomed to be a part of this organisation."+
				"\n\nBest Regards,\nCSCI5708-Grp12";
		send(fromgmail,fromPassword,user.getEmail(),subject,body);  

	}
	
	private void send(String from,String password,String to,String sub,String msg){  
	        
			//Get properties object    		
	        Properties props = new Properties();    
	        props.put("mail.smtp.host", "smtp.gmail.com");    
	        props.put("mail.smtp.socketFactory.port", "465");    
	        props.put("mail.smtp.socketFactory.class",    
	                  "javax.net.ssl.SSLSocketFactory");    
	        props.put("mail.smtp.auth", "true");    
	        props.put("mail.smtp.port", "465");    
	        props.put("mail.smtp.starttls.enable","true");
	        
	        //get Session   
	        Session session = Session.getInstance(props,    
	         new javax.mail.Authenticator() {    
	         protected PasswordAuthentication getPasswordAuthentication() {    
	         return new PasswordAuthentication(from,password);  
	         }    
	        });    
        
	        //compose message 
	           
	         MimeMessage message = new MimeMessage(session);    
	         try {
				message.addRecipient(javax.mail.Message.RecipientType.TO,new InternetAddress(to));
			  
				message.setSubject(sub);    
				message.setText(msg);    
				//send message  
				Transport.send(message);  
	         } catch (MessagingException e) {
					// TODO Auto-generated catch block
					logger.error("Error in sending email");
			 }  
	}
	        

}
