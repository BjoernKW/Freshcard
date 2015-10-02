package com.freshcard.backend.util;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.freshcard.backend.model.Organization;
import com.freshcard.backend.model.User;
import ezvcard.VCard;
import ezvcard.property.Address;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

import java.io.*;

/**
 * Created by willy on 22.03.15.
 */
public class SVGUtil {
    private static String fileSuffix = "png";

    public static void publishAsPNG(Organization organization, User user, VCard vCard) throws TranscoderException, IOException {
        String svgResult = organization.getTemplateAsSVG();

        if (vCard.getFormattedName() != null) {
            svgResult = svgResult.replace("NAME", vCard.getFormattedName().getValue());
        }
        if (vCard.getStructuredName() != null) {
            svgResult = svgResult.replace("NAME", vCard.getStructuredName().getGiven() + " " + vCard.getStructuredName().getFamily());
        }
        if (vCard.getAddresses().size() > 0) {
            Address address = vCard.getAddresses().get(0);
            if (address.getRegion() != null) {
                svgResult = svgResult.replace("CITY", address.getRegion());
            } else {
                svgResult = svgResult.replace("CITY", "");
            }
            svgResult = svgResult.replace("STREET_ADDRESS", address.getStreetAddress());
            svgResult = svgResult.replace("POSTAL_CODE", address.getPostalCode());
        } else {
            svgResult = svgResult.replace("CITY", "");
            svgResult = svgResult.replace("STREET_ADDRESS", "");
            svgResult = svgResult.replace("POSTAL_CODE", "");
        }
        if (vCard.getEmails().size() > 0) {
            svgResult = svgResult.replace("EMAIL_ADDRESS", vCard.getEmails().get(0).getValue());
        } else {
            svgResult = svgResult.replace("EMAIL_ADDRESS", "");
        }
        if (vCard.getTelephoneNumbers().size() > 0) {
            svgResult = svgResult.replace("PHONE_NUMBER", vCard.getTelephoneNumbers().get(0).getText());
        } else {
            svgResult = svgResult.replace("PHONE_NUMBER", "");
        }
        if (vCard.getUrls().size() > 0) {
            svgResult = svgResult.replace("WEBSITE", vCard.getUrls().get(0).getValue());
        } else {
            svgResult = svgResult.replace("WEBSITE", "");
        }

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory svgDocumentFactory = new SAXSVGDocumentFactory(parser);
        Document document = svgDocumentFactory.createSVGDocument(organization.getUrl() + "/" + user.getHashCode(), new StringReader(svgResult));

        PNGTranscoder transcoder = new PNGTranscoder();

        TranscoderInput input = new TranscoderInput(document);
        String newFilename = "business-card-" + organization.getHashCode() + "-" + user.getHashCode();
        File tempFile = File.createTempFile(newFilename + "-" + System.currentTimeMillis(), fileSuffix);
        OutputStream ostream = new FileOutputStream(tempFile);
        TranscoderOutput output = new TranscoderOutput(ostream);

        transcoder.transcode(input, output);
        ostream.flush();
        ostream.close();

        AmazonS3 s3client = new AmazonS3Client(new EnvironmentVariableCredentialsProvider());
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                System.getenv().get("S3_BUCKET_NAME"),
                "organizations/" + newFilename + "." + fileSuffix,
                tempFile
        );
        putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/png");
        putObjectRequest.withMetadata(metadata);
        s3client.putObject(putObjectRequest);
    }
}
