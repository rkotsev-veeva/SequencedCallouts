package com.veeva.vault.custom.udc;

import com.veeva.vault.sdk.api.core.RollbackException;
import com.veeva.vault.sdk.api.core.ServiceLocator;
import com.veeva.vault.sdk.api.core.StringUtils;
import com.veeva.vault.sdk.api.core.UserDefinedClassInfo;
import com.veeva.vault.sdk.api.http.HttpMethod;
import com.veeva.vault.sdk.api.http.HttpRequest;
import com.veeva.vault.sdk.api.http.HttpService;

import java.util.Map;

/**
 * Provides requests to be executed via {@link com.veeva.vault.sdk.api.http.HttpService}
 *
 * @Author Radoslav
 */
@UserDefinedClassInfo()
public class RequestFactory {

    public HttpRequest localDocAction(final String versionId, Map<String, String> params, final String action) {
        final String[] versions = StringUtils.split(versionId, "_");
        final HttpService httpService = ServiceLocator.locate(HttpService.class);

        final String path = String.format("/api/v19.1/objects/documents/%s/versions/%s/%s/lifecycle_actions/%s", versions[0], versions[1], versions[2], action);

        final HttpRequest request =  httpService.newHttpRequest("srd_test_poc")
        //final HttpRequest request =  httpService.newLocalHttpRequest()
                .setMethod(HttpMethod.PUT)
                .appendPath(path);

        params.keySet().forEach(key -> {
            request.setBodyParam(key, params.get(key));
        });

        return request;
    }

    public HttpRequest localDocUpdate(final String versionId, Map<String, String> params) {
        final String[] versions = StringUtils.split(versionId, "_");
        final HttpService httpService = ServiceLocator.locate(HttpService.class);

        final String path = String.format("/api/v19.1/objects/documents/%s", versions[0]);

        final HttpRequest request = httpService.newHttpRequest("srd_test_poc")
                .setMethod(HttpMethod.PUT)
                .appendPath(path);

        if(params.isEmpty()) {
            throw new RollbackException("IllegalArgumentException","There are no fields to be updated!");
        }

        params.keySet().forEach(key -> {
            request.setBodyParam(key, params.get(key));
        });

        return request;
    }

}
