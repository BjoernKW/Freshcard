package com.freshcard.backend.util;

import com.freshcard.backend.model.User;
import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.property.StructuredName;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by willy on 07.09.14.
 */
public class Mailer {
    private static final Logger logger = Logger.getLogger(Mailer.class);

    private Properties mailerProperties;

    private MessageSource messageSource;

    public void setMailerProperties(Properties mailerProperties) {
        this.mailerProperties = mailerProperties;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void sendWelcomeMail(User user) {
        sendMail(
                user,
                null,
                "welcome.subject",
                "welcome.body",
                new Object[] { },
                new Object[] {
                        mailerProperties.getProperty("freshcard.baseURL") + "public/confirmAccount/"
                                + user.getConfirmationHashCode()
                },
                null,
                "welcome"
        );
    }

    public void sendPasswordResetMail(User user) {
        sendMail(
                user,
                null,
                "passwordReset.subject",
                "passwordReset.body",
                new Object[] { },
                new Object[] {
                        mailerProperties.getProperty("freshcard.baseURL") + "public/changePassword/"
                                + user.getConfirmationHashCode()
                },
                null,
                null
        );
    }

    public void sendPasswordResetSuccessfulMail(User user) {
        sendMail(
                user,
                null,
                "passwordResetSuccessful.subject",
                "passwordResetSuccessful.body",
                new Object[] { },
                new Object[] { },
                null,
                null
        );
    }

    public void sendEMailConnectionRequest(User user, VCard vCard, String eMailAddress, String connectionHashCode) {
        String phoneNumber = "";
        String name = "";
        File vCardFile = null;

        if (vCard != null) {
            if (vCard.getTelephoneNumbers() != null && vCard.getTelephoneNumbers().size() > 0) {
                phoneNumber = vCard.getTelephoneNumbers().get(0).getText();
            }

            if (vCard.getFormattedName() != null) {
                name = vCard.getFormattedName().getValue();
            } else {
                if (vCard.getStructuredName() != null) {
                    StructuredName structuredName = vCard.getStructuredName();
                    name =
                            (structuredName.getGiven() != null ? structuredName.getGiven() + " " : "")
                                    + (structuredName.getFamily() != null ? structuredName.getFamily() : "");
                } else {
                    name = user.getUsername();
                }
            }

            try {
                vCardFile = File.createTempFile(user.getUsername() + ".vCard.tmp." + System.currentTimeMillis(), ".vcf");
                Ezvcard.write(vCard).version(VCardVersion.V3_0).go(vCardFile);
            } catch (IOException e) {
                logger.debug(e);
            }
        }

        String firstName = "";
        if (name != null) {
            String[] names = name.split(" ");
            if (names.length > 0) {
                firstName = names[0];
            }
        }

        sendMail(
                user,
                eMailAddress,
                "eMailConnectionRequest.subject",
                "eMailConnectionRequest.body",
                new Object[]{ name },
                new Object[]{
                        user.getProfilePicturePath(),
                        name,
                        user.getUsername(),
                        phoneNumber,
                        mailerProperties.getProperty("freshcard.baseURL") + "public/profile/"
                                + user.getHashCode()
                                + "/" + connectionHashCode,
                        user.getCustomSignature() != null ? user.getCustomSignature() : "",
                        firstName
                },
                vCardFile,
                "connectionRequest"
        );
    }

    private void sendMail(
            final User user,
            final String recipientEMailAddress,
            final String subjectMessage,
            final String bodyMessage,
            final Object[] subjectVariables,
            final Object[] bodyVariables,
            final File vCard,
            final String templateName
    ) {
        Thread mailThread = new Thread() {
            public void run() {
                Locale locale = new Locale(recipientEMailAddress == null ? user.getPreferredLanguage() : "en");

                SendGrid sendgrid = new SendGrid(mailerProperties.getProperty("sendgrid.username"), mailerProperties.getProperty("sendgrid.password"));

                SendGrid.Email email = new SendGrid.Email();
                email.addTo(recipientEMailAddress != null ? recipientEMailAddress : user.getUsername());
                email.setFrom(mailerProperties.getProperty("freshcard.from"));
                email.setSubject(messageSource.getMessage(subjectMessage, subjectVariables, locale));
                email.setText(
                        messageSource.getMessage(
                                bodyMessage,
                                bodyVariables,
                                locale
                        )
                );

                String emailHTML = "";
                try {
                    InputStream htmlTemplateInputStream = this.getClass().getResource("/email/" + templateName + "_" + locale.getLanguage() + ".html").openStream();
                    emailHTML = IOUtils.toString(htmlTemplateInputStream, Encoding.charset);
                    for (int i = 0; i < bodyVariables.length; i++) {
                        emailHTML = emailHTML.replaceAll("\\{" + i + "\\}", bodyVariables[i] != null ? bodyVariables[i].toString() : "");
                    }
                    email.setHtml(emailHTML);
                } catch (FileNotFoundException e) {
                    logger.debug(e.getMessage());
                } catch (IOException e) {
                    logger.debug(e.getMessage());
                } catch (NullPointerException e) {
                    logger.debug(e.getMessage());
                }

                if (vCard != null) {
                    try {
                        email.addAttachment("vcard.vcf", vCard);
                    } catch (IOException e) {
                        logger.debug(e.getMessage());
                    }
                }

                try {
                    sendgrid.send(email);
                } catch (SendGridException e) {
                    logger.debug(e.getMessage());
                }
            }
        };
        mailThread.run();
    }
}
