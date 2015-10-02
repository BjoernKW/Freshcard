package com.freshcard.backend.api;

import com.freshcard.backend.model.*;
import com.freshcard.backend.model.dao.ContactDAO;
import com.freshcard.backend.model.dao.OrganizationDAO;
import com.freshcard.backend.model.dao.UserDAO;
import com.freshcard.backend.security.ForbiddenException;
import com.freshcard.backend.security.TokenUtil;
import com.freshcard.backend.security.UnauthorizedException;
import com.freshcard.backend.util.UserManagementHelper;
import com.wordnik.swagger.annotations.*;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "contacts", description = "contacts")
@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {
    private static final Logger logger = Logger.getLogger(ContactController.class);

    @Autowired
    private ContactDAO contactDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private OrganizationDAO organizationDAO;

    @Autowired
    private UserManagementHelper userManagementHelper;

    private Boolean skipJCard = false;

    @ApiResponses(value = { @ApiResponse(code = 404, message = "Resource not found") } )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler( { NotFoundException.class } )
    public void handleResourceNotFound() {
    }

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

    @ApiOperation(value = "Get a user's own contact info")
    @Authorization(value = "token-based")
    @RequestMapping(value = "/getUserContacts", method = RequestMethod.POST, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    List<Contact> getUserContacts(
            @RequestParam("userId") Integer userId,
            @RequestParam("organizationId") Integer organizationId,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(userId);
        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return contactDAO.getUserContacts(userId, organizationId);
    }

    @ApiOperation(value = "Get a user's own contact info (using JSON parameters)")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/getUserContactsFromJSON",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    List<Contact> getUserContactsFromJSON(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(parameters.getUserId());
        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return contactDAO.getUserContacts(parameters.getUserId(), parameters.getOrganizationId());
    }

    @ApiOperation(value = "Get a user's connections")
    @Authorization(value = "token-based")
    @RequestMapping(value = "/getUserConnections", method = RequestMethod.POST, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    List<Contact> getUserConnections(
            @RequestParam("userId") Integer userId,
            @RequestParam("organizationId") Integer organizationId,
            @RequestParam("offset") Integer offset,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(userId);
        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return contactDAO.getUserConnections(userId, organizationId, offset);
    }

    @ApiOperation(value = "Get a user's connections (using JSON parameters)")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/getUserConnectionsFromJSON",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    List<Contact> getUserConnectionsFromJSON(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(parameters.getUserId());
        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return contactDAO.getUserConnections(parameters.getUserId(), parameters.getOrganizationId(), parameters.getOffset());
    }

    @ApiOperation(value = "Get a connection")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/getUserConnection",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Contact getUserConnection(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(parameters.getUserId());
        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return contactDAO.getUserConnection(parameters.getUserId(), parameters.getContactId(), parameters.getOrganizationId());
    }

    @ApiOperation(value = "Get a user's co-workers")
    @Authorization(value = "token-based")
    @RequestMapping(value = "/getCoWorkers", method = RequestMethod.POST, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    List<Contact> getCoWorkers(
            @RequestParam("userId") Integer userId,
            @RequestParam("organizationId") Integer organizationId,
            @RequestParam("offset") Integer offset,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(userId);
        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return contactDAO.getCoWorkers(userId, organizationId, offset);
    }

    @ApiOperation(value = "Get a user's co-workers (using JSON parameters)")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/getCoWorkersFromJSON",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    List<Contact> getCoWorkersFromJSON(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(parameters.getUserId());
        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        return contactDAO.getCoWorkers(parameters.getUserId(), parameters.getOrganizationId(), parameters.getOffset());
    }

    @ApiOperation(value = "Get a co-worker")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/getCoWorker",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Contact getCoWorker(
            @RequestBody Parameters parameters
    ) throws UnauthorizedException {
        return contactDAO.getCoWorker(parameters.getUserId(), parameters.getContactId(), parameters.getOrganizationId());
    }

    @ApiOperation(value = "Find contact by ID")
    @Authorization(value = "token-based")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
	public @ResponseBody
    Contact find(@PathVariable Integer id) {
		return contactDAO.find(id);
	}

    @ApiOperation(value = "Find contact by hash code")
    @RequestMapping(value = "/findByHashCode", method = RequestMethod.POST, produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    Contact findByHashCode(@RequestBody String hashCode) {
        return contactDAO.findByHashCode(hashCode);
    }

    @ApiOperation(value = "Get a contact's vCards by hash code")
    @RequestMapping(value = "/getVCardsByHashCode/{hashCode}", method = RequestMethod.GET)
    public @ResponseBody
    String getVCardsByHashCode(
            @PathVariable String hashCode,
            HttpServletResponse response
    ) {
        response.setContentType("text/vcard; charset=utf-8");

        return Ezvcard.write(Ezvcard.parseJson(contactDAO.findByHashCode(hashCode).getVcard()).all()).version(VCardVersion.V3_0).go();
    }

    @ApiOperation(value = "Create contact")
    @Authorization(value = "token-based")
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Contact create(
            @RequestBody Contact contact
    ) {
        Number id = contactDAO.insert(contact);

        return contactDAO.find(new Long(id.longValue()).intValue());
    }

    @ApiOperation(value = "Update contact")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/{id}",
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            method = RequestMethod.PUT
    )
    public @ResponseBody Status update(
            @PathVariable Integer id,
            @RequestBody Contact contact,
            HttpServletRequest httpRequest
    ) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!userManagementHelper.isAuthorizedForContact(userDAO.findEnabledUserByUsername(authenticatedUsername).getId(), id)) {
            throw new UnauthorizedException("Unauthorized");
        }

        contactDAO.update(id, contact);

        return new Status("OK");
    }

    @ApiOperation(value = "Delete contact")
    @Authorization(value = "token-based")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Status delete(@PathVariable Integer id, HttpServletRequest httpRequest) throws UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!userManagementHelper.isAuthorizedForContact(userDAO.findEnabledUserByUsername(authenticatedUsername).getId(), id)) {
            throw new UnauthorizedException("Unauthorized");
        }

        contactDAO.delete(id);

        return new Status("OK");
    }

    @ApiOperation(value = "Add co-worker")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/addCoWorker",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status addCoWorkerFromParameters(
            @RequestParam("userId") Integer userId,
            @RequestParam("otherUsername") String otherUsername,
            @RequestParam("organizationId") Integer organizationId,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(userId);
        if (!(authenticatedUsername.equals(user.getUsername()) && userManagementHelper.isAuthorizedForOrganization(user.getUsername(), organizationId) && userManagementHelper.isAdmin(user.getUsername(), organizationId))) {
            throw new UnauthorizedException("Unauthorized");
        }

        addCoWorker(otherUsername, organizationId);

        return new Status("OK");
    }

    @ApiOperation(value = "Add co-worker (using JSON parameters)")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/addCoWorkerFromJSON",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status addCoWorkerFromJSON(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(parameters.getUserId());
        if (!(authenticatedUsername.equals(user.getUsername()) && userManagementHelper.isAuthorizedForOrganization(user.getUsername(), parameters.getOrganizationId()) && userManagementHelper.isAdmin(user.getUsername(), parameters.getOrganizationId()))) {
            throw new UnauthorizedException("Unauthorized");
        }

        addCoWorker(parameters.getOtherUsername(), parameters.getOrganizationId());

        return new Status("OK");
    }

    @ApiOperation(value = "Add connection to existing user")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/addConnection",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    Contact addConnectionFromParameters(
            @RequestParam("userId") Integer userId,
            @RequestParam("otherUsername") String otherUsername,
            @RequestParam("organizationId") Integer organizationId,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException, ForbiddenException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(userId);
        if (!(authenticatedUsername.equals(user.getUsername()) && userManagementHelper.isAuthorizedForOrganization(user.getUsername(), organizationId))) {
            throw new UnauthorizedException("Unauthorized");
        }

        Contact contact = userManagementHelper.addConnection(userId, otherUsername, organizationId, false, null, null, null, skipJCard);

        return contact;
    }

    @ApiOperation(value = "Mutually add connection from users' hash codes")
    @RequestMapping(
            value = "/addMutualConnection",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    User addMutualConnection(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException, ForbiddenException {
        User user = userDAO.findByHashCode(parameters.getHashCode());
        User otherUser = userDAO.findByHashCode(parameters.getConnectionHashCode());

        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        userManagementHelper.addConnection(
                user.getId(),
                otherUser.getUsername(),
                user.getCurrentOrganization().getId(),
                false,
                null,
                null,
                null,
                skipJCard
        );

        userManagementHelper.addConnection(
                otherUser.getId(),
                user.getUsername(),
                otherUser.getCurrentOrganization().getId(),
                false,
                null,
                null,
                null,
                skipJCard
        );

        return otherUser;
    }

    @ApiOperation(value = "Mutually add connection from users' hash codes")
    @RequestMapping(
            value = "/addMutualConnectionWithTemporaryUser",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    User addMutualConnectionWithTemporaryUser(
            @RequestBody Parameters parameters
    ) throws NotFoundException, UnauthorizedException, ForbiddenException {
        User user = userDAO.findByHashCode(parameters.getHashCode());
        User otherUser = userDAO.findByHashCode(parameters.getConnectionHashCode());

        userManagementHelper.addConnection(
                otherUser.getId(),
                user.getUsername(),
                otherUser.getCurrentOrganization().getId(),
                false,
                null,
                null,
                null,
                skipJCard
        );

        return otherUser;
    }

    @ApiOperation(value = "Add connection to existing user (using JSON parameters)")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/addConnectionFromJSON",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Contact addConnectionFromJSON(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException, ForbiddenException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(parameters.getUserId());
        if (!(authenticatedUsername.equals(user.getUsername()) && userManagementHelper.isAuthorizedForOrganization(user.getUsername(), parameters.getOrganizationId()))) {
            throw new UnauthorizedException("Unauthorized");
        }

        Contact contact = userManagementHelper.addConnection(
                parameters.getUserId(),
                parameters.getOtherUsername(),
                parameters.getOrganizationId(),
                false,
                parameters.getNotes(),
                parameters.getUpdatedOffline(),
                parameters.getAddedAt(),
                skipJCard
        );

        return contact;
    }

    @ApiOperation(value = "Add connection to existing user (using JSON parameters)")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/updateConnection",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status updateConnection(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException, ForbiddenException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(parameters.getUserId());
        if (!(authenticatedUsername.equals(user.getUsername()) && userManagementHelper.isAuthorizedForOrganization(user.getUsername(), parameters.getOrganizationId()))) {
            throw new UnauthorizedException("Unauthorized");
        }

        Timestamp updatedOfflineTimestamp = new Timestamp(System.currentTimeMillis());
        if (parameters.getUpdatedOffline() != null) {
            updatedOfflineTimestamp = new Timestamp(parameters.getUpdatedOffline());
        }

        contactDAO.updateConnection(
                parameters.getUserId(),
                parameters.getOtherContactId(),
                parameters.getOrganizationId(),
                parameters.getNotes(),
                updatedOfflineTimestamp
        );

        return new Status("OK");
    }

    @ApiOperation(value = "Remove co-worker")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/removeCoWorker",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status removeCoWorkerFromParameters(
            @RequestParam("userId") Integer userId,
            @RequestParam("organizationId") Integer organizationId,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!(userManagementHelper.isAuthorizedForOrganization(authenticatedUsername, organizationId) && userManagementHelper.isAdmin(authenticatedUsername, organizationId))) {
            throw new UnauthorizedException("Unauthorized");
        }

        removeCoWorker(userId, organizationId);

        return new Status("OK");
    }

    @ApiOperation(value = "Remove co-worker (using JSON parameters)")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/removeCoWorkerFromJSON",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status removeCoWorkerFromJSON(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!(userManagementHelper.isAuthorizedForOrganization(authenticatedUsername, parameters.getOrganizationId()) && userManagementHelper.isAdmin(authenticatedUsername, parameters.getOrganizationId()))) {
            throw new UnauthorizedException("Unauthorized");
        }

        removeCoWorker(parameters.getUserId(), parameters.getOrganizationId());

        return new Status("OK");
    }

    @ApiOperation(value = "Remove connection")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/removeConnection",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status removeConnectionFromParameters(
            @RequestParam("userId") Integer userId,
            @RequestParam("otherContactId") Integer otherContactId,
            @RequestParam("organizationId") Integer organizationId,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(userId);
        if (!(authenticatedUsername.equals(user.getUsername()) && userManagementHelper.isAuthorizedForOrganization(user.getUsername(), organizationId))) {
            throw new UnauthorizedException("Unauthorized");
        }

        removeConnection(userId, otherContactId, organizationId, null);

        return new Status("OK");
    }

    @ApiOperation(value = "Remove connection (using JSON parameters)")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/removeConnectionFromJSON",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status removeConnectionFromJSON(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(parameters.getUserId());
        if (!(authenticatedUsername.equals(user.getUsername()) && userManagementHelper.isAuthorizedForOrganization(user.getUsername(), parameters.getOrganizationId()))) {
            throw new UnauthorizedException("Unauthorized");
        }

        removeConnection(parameters.getUserId(), parameters.getOtherContactId(), parameters.getOrganizationId(), parameters.getUpdatedOffline());

        return new Status("OK");
    }

    @ApiOperation(value = "Remove connection by username")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/removeConnectionByUsername",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status removeConnectionByUsernameFromParameters(
            @RequestParam("userId") Integer userId,
            @RequestParam("otherUsername") String otherUsername,
            @RequestParam("organizationId") Integer organizationId,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(userId);
        if (!(authenticatedUsername.equals(user.getUsername()) && userManagementHelper.isAuthorizedForOrganization(user.getUsername(), organizationId))) {
            throw new UnauthorizedException("Unauthorized");
        }
        removeConnectionByUsername(userId, otherUsername, organizationId, null);

        return new Status("OK");
    }

    @ApiOperation(value = "Remove connection by username (using JSON parameters)")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/removeConnectionByUsernameFromJSON",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Status removeConnectionByUsernameFromJSON(
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException {
        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        User user = userDAO.find(parameters.getUserId());
        if (!(authenticatedUsername.equals(user.getUsername()) && userManagementHelper.isAuthorizedForOrganization(user.getUsername(), parameters.getOrganizationId()))) {
            throw new UnauthorizedException("Unauthorized");
        }

        removeConnectionByUsername(parameters.getUserId(), parameters.getOtherUsername(), parameters.getOrganizationId(), parameters.getUpdatedOffline());

        return new Status("OK");
    }

    @ApiOperation(value = "Create a contact by uploading a vCard")
    @RequestMapping(
            value = "/createFromVCard/{id}",
            method = RequestMethod.POST,
            produces = { "application/json; charset=utf-8" }
    )
    public @ResponseBody
    Map<String, Object> createFromVCard(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException {
        User user = userDAO.find(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        Map<String, Object> jsonObject = new HashMap<String, Object>();

        if (!file.isEmpty() && file.getOriginalFilename().endsWith(".vcf")) {
            try {
                Contact contact = new Contact();

                List<VCard> vCards = Ezvcard.parse(file.getInputStream()).all();
                contact.setVcard(Ezvcard.writeJson(vCards).go());

                contactDAO.addContact(user.getId(), user.getCurrentOrganization().getId(), contact, null, null);

                jsonObject.put("numberOfImportedVCards", vCards.size());
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

    @ApiOperation(value = "Create a contact from a vCard string")
    @RequestMapping(
            value = "/createFromVCardString/{id}",
            method = RequestMethod.POST,
            consumes = { "application/json; charset=utf-8", "application/xml; charset=utf-8" },
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" }
    )
    public @ResponseBody
    Map<String, Object> createFromVCardString(
            @PathVariable Integer id,
            @RequestBody Parameters parameters,
            HttpServletRequest httpRequest
    ) throws NotFoundException, UnauthorizedException {
        User user = userDAO.find(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        String authToken = httpRequest.getHeader("X-Auth-Token");
        String authenticatedUsername = TokenUtil.getUserNameFromToken(authToken);

        if (!authenticatedUsername.equals(user.getUsername())) {
            throw new UnauthorizedException("Unauthorized");
        }

        Map<String, Object> jsonObject = new HashMap<String, Object>();

        Contact contact = new Contact();

        List<VCard> vCards = Ezvcard.parse(parameters.getVcard()).all();
        contact.setVcard(Ezvcard.writeJson(vCards).go());

        Timestamp updatedOfflineTimestamp = new Timestamp(System.currentTimeMillis());
        if (parameters.getUpdatedOffline() != null) {
            updatedOfflineTimestamp = new Timestamp(parameters.getUpdatedOffline());
        }
        Timestamp addedAtTimestamp = new Timestamp(System.currentTimeMillis());
        if (parameters.getAddedAt() != null) {
            addedAtTimestamp = new Timestamp(parameters.getAddedAt());
        }

        if (!parameters.getIsTestRun()) {
            contactDAO.addContact(
                    user.getId(),
                    user.getCurrentOrganization().getId(),
                    contact,
                    updatedOfflineTimestamp,
                    addedAtTimestamp
            );
        }

        jsonObject.put("numberOfImportedVCards", vCards.size());

        return jsonObject;
    }

    public void setSkipJCard(Boolean skipJCard) {
        this.skipJCard = skipJCard;
    }

    private void addCoWorker(String username, Integer organizationId) throws NotFoundException {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        Organization organization = organizationDAO.find(organizationId);
        if (organization == null) {
            throw new NotFoundException("Organization not found");
        }

        Boolean successful = contactDAO.addCoWorker(user.getId(), organization.getId());
        if (!successful) {
            throw new NotFoundException("Contact data not found");
        }
    }

    private void removeCoWorker(Integer userId, Integer organizationId) throws NotFoundException {
        User user = userDAO.find(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        Organization organization = organizationDAO.find(organizationId);
        if (organization == null) {
            throw new NotFoundException("Organization not found");
        }

        Boolean successful = contactDAO.removeCoWorker(user.getId(), organization.getId());
        if (!successful) {
            throw new NotFoundException("Contact data not found");
        }
    }

    private void removeConnection(Integer userId, Integer otherContactId, Integer organizationId, Long updatedOffline) throws NotFoundException {
        User user = userDAO.find(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        Contact otherContact = contactDAO.find(otherContactId);
        if (otherContact == null) {
            throw new NotFoundException("Contact not found");
        }
        Organization organization = organizationDAO.find(organizationId);
        if (organization == null) {
            throw new NotFoundException("Organization not found");
        }

        Boolean successful = contactDAO.removeConnection(user.getId(), otherContact.getId(), organization.getId());
        if (!successful) {
            throw new NotFoundException("Contact data not found");
        }

        Timestamp updatedOfflineTimestamp = new Timestamp(System.currentTimeMillis());
        if (updatedOffline != null) {
            updatedOfflineTimestamp = new Timestamp(updatedOffline);
        }

        userDAO.updateConnectionSynchronizationTimestamp(user.getId(), updatedOfflineTimestamp);
    }

    private void removeConnectionByUsername(Integer userId, String otherUsername, Integer organizationId, Long updatedOffline) throws NotFoundException {
        User user = userDAO.find(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        User otherUser = userDAO.findByUsername(otherUsername);
        if (otherUser == null) {
            throw new NotFoundException("User not found");
        }
        Organization organization = organizationDAO.find(organizationId);
        if (organization == null) {
            throw new NotFoundException("Organization not found");
        }

        Boolean successful = contactDAO.removeConnectionByUserId(user.getId(), otherUser.getId(), organization.getId());
        if (!successful) {
            throw new NotFoundException("Contact data not found");
        }

        Timestamp updatedOfflineTimestamp = new Timestamp(System.currentTimeMillis());
        if (updatedOffline != null) {
            updatedOfflineTimestamp = new Timestamp(updatedOffline);
        }

        userDAO.updateConnectionSynchronizationTimestamp(user.getId(), updatedOfflineTimestamp);
    }
}