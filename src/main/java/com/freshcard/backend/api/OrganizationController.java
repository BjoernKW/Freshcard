package com.freshcard.backend.api;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.freshcard.backend.http.BadRequestException;
import com.freshcard.backend.http.InternalServerErrorException;
import com.freshcard.backend.model.Contact;
import com.freshcard.backend.model.Organization;
import com.freshcard.backend.model.Status;
import com.freshcard.backend.model.User;
import com.freshcard.backend.model.dao.OrganizationDAO;
import com.freshcard.backend.security.TokenUtil;
import com.freshcard.backend.security.UnauthorizedException;
import com.freshcard.backend.util.SVGUtil;
import com.freshcard.backend.util.UploadHelper;
import com.wordnik.swagger.annotations.*;
import ezvcard.Ezvcard;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "organizations", description = "organizations")
@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
    private static final Logger logger = Logger.getLogger(OrganizationController.class);

    @Autowired
    private OrganizationDAO organizationDAO;

    @Autowired
    private UploadHelper uploadHelper;

    private String s3URL = System.getenv().get("S3_ENDPOINT") + System.getenv().get("S3_BUCKET_NAME") + "/organizations";

    @ApiResponses(value = { @ApiResponse(code = 401, message = "Unauthorized") } )
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler( { UnauthorizedException.class } )
    public void handleUnauthorizedAccess() {
    }

    @ApiResponses(value = { @ApiResponse(code = 400, message = "Bad request") } )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler( { BadRequestException.class } )
    public void handleBadRequestError() {
    }

    @ApiOperation(value = "Find organization by username")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/findByUsername",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody List<Organization> findByUsername(
            @RequestBody User user,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return organizationDAO.findByUsername(user.getUsername());
    }

    @ApiOperation(value = "Find organization by ID")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
	public @ResponseBody
    Organization find(@PathVariable Integer id, HttpServletRequest httpRequest) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!isAuthorized(authenticatedUsername, id)) {
            throw new UnauthorizedException("Unauthorized");
        }

        return organizationDAO.find(id);
	}

    @ApiOperation(value = "Create organization")
    @Authorization(value = "token-based")
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    Organization create(
            @RequestBody Organization organization
    ) {
        Number id = organizationDAO.insert(organization);

        return organizationDAO.find(new Long(id.longValue()).intValue());
    }

    @ApiOperation(value = "Update organization")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/{id}",
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            method = RequestMethod.PUT)
    public @ResponseBody  Organization update(
            @PathVariable Integer id,
            @RequestBody Organization organization,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!isAuthorized(authenticatedUsername, id)) {
            throw new UnauthorizedException("Unauthorized");
        }

        organizationDAO.update(id, organization);

        return organization;
    }

    @ApiOperation(value = "Delete organization")
    @Authorization(value = "token-based")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Status delete(@PathVariable Integer id, HttpServletRequest httpRequest) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!isAuthorized(authenticatedUsername, id)) {
            throw new UnauthorizedException("Unauthorized");
        }

        organizationDAO.delete(id);

        return new Status("OK");
    }

    @ApiOperation(value = "Upload a organization's template background")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/uploadTemplateImage/{id}",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8" }
    )
    public @ResponseBody
    Map<String, Object> uploadTemplateImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest
    ) throws InternalServerErrorException, BadRequestException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!isAuthorized(authenticatedUsername, id)) {
            throw new UnauthorizedException("Unauthorized");
        }

        Organization organization = organizationDAO.find(id);
        Map<String, Object> jsonObject = new HashMap<String, Object>();

        uploadImage("template", file, organization, jsonObject);

        return jsonObject;
    }

    @ApiOperation(value = "Upload a organization's logo")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/uploadLogoImage/{id}",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8" }
    )
    public @ResponseBody
    Map<String, Object> uploadLogoImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest
    ) throws InternalServerErrorException, BadRequestException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!isAuthorized(authenticatedUsername, id)) {
            throw new UnauthorizedException("Unauthorized");
        }

        Organization organization = organizationDAO.find(id);
        Map<String, Object> jsonObject = new HashMap<String, Object>();

        uploadImage("logo", file, organization, jsonObject);

        return jsonObject;
    }

    @ApiOperation(value = "Publish current template to organization's users")
    @RequestMapping(
            value = "/publishTemplate",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status publishTemplate(
            @RequestBody Organization organization,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!isAuthorized(authenticatedUsername, organization.getId())) {
            throw new UnauthorizedException("Unauthorized");
        }

        try {
            Organization organizationToPublish = organizationDAO.find(organization.getId());
            for (User user : organizationToPublish.getUsers()) {
                if (user.getOwnContacts().size() > 0) {
                    Contact mainContact = user.getOwnContacts().get(0);
                    if (mainContact.getVcard() != null) {
                        Ezvcard.ParserChainJsonString vCards = Ezvcard.parseJson(mainContact.getVcard());
                        if (vCards != null && vCards.first() != null) {
                            SVGUtil.publishAsPNG(organizationToPublish, user, vCards.first());
                        }
                    }
                }
            }
        } catch (TranscoderException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return new Status("OK");
    }

    private Boolean isAuthorized(String username, Integer organizationId) {
        Boolean isAuthorized = false;
        List<Organization> organizations = organizationDAO.findByUsername(username);
        for (Organization organization : organizations) {
            if (organization.getId() == organizationId) {
                isAuthorized = true;
            }
        }

        return isAuthorized;
    }

    private void uploadImage(String prefix, MultipartFile file, Organization organization, Map<String, Object> jsonObject) throws InternalServerErrorException, BadRequestException {
        String filename = file.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        if (!file.isEmpty() && uploadHelper.isValidImageSuffix(suffix)) {
            try {
                String newFilename = "organization-" + prefix + "-image-" + organization.getHashCode() + System.currentTimeMillis();
                File tempFile = File.createTempFile(newFilename, suffix);
                file.transferTo(tempFile);

                AmazonS3 s3client = new AmazonS3Client(new EnvironmentVariableCredentialsProvider());
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        System.getenv().get("S3_BUCKET_NAME"),
                        "organizations/" + newFilename + "." + suffix,
                        tempFile
                );
                putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                putObjectRequest.withMetadata(metadata);
                PutObjectResult putObjectResult = s3client.putObject(putObjectRequest);

                String imagePath = s3URL + "/" + newFilename + "." + suffix;

                if (prefix.equals("logo")) {
                    organization.setLogoImagePath(imagePath);
                } else {
                    organization.setTemplateImagePath(imagePath);
                }
                organizationDAO.update(organization.getId(), organization);

                jsonObject.put("imagePath", imagePath);
                jsonObject.put("checksum", putObjectResult.getContentMd5());
            } catch (IOException e) {
                logger.error(e.getMessage());

                throw new InternalServerErrorException("Couldn't create temporary file.");
            } catch (AmazonServiceException e) {
                logger.error(e.getMessage());

                throw new InternalServerErrorException("Couldn't upload file.");
            }
        } else {
            throw new BadRequestException("Invalid image");
        }
    }
}