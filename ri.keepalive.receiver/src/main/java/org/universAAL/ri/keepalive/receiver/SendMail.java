/*
	Copyright 2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.ri.keepalive.receiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.universAAL.middleware.container.utils.LogUtils;

public class SendMail {

	public static boolean send(String tenant, long now) {
		Properties props;
		try {
			props = new Properties();
			InputStream in = new FileInputStream(
					new File(Activator.context.getConfigHome(), "keepalive.mail.properties"));
			props.load(in);
			in.close();
		} catch (Exception e) {
			LogUtils.logError(Activator.context, SendMail.class, "send",
					"Email properties not available "
							+ "(file keepalive.mail.properties required in config folder ri.keepalive),"
							+ " email warning is disabled: " + e);
			e.printStackTrace();
			return false;
		}

		String auth = props.getProperty("org.universAAL.ri.keepalive.mail.auth", "true");
		String ttls = props.getProperty("org.universAAL.ri.keepalive.mail.ttls", "true");
		String host = props.getProperty("org.universAAL.ri.keepalive.mail.host");
		String port = props.getProperty("org.universAAL.ri.keepalive.mail.port");
		final String user = props.getProperty("org.universAAL.ri.keepalive.mail.user");
		final String pass = props.getProperty("org.universAAL.ri.keepalive.mail.pass");
		String from = props.getProperty("org.universAAL.ri.keepalive.mail.from");
		String to = props.getProperty("org.universAAL.ri.keepalive.mail.to");

		if (host == null | port == null | user == null | pass == null | from == null | to == null) {
			return false;
		}

		Properties mailprops = new Properties();
		mailprops.put("mail.smtp.auth", auth);
		mailprops.put("mail.smtp.starttls.enable", ttls);
		mailprops.put("mail.smtp.host", host);
		mailprops.put("mail.smtp.port", port);

		Session session = Session.getInstance(mailprops, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, pass);
			}
		});

		try {
			Date nowis = new Date(now);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject("[universAAL] Tenant " + tenant + " stopped reporting");
			message.setText("The universAAL remote tenant with id: " + tenant
					+ " is no longer reporting its system status signal. \n"
					+ "This tenant had reported correctly for the last time on " + nowis.toString() + " \n"
					+ "\nThis indicates that now the system is either shut down or is having some issues, "
					+ "and may require corrective action.\n\n" + "This is an automatic message, do not reply.");
			Transport.send(message);
		} catch (MessagingException e) {
			LogUtils.logError(Activator.context, SendMail.class, "send", "Could not send the warning email: " + e);
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
