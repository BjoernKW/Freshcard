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
import com.freshcard.backend.http.ConflictException;
import com.freshcard.backend.http.InternalServerErrorException;
import com.freshcard.backend.model.*;
import com.freshcard.backend.model.dao.ContactDAO;
import com.freshcard.backend.model.dao.OrganizationDAO;
import com.freshcard.backend.model.dao.UserDAO;
import com.freshcard.backend.security.*;
import com.freshcard.backend.util.Mailer;
import com.freshcard.backend.util.UploadHelper;
import com.freshcard.backend.util.UserManagementHelper;
import com.wordnik.swagger.annotations.*;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "users", description = "users")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private static final Logger logger = Logger.getLogger(UserController.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private OrganizationDAO organizationDAO;

    @Autowired
    private ContactDAO contactDAO;

    @Autowired
    private Mailer mailer;

    @Autowired
    private UploadHelper uploadHelper;

    @Autowired
    private UserManagementHelper userManagementHelper;

    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("oAuthAuthenticationManager")
    private AuthenticationManager oAuthAuthenticationManager;

    private Boolean sendMail = true;

    private Boolean skipJCard = false;

    private String s3URL = System.getenv().get("S3_ENDPOINT") + System.getenv().get("S3_BUCKET_NAME") + "/users";

    @ApiResponses(value = { @ApiResponse(code = 401, message = "Unauthorized") } )
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler( { UnauthorizedException.class } )
    public void handleUnauthorizedAccess() {
    }

    @ApiResponses(value = { @ApiResponse(code = 403, message = "Forbidden") } )
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler( { ForbiddenException.class } )
    public void handleForbiddenResource() {
    }

    @ApiResponses(value = { @ApiResponse(code = 400, message = "Bad request") } )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler( { BadRequestException.class } )
    public void handleBadRequestError() {
    }

    @ApiResponses(value = { @ApiResponse(code = 409, message = "Conflict") } )
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler( { ConflictException.class } )
    public void handleConflictError() {
    }

    @ApiResponses(value = { @ApiResponse(code = 500, message = "Internal server error") } )
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler( { InternalServerErrorException.class } )
    public void handleInternalServerError() {
    }

    @ApiOperation(value = "Get user")
    @Authorization(value = "token-based")
    @RequestMapping(
            method = RequestMethod.GET,
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    UserTransfer get() throws UnauthorizedException {
        UserDetails userDetails = getUserDetails();

        User user = userDAO.getUserInfoByUsername(userDetails.getUsername());
        user.setOwnContacts(contactDAO.getUserContacts(user.getId(), user.getCurrentOrganization().getId()));

        UserTransfer userTransfer = new UserTransfer(
                user.getId(),
                userDetails.getUsername(),
                this.createRoleMap(userDetails),
                user.getOrganizations(),
                user.getCurrentOrganization().getId(),
                user.getOwnContacts(),
                user.getHashCode(),
                user.getConfirmed(),
                user.getCustomSignature(),
                user.getProfilePicturePath(),
                user.getCurrentOrganization().getSearchPublicKey(),
                user.getCurrentOrganization().getSearchPublicKeyForCoWorkers()
        );

        return userTransfer;
    }

    @ApiOperation(value = "Find user by ID")
    @Authorization(value = "token-based")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
	public @ResponseBody
    User find(@PathVariable Integer id, HttpServletRequest httpRequest) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(id);
        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return user;
	}

    @ApiOperation(value = "Find user by hash code")
    @RequestMapping(value = "/findByHashCode", method = RequestMethod.POST, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    User findByHashCode(@RequestBody String hashCode) {
        User user = userDAO.findByHashCode(hashCode);
        user.setOwnContacts(contactDAO.getUserContacts(user.getId(), user.getCurrentOrganization().getId()));

        return user;
    }

    @ApiOperation(value = "Confirm account")
    @RequestMapping(value = "/confirmAccount", method = RequestMethod.POST, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    User confirmAccount(@RequestBody String hashCode) {
        User user = userDAO.findByConfirmationHashCode(hashCode);

        if (user != null) {
            user.setConfirmed(true);
            userDAO.update(user.getId(), user);
        }

        return user;
    }

    @ApiOperation(value = "Change password")
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    User changePassword(@RequestBody User user) {
        User resetUser = userDAO.findByConfirmationHashCode(user.getConfirmationHashCode());

        if (resetUser != null) {
            resetUser.setConfirmed(true);
            resetUser.setPassword(user.getPassword());

            userDAO.updatePassword(resetUser.getId(), resetUser);
        }

        if (sendMail) {
            mailer.sendPasswordResetSuccessfulMail(resetUser);
        }

        return resetUser;
    }

    @ApiOperation(value = "Find user by username")
    @RequestMapping(value = "/findByUsername", method = RequestMethod.POST, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    User findByUsername(@RequestBody String username, HttpServletRequest httpRequest) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!authenticatedUsername.equals(username)) {
            throw new UnauthorizedException("Unauthorized");
        }

        User user = userDAO.findByUsername(username);

        return user;
    }

    @ApiOperation(value = "Get user info")
    @RequestMapping(value = "/getUserInfo", method = RequestMethod.POST, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    User getUserInfo(@RequestBody User user, HttpServletRequest httpRequest) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);
        user.setUsername(user.getUsername().toLowerCase());

        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        User userInfo = userDAO.getUserInfo(user.getUsername());
        if (userInfo != null && userInfo.getCurrentOrganization() != null) {
            userInfo.setOwnContacts(contactDAO.getUserContacts(userInfo.getId(), userInfo.getCurrentOrganization().getId()));
        }

        return userInfo;
    }

    @ApiOperation(value = "Check by username if user exists")
    @RequestMapping(value = "/checkByUsername", method = RequestMethod.POST, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    Status checkByUsername(@RequestBody String username) throws ForbiddenException {
        User user = userDAO.findByUsername(username.toLowerCase()); // force lower-case username
        if (user != null && !user.getTemporaryUser()) {
            throw new ForbiddenException("Forbidden: User already exists.");
        }

        return new Status("OK");
    }

    @ApiOperation(value = "Check if a user exists")
    @RequestMapping(
            value = "/checkIfUserExists",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status checkIfUserExists(@RequestBody User user) throws ForbiddenException {
        User existingUser = userDAO.findByUsername(user.getUsername().toLowerCase()); // force lower-case username
        if (existingUser != null && !existingUser.getTemporaryUser()) {
            throw new ForbiddenException("Forbidden: User already exists.");
        }

        return new Status("OK");
    }

    @ApiOperation(value = "Get a users's vCards by hash code")
    @RequestMapping(value = "/getVCardsByHashCode/{hashCode}", method = RequestMethod.GET)
    public @ResponseBody
    String getVCardsByHashCode(
            @PathVariable String hashCode,
            HttpServletResponse response
    ) {
        response.setContentType("text/vcard; charset=utf-8");

        User user = userDAO.findByHashCode(hashCode);

        List<VCard> vCards = new ArrayList<VCard>();
        for (Contact contact : contactDAO.getUserContacts(user.getId(), user.getCurrentOrganization().getId())) {
            vCards.addAll(Ezvcard.parseJson(contact.getVcard()).all());
        }

        return Ezvcard.write(vCards).version(VCardVersion.V3_0).go();
    }

    @ApiOperation(value = "Create user")
    @Authorization(value = "token-based")
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    User create(
            @RequestBody User user
    ) {
        return userDAO.insert(user);
    }

    @ApiOperation(value = "Sign up user")
    @RequestMapping(
            value = "/signUp",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    TokenTransfer signUp(@RequestBody User user) throws InternalServerErrorException, ConflictException {
        user.setUsername(user.getUsername().toLowerCase()); // force lower-case username

        if (userDAO.findByUsername(user.getUsername()) != null) {
            throw new ConflictException("Conflict: User already exists.");
        }

        String plainTextPassword = user.getPassword();

        User signedUpUser = userDAO.findTemporaryUserByUsername(user.getUsername());
        if (signedUpUser != null) {
            signedUpUser.setPassword(plainTextPassword);
            userDAO.updatePassword(signedUpUser.getId(), signedUpUser);

            signedUpUser.setPreferredLanguage(user.getPreferredLanguage());
            signedUpUser.setTemporaryUser(false);
            userDAO.update(signedUpUser.getId(), signedUpUser);
        } else {
            signedUpUser = userManagementHelper.createUser(user);
            if (signedUpUser.getId() == null) {
                throw new InternalServerErrorException("Internal server error: User could not be created.");
            }
        }

        if (sendMail && !isOauthUser(user)) {
            mailer.sendWelcomeMail(signedUpUser);
        }

        return getTokenTransfer(user.getUsername(), plainTextPassword);
    }

    @ApiOperation(value = "Sign up existing user (through contact request)")
    @RequestMapping(
            value = "/signUpExistingUser",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    TokenTransfer signUpExistingUser(@RequestBody User user) throws InternalServerErrorException {
        String plainTextPassword = user.getPassword();

        User existingUser = userDAO.findByHashCode(user.getHashCode());
        if (existingUser == null) {
            throw new InternalServerErrorException("Internal server error: User does not exist.");
        }
        if (!existingUser.getTemporaryUser()) {
            throw new InternalServerErrorException("Internal server error: User is not temporary.");
        }

        existingUser.setPassword(plainTextPassword);
        userDAO.updatePassword(existingUser.getId(), existingUser);

        existingUser.setPreferredLanguage(user.getPreferredLanguage());
        existingUser.setTemporaryUser(false);
        userDAO.update(existingUser.getId(), existingUser);

        if (sendMail && !isOauthUser(user)) {
            mailer.sendWelcomeMail(existingUser);
        }

        return getTokenTransfer(user.getUsername(), plainTextPassword);
    }

    @ApiOperation(value = "Send password reset link")
    @RequestMapping(
            value = "/sendPasswordResetLink",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status sendPasswordResetLink (@RequestBody User user) {
        User existingUser = userDAO.findByUsername(user.getUsername());
        existingUser.setConfirmed(false);
        existingUser.setPreferredLanguage(user.getPreferredLanguage());
        existingUser = userDAO.update(existingUser.getId(), existingUser);

        if (sendMail) {
            mailer.sendPasswordResetMail(existingUser);
        }

        return new Status("OK");
    }

    @ApiOperation(value = "Connect via eMail")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/sendEMailConnectionRequest",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    User sendEMailConnectionRequestFromParameters(
            @RequestParam("username") String username,
            @RequestParam("recipientEMailAddress") String recipientEMailAddress,
            HttpServletRequest httpRequest
    ) throws InternalServerErrorException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User existingUser = userDAO.findByUsername(authenticatedUsername);

        if (!(authenticatedUsername.equals(username) || userManagementHelper.isUserAuthorizedForEMailAddress(existingUser, username))) {
            throw new UnauthorizedException("Unauthorized");
        }

        User newUser = sendEMailConnectionRequest(existingUser, recipientEMailAddress.toLowerCase()); // force lower-case username

        return newUser;
    }

    @ApiOperation(value = "Connect via eMail (using JSON parameters)")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/sendEMailConnectionRequestFromJSON",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    User sendEMailConnectionRequestFromJSON (
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws InternalServerErrorException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User existingUser = userDAO.findByUsername(authenticatedUsername);

        if (!(authenticatedUsername.equals(parameters.getUsername()) || userManagementHelper.isUserAuthorizedForEMailAddress(existingUser, parameters.getUsername()))) {
            throw new UnauthorizedException("Unauthorized");
        }

        User newUser = sendEMailConnectionRequest(existingUser, parameters.getRecipientEMailAddress().toLowerCase()); // force lower-case username

        return newUser;
    }

    @ApiOperation(value = "Add co-worker via eMail")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/addCoWorkerViaEMail",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    User addCoWorkerViaEMail (
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws InternalServerErrorException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User existingUser = userDAO.findByUsername(authenticatedUsername);

        if (!((authenticatedUsername.equals(parameters.getUsername()) || userManagementHelper.isUserAuthorizedForEMailAddress(existingUser, parameters.getUsername())) && userManagementHelper.isAdmin(existingUser.getUsername(), parameters.getOrganizationId()))) {
            throw new UnauthorizedException("Unauthorized");
        }

        User newUser = sendEMailToCoWorker(existingUser, parameters.getRecipientEMailAddress(), parameters.getOrganizationId());

        return newUser;
    }

    @ApiOperation(value = "Update user")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/{id}",
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            method = RequestMethod.PUT
    )
    public @ResponseBody Status update(
            @PathVariable Integer id,
            @RequestBody User user,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        userDAO.update(id, user);

        return new Status("OK");
    }

    @ApiOperation(value = "Update a user's password")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/updatePassword",
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            method = RequestMethod.POST
    )
    public @ResponseBody User updatePassword(
            @RequestBody User user,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User inputUser = userDAO.find(user.getId());
        if (inputUser != null) {
            if (!authenticatedUsername.equals(inputUser.getUsername())) {
                throw new UnauthorizedException("Unauthorized");
            }
        } else {
            throw new UnauthorizedException("Unauthorized");
        }

        User userToUpdate = userDAO.find(user.getId());

        if (userToUpdate != null) {
            userToUpdate.setPassword(user.getPassword());
            userToUpdate.setConfirmed(true);

            userDAO.updatePassword(userToUpdate.getId(), userToUpdate);
        }

        return userToUpdate;
    }

    @ApiOperation(value = "Update a user's username")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/updateUsername",
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            method = RequestMethod.POST
    )
    public @ResponseBody User updateUsername(
            @RequestBody User user,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User inputUser = userDAO.find(user.getId());
        if (inputUser != null) {
            if (!authenticatedUsername.equals(inputUser.getUsername())) {
                throw new UnauthorizedException("Unauthorized");
            }
        } else {
            throw new UnauthorizedException("Unauthorized");
        }

        User userToUpdate = userDAO.find(user.getId());

        if (userToUpdate != null) {
            User updatedUser = userManagementHelper.updateUsername(userToUpdate, user.getNewUsername());

            if (sendMail && !isOauthUser(user)) {
                mailer.sendWelcomeMail(updatedUser);
            }
        }

        return userToUpdate;
    }

    @ApiOperation(value = "Set current organization for user")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/updateCurrentOrganization",
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            method = RequestMethod.POST
    )
    public @ResponseBody Organization updateCurrentOrganization(
            @RequestBody User currentUser,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!authenticatedUsername.equals(currentUser.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        User user = userDAO.find(currentUser.getId());
        user.setCurrentOrganization(organizationDAO.find(currentUser.getCurrentOrganization().getId()));

        userDAO.updateCurrentOrganization(currentUser.getId(), user);

        return organizationDAO.find(currentUser.getCurrentOrganization().getId());
    }

    @ApiOperation(value = "Delete user")
    @Authorization(value = "token-based")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Status delete(@PathVariable Integer id, HttpServletRequest httpRequest) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        Boolean isSuperAdmin = false;
        Boolean isAdmin = false;
        UserDetails userDetails = getUserDetails();
        User authenticatedUser = userDAO.getUserInfoByUsername(userDetails.getUsername());
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                isAdmin = true;
            }
            if (authority.getAuthority().equals("ROLE_SUPER_ADMIN")) {
                isSuperAdmin = true;
            }
        }

        User user = userDAO.find(id);
        if (user != null) {
            if (!(authenticatedUsername.equals(user.getUsername()) || (isAdmin && userManagementHelper.isOrganizationAuthenticated(authenticatedUser, user)) || isSuperAdmin)) {
                throw new UnauthorizedException("Unauthorized");
            }
        } else {
            throw new UnauthorizedException("Unauthorized");
        }

        userManagementHelper.deleteUser(user);

        return new Status("OK");
    }

    @ApiOperation(value = "Authenticate user")
    @RequestMapping(
            value = "/authenticate",
            consumes = { "application/x-www-form-urlencoded; charset=utf-8" },
            method = RequestMethod.POST)
    public @ResponseBody TokenTransfer authenticate(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ) {
        return getTokenTransfer(username.toLowerCase(), password); // force lower-case username
    }

    @ApiOperation(value = "Authenticate user (using JSON parameters)")
    @RequestMapping(
            value = "/authenticateFromJSON",
            consumes = { "application/json; charset=utf-8" },
            produces = { "application/json; charset=utf-8" },
            method = RequestMethod.POST)
    public @ResponseBody TokenTransfer authenticateFromJSON(
            @RequestBody Parameters parameters
    ) {
        return getTokenTransfer(parameters.getUsername().toLowerCase(), parameters.getPassword()); // force lower-case username
    }

    @ApiOperation(value = "Authenticate user via oAuth")
    @RequestMapping(
            value = "/authenticateViaOAuth",
            consumes = { "application/x-www-form-urlencoded; charset=utf-8" },
            method = RequestMethod.POST)
    public @ResponseBody TokenTransfer authenticateViaOAuth(
            @RequestParam("username") String username,
            @RequestParam("accessToken") String accessToken
    ) throws UnauthorizedException {
        User user = userDAO.getOAuthUser(username);

        if (user.getMostRecentlyUsedOAuthService().equals(User.GITHUB_SERVICE_NAME)) {
            if (!user.getGitHubAccessToken().equals(accessToken)) {
                throw new UnauthorizedException("Unauthorized: Invalid access token.");
            }
        }

        if (user.getMostRecentlyUsedOAuthService().equals(User.LINKEDIN_SERVICE_NAME)) {
            if (!user.getLinkedInAccessToken().equals(accessToken)) {
                throw new UnauthorizedException("Unauthorized: Invalid access token.");
            }
        }

        if (user.getMostRecentlyUsedOAuthService().equals(User.TWITTER_SERVICE_NAME)) {
            if (!user.getTwitterAccessToken().equals(accessToken)) {
                throw new UnauthorizedException("Unauthorized: Invalid access token.");
            }
        }

        if (user.getMostRecentlyUsedOAuthService().equals(User.XING_SERVICE_NAME)) {
            if (!user.getXingAccessToken().equals(user.getAccessToken())) {
                throw new UnauthorizedException("Unauthorized: Invalid access token.");
            }
        }

        return getTokenTransferForOAuth(username, accessToken);
    }

    @ApiOperation(value = "Upload a user's vCard")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/uploadVCards/{id}",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8" }
    )
    public @ResponseBody
    Map<String, Object> uploadVCards(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(id);
        if (user != null) {
            if (!authenticatedUsername.equals(user.getUsername())) {
                throw new UnauthorizedException("Unauthorized");
            }
        } else {
            throw new UnauthorizedException("Unauthorized");
        }

        List<Contact> ownContacts = contactDAO.getUserContacts(user.getId(), user.getCurrentOrganization().getId());
        Contact primaryContact;
        if (ownContacts.size() > 0) {
            primaryContact = ownContacts.get(0);
        } else {
            primaryContact = new Contact();
            ownContacts.add(primaryContact);
        }

        Map<String, Object> jsonObject = new HashMap<String, Object>();

        if (!file.isEmpty() && file.getOriginalFilename().endsWith(".vcf")) {
            try {
                List<VCard> vCards = Ezvcard.parse(file.getInputStream()).all();
                primaryContact.setVcard(Ezvcard.writeJson(vCards).go());

                if (primaryContact.getId() == null) {
                    Number contactId = contactDAO.insert(primaryContact);
                    userDAO.addContactToUser(contactId.intValue(), user.getId(), user.getCurrentOrganization().getId());
                } else {
                    contactDAO.update(primaryContact.getId(), primaryContact);
                }

                jsonObject.put("numberOfImportedVCards", vCards != null ? vCards.size() : 0);
            } catch (Exception e) {
                jsonObject.put("numberOfImportedVCards", 0);
                jsonObject.put("error", "Failure to upload " + file.getName() + ": " + e.getMessage());
            }
        } else {
            jsonObject.put("numberOfImportedVCards", 0);
            jsonObject.put("error", "Failure: " + file.getName() + " was empty.");
        }

        return jsonObject;
    }

    @ApiOperation(value = "Upload a user's profile picture")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/uploadProfilePicture/{id}",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8" }
    )
    public @ResponseBody
    Map<String, Object> uploadProfilePicture(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest
    ) throws InternalServerErrorException, BadRequestException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(id);
        if (user != null) {
            if (!authenticatedUsername.equals(user.getUsername())) {
                throw new UnauthorizedException("Unauthorized");
            }
        } else {
            throw new UnauthorizedException("Unauthorized");
        }

        Map<String, Object> jsonObject = new HashMap<String, Object>();

        String filename = file.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        if (!file.isEmpty() && uploadHelper.isValidImageSuffix(suffix)) {
            try {
                String newFilename = "user-profile-picture-" + user.getHashCode() + System.currentTimeMillis();
                File tempFile = File.createTempFile(newFilename, suffix);
                file.transferTo(tempFile);

                AmazonS3 s3client = new AmazonS3Client(new EnvironmentVariableCredentialsProvider());
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        System.getenv().get("S3_BUCKET_NAME"),
                        "users/" + newFilename + "." + suffix,
                        tempFile
                );
                putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                putObjectRequest.withMetadata(metadata);
                PutObjectResult putObjectResult = s3client.putObject(putObjectRequest);

                String imagePath =  s3URL + "/" + newFilename + "." + suffix;

                user.setProfilePicturePath(imagePath);
                userDAO.update(user.getId(), user);

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

        return jsonObject;
    }

    @ApiOperation(value = "Create a user's vCard from string input")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/createVCardsFromString/{id}",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Map<String, Object> createVCardsFromString(
            @PathVariable Integer id,
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(id);
        if (user != null) {
            if (!authenticatedUsername.equals(user.getUsername())) {
                throw new UnauthorizedException("Unauthorized");
            }
        } else {
            throw new UnauthorizedException("Unauthorized");
        }

        List<Contact> ownContacts = contactDAO.getUserContacts(user.getId(), user.getCurrentOrganization().getId());
        Contact primaryContact;
        if (ownContacts.size() > 0) {
            primaryContact = ownContacts.get(0);
        } else {
            primaryContact = new Contact();
            ownContacts.add(primaryContact);
        }

        Map<String, Object> jsonObject = new HashMap<String, Object>();

        String format = "vcard";
        if (parameters.getFormat() != null) {
            format = parameters.getFormat();
        }

        List<VCard> vCards;
        if (format.equals("vcard")) {
            vCards = Ezvcard.parse(parameters.getVcard()).all();
        } else {
            vCards = Ezvcard.parseJson(parameters.getVcard()).all();
        }
        primaryContact.setVcard(Ezvcard.writeJson(vCards).go());

        Timestamp updatedOfflineTimestamp = new Timestamp(System.currentTimeMillis());
        if (parameters.getUpdatedOffline() != null) {
            updatedOfflineTimestamp = new Timestamp(parameters.getUpdatedOffline());
        }
        Timestamp addedAtTimestamp = new Timestamp(System.currentTimeMillis());
        if (parameters.getAddedAt() != null) {
            addedAtTimestamp = new Timestamp(parameters.getAddedAt());
        }

        if (parameters.getIsTestRun() == null) {
            parameters.setIsTestRun(false);
        }
        if (!parameters.getIsTestRun()) {
            if (primaryContact.getId() == null) {
                contactDAO.addContact(user.getId(), user.getCurrentOrganization().getId(), primaryContact, updatedOfflineTimestamp, addedAtTimestamp);
                Number contactId = contactDAO.insert(primaryContact);
                userDAO.addContactToUser(contactId.intValue(), user.getId(), user.getCurrentOrganization().getId());
            } else {
                if (parameters.getUpdatedOffline() != null) {
                    primaryContact.setUpdatedOffline(updatedOfflineTimestamp);
                    primaryContact.setAddedAt(addedAtTimestamp);
                }
                contactDAO.update(primaryContact.getId(), primaryContact);
                contactDAO.updateConnectionTimestamps(user.getId(), user.getCurrentOrganization().getId(), primaryContact.getId(), updatedOfflineTimestamp, addedAtTimestamp);
            }
        }

        jsonObject.put("numberOfImportedVCards", vCards != null ? vCards.size() : 0);

        return jsonObject;
    }

    @ApiOperation(value = "Update a user's vCards from string input")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/updateVCards",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status updateVCards(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(parameters.getUserId());
        if (user != null) {
            if (!authenticatedUsername.equals(user.getUsername())) {
                throw new UnauthorizedException("Unauthorized");
            }
        } else {
            throw new UnauthorizedException("Unauthorized");
        }

        if (parameters.getIsTestRun() == null) {
            parameters.setIsTestRun(false);
        }

        List<Contact> ownContacts = contactDAO.getUserContacts(user.getId(), user.getCurrentOrganization().getId());
        List<String> vcardsForContacts = parameters.getVcards();
        for (int i = 0; i < ownContacts.size(); i++) {
            Contact contact = ownContacts.get(i);
            contact.setVcard(vcardsForContacts.get(i));

            if (!parameters.getIsTestRun()) {
                contactDAO.update(contact.getId(), contact);
            }
        }

        return new Status("OK");
    }

    @ApiOperation(value = "Set 'synchronizedAt' timestamp to now.")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/finishSynchronization/{id}",
            method = RequestMethod.GET)
    public @ResponseBody Status finishSynchronization(
            @PathVariable Integer id,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(id);
        if (user != null) {
            if (!authenticatedUsername.equals(user.getUsername())) {
                throw new UnauthorizedException("Unauthorized");
            }
        } else {
            throw new UnauthorizedException("Unauthorized");
        }

        userDAO.updateSynchronizationTimestamp(id);

        return new Status("OK");
    }

    public void setSendMail(Boolean sendMail) {
        this.sendMail = sendMail;
    }

    public void setSkipJCard(Boolean skipJCard) {
        this.skipJCard = skipJCard;
    }

    private Boolean isOauthUser(User user) {
        Boolean isOauthUser = false;

        if (user.getUseOAuth() != null && user.getUseOAuth()) {
           isOauthUser = true;
        }

        return isOauthUser;
    }

    private Map<String, Boolean> createRoleMap(UserDetails userDetails) {
        Map<String, Boolean> roles = new HashMap<String, Boolean>();

        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            roles.put(authority.getAuthority(), Boolean.TRUE);
        }

        return roles;
    }

    private TokenTransfer getTokenTransfer(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = userDAO.loadUserByUsername(username);

        return new TokenTransfer(TokenUtil.createToken(userDetails));
    }

    private TokenTransfer getTokenTransferForOAuth(String username, String accessToken) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, accessToken);
        Authentication authentication = oAuthAuthenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = userDAO.loadUserByUsername(username);

        return new TokenTransfer(TokenUtil.createToken(userDetails));
    }

    private User sendEMailConnectionRequest(User existingUser, String recipientEMailAddress) throws InternalServerErrorException {
        User user = new User();
        user.setUsername(recipientEMailAddress);
        user.setPassword(recipientEMailAddress + Math.random() + System.currentTimeMillis());
        user.setTemporaryUser(true);

        User newUser = userDAO.findTemporaryUserByUsername(user.getUsername());
        if (newUser == null) {
            newUser = userDAO.findByUsername(user.getUsername());
        }
        if (newUser == null) {
            newUser = userManagementHelper.createUser(user);
            if (newUser.getId() == null) {
                throw new InternalServerErrorException("Internal server error: User could not be created.");
            }
        }

        if (existingUser.getOwnContacts() == null || existingUser.getOwnContacts().size() <= 0) {
            existingUser.setOwnContacts(contactDAO.getUserContacts(existingUser.getId(), existingUser.getCurrentOrganization().getId()));
        }
        VCard vCard = null;
        if (existingUser.getOwnContacts() != null && existingUser.getOwnContacts().size() > 0) {
            vCard = Ezvcard.parseJson(existingUser.getOwnContacts().get(0).getVcard()).first();
        }

        if (sendMail) {
            mailer.sendEMailConnectionRequest(existingUser, vCard, recipientEMailAddress, newUser.getHashCode());
        }

        try {
            userManagementHelper.addConnection(
                    existingUser.getId(),
                    newUser.getUsername(),
                    existingUser.getCurrentOrganization().getId(),
                    true,
                    null,
                    null,
                    null,
                    skipJCard
            );
        } catch (NotFoundException e) {
            logger.debug(e.getMessage());
        } catch (ForbiddenException e) {
            logger.debug(e.getMessage());
        }

        return newUser;
    }

    private User sendEMailToCoWorker(User existingUser, String recipientEMailAddress, Integer organizationId) throws InternalServerErrorException {
        User user = new User();
        user.setUsername(recipientEMailAddress);
        user.setPassword(recipientEMailAddress + Math.random() + System.currentTimeMillis());
        user.setTemporaryUser(true);

        Boolean isTemporaryUser = true;
        User newUser = userDAO.findTemporaryUserByUsername(user.getUsername());
        if (newUser == null) {
            isTemporaryUser = false;
            newUser = userDAO.findByUsername(user.getUsername());
        }
        if (newUser == null) {
            isTemporaryUser = true;
            newUser = userManagementHelper.createUser(user);
            if (newUser.getId() == null) {
                throw new InternalServerErrorException("Internal server error: User could not be created.");
            }
        }

        Organization organization = organizationDAO.find(organizationId);
        if (existingUser.getOwnContacts() == null || existingUser.getOwnContacts().size() <= 0) {
            existingUser.setOwnContacts(contactDAO.getUserContacts(existingUser.getId(), organization.getId()));
        }
        VCard vCard = null;
        if (existingUser.getOwnContacts() != null && existingUser.getOwnContacts().size() > 0) {
            vCard = Ezvcard.parseJson(existingUser.getOwnContacts().get(0).getVcard()).first();
        }

        if (sendMail) {
            mailer.sendEMailConnectionRequest(existingUser, vCard, recipientEMailAddress, newUser.getHashCode());
        }

        try {
            newUser.setCurrentOrganization(organization);
            userManagementHelper.createContact(newUser, skipJCard);
            
            userManagementHelper.addCoWorker(
                    newUser.getUsername(),
                    organization.getId(),
                    isTemporaryUser,
                    skipJCard
            );
        } catch (NotFoundException e) {
            logger.debug(e.getMessage());
        }

        return newUser;
    }

    public UserDetails getUserDetails() throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof String && principal.equals("anonymousUser")) {
            throw new UnauthorizedException("Unauthorized: Anonymous user.");
        }

        return (UserDetails) principal;
    }
}
