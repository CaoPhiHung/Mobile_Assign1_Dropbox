package com.example.assigment1_dropbox;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;



import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class CheckVerification extends Activity {

	private Button Ok;
	private Button Exit;
	private EditText input;
	private TextView error;
	private String a;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_check_verification);
		input = (EditText) findViewById(R.id.etStep);
		error = (TextView) findViewById(R.id.error);
		
		
		Ok = (Button) findViewById(R.id.okStep);
		 a = generate();
		sendMail("caophihung8392@gmail.com", "Vertificated Code", a);
		Ok.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String b = input.getText().toString();
				
				if(a.equalsIgnoreCase(b)){
					
					Intent t = new Intent(CheckVerification.this,MainActivity.class);
					startActivity(t);
					
				}else{
					error.setText("Wrong code");
				}
				
			}
		});
		
		Exit = (Button) findViewById(R.id.cancelStep);
		Exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.check_verification, menu);
		
		
		
		
		
		return true;
	}
	
	public String generate(){
		String alphabet = 
				new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
				int n = alphabet.length();
				String result = new String(); 
				Random r = new Random();
				for (int i=0; i<5; i++){
				result = result + alphabet.charAt(r.nextInt(n));
				}
				return result;
	}
	
	
	//Sent email
	private Session createSessionObject() {
	    Properties properties = new Properties();
	    properties.put("mail.smtp.auth", "true");
	    properties.put("mail.smtp.starttls.enable", "true");
	    properties.put("mail.smtp.host", "smtp.gmail.com");
	    properties.put("mail.smtp.port", "587");
	 
	    return Session.getInstance(properties, new javax.mail.Authenticator() {
	        protected PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication("caophihung8392@gmail.com", "Rmit1992");
	        }
	    });
	}
	
	private Message createMessage(String email, String subject, String messageBody, Session session) throws MessagingException, UnsupportedEncodingException {
	    Message message = new MimeMessage(session);
	    message.setFrom(new InternetAddress("caophihung8392@gmail.com", "Rmit1992"));
	    message.addRecipient(Message.RecipientType.TO, new InternetAddress(email, email));
	    message.setSubject(subject);
	    message.setText(messageBody);
	    return message;
	}
	
	
	private class SendMailTask extends AsyncTask<Message, Void, Void> {
	    private ProgressDialog progressDialog;
	 
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        progressDialog = ProgressDialog.show(CheckVerification.this, "Please wait", "Sending mail", true, false);
	    }
	 
	    @Override
	    protected void onPostExecute(Void aVoid) {
	        super.onPostExecute(aVoid);
	        progressDialog.dismiss();
	    }
	 
	    @Override
	    protected Void doInBackground(Message... messages) {
	        try {
	            Transport.send(messages[0]);
	        } catch (MessagingException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	}
	
	private void sendMail(String email, String subject, String messageBody) {
	    Session session = createSessionObject();
	 
	    try {
	        Message message = createMessage(email, subject, messageBody, session);
	        new SendMailTask().execute(message);
	    } catch (AddressException e) {
	        e.printStackTrace();
	    } catch (MessagingException e) {
	        e.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    }
	}

}
