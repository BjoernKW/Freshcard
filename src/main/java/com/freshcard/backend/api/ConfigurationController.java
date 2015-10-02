package com.freshcard.backend.api;

import com.freshcard.backend.http.BadRequestException;
import com.freshcard.backend.model.AlgoliaConfiguration;
import com.freshcard.backend.security.UnauthorizedException;
import com.wordnik.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Api(value = "configuration", description = "configuration")
@RestController
@RequestMapping("/api/v1/configuration")
public class ConfigurationController {
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

    @ApiOperation(value = "Get Algolia configuration")
    @Authorization(value = "token-based")
    @RequestMapping(
            value = "/getAlgoliaConfiguration",
            method = RequestMethod.GET,
            produces = { "application/json; charset=utf-8", "application/xml; charset=utf-8" })
    public @ResponseBody
    AlgoliaConfiguration getAlgoliaConfiguration() throws UnauthorizedException {
        AlgoliaConfiguration algoliaConfiguration = new AlgoliaConfiguration();
        algoliaConfiguration.setAlgoliaApplicationID(System.getenv().get("ALGOLIASEARCH_APPLICATION_ID"));
        algoliaConfiguration.setAlgoliaSearchKey(System.getenv().get("ALGOLIASEARCH_API_KEY_SEARCH"));

        return algoliaConfiguration;
    }
}