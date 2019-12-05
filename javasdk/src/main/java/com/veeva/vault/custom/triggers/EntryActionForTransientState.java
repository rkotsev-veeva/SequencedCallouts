package com.veeva.vault.custom.triggers;


import com.veeva.vault.custom.udc.RequestFactory;
import com.veeva.vault.custom.udc.SequencedCommunication;
import com.veeva.vault.custom.udc.VaultConstants;
import com.veeva.vault.sdk.api.action.DocumentAction;
import com.veeva.vault.sdk.api.action.DocumentActionContext;
import com.veeva.vault.sdk.api.action.DocumentActionInfo;
import com.veeva.vault.sdk.api.action.Usage;
import com.veeva.vault.sdk.api.core.LogService;
import com.veeva.vault.sdk.api.core.ServiceLocator;
import com.veeva.vault.sdk.api.core.ValueType;
import com.veeva.vault.sdk.api.core.VaultCollections;
import com.veeva.vault.sdk.api.http.*;
import com.veeva.vault.sdk.api.json.JsonData;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>This action should be assigned as an entry action on the transitioning state.</p>
 * <p>We want to start a workflow and then move away from the transitioning state.</p>
 *
 * @Author Radoslav Kotsev
 */
@DocumentActionInfo(label = "SRD POC", usages = Usage.UNSPECIFIED)
public class EntryActionForTransientState implements DocumentAction {


    @Override
    public boolean isExecutable(DocumentActionContext documentActionContext) {
        return true;
    }

    @Override
    public void execute(DocumentActionContext documentActionContext) {

        final LogService logService = ServiceLocator.locate(LogService.class);

        final RequestFactory requestFactory = new RequestFactory();

        documentActionContext.getDocumentVersions().stream().forEach(documentVersion -> {

            final String id = documentVersion.getValue(VaultConstants.DOCFIELD_ID, ValueType.STRING);
            final BigDecimal majorVersionNumber = documentVersion.getValue(VaultConstants.DOCFIELD_MAJOR_VERSION_NUMBER, ValueType.NUMBER);
            final BigDecimal minorVersionNumber = documentVersion.getValue(VaultConstants.DOCFIELD_MINOR_VERSION_NUMBER, ValueType.NUMBER);
            final String versionId = String.format("%s_%s_%s", id, majorVersionNumber, minorVersionNumber);

            //Start Workflow
            final HttpRequest wfRequest = requestFactory.localDocAction(versionId, VaultCollections.newMap(), "LifecycleUserAction1");
            //Change State
            final HttpRequest stateChangeReq = requestFactory.localDocAction(versionId, VaultCollections.newMap(), "LifecycleUserAction");

            final Map<String, String> params1 = VaultCollections.newMap();
            params1.put("link__sys", "test");

            final Map<String, String> params2 = VaultCollections.newMap();
            params2.put("version_link__sys", "test2");

            //Update Document Link Field
            final HttpRequest updField1 = requestFactory.localDocUpdate(versionId, params1);
            //Update Document Version Link Field
            final HttpRequest updField2 = requestFactory.localDocUpdate(versionId, params2);

            final SequencedCommunication comm1 = new SequencedCommunication(wfRequest) {
                @Override
                protected void doOnSuccess(final HttpResponse<JsonData> httpResponse) {
                    final String response = httpResponse.getResponseBody().getJsonObject().asString();
                    logService.debug(response);
                    logService.debug("success1");
                }
            };

            final SequencedCommunication comm2 = new SequencedCommunication(stateChangeReq) {
                @Override
                protected void doOnSuccess(HttpResponse<JsonData> httpResponse) {
                    final String response2 = httpResponse.getResponseBody().getJsonObject().asString();
                    logService.debug(httpResponse.getResponseBody().getJsonObject().asString());
                    logService.debug("success2");
                }
            };

            final SequencedCommunication comm3 = new SequencedCommunication(updField1) {
                @Override
                protected void doOnSuccess(HttpResponse<JsonData> httpResponse) {
                    final String response3 = httpResponse.getResponseBody().getJsonObject().asString();
                    logService.debug(httpResponse.getResponseBody().getJsonObject().asString());
                    logService.debug("success3");
                }
            };

            final SequencedCommunication comm4 = new SequencedCommunication(updField2) {
                @Override
                protected void doOnSuccess(HttpResponse<JsonData> httpResponse) {
                    final String response4 = httpResponse.getResponseBody().getJsonObject().asString();
                    logService.debug(httpResponse.getResponseBody().getJsonObject().asString());
                    logService.debug("success4");
                }
            };

            comm1.setNext(comm2);
            comm2.setNext(comm3);
            comm3.setNext(comm4);

            comm1.execute();

            /*
            final HttpService httpService = ServiceLocator.locate(HttpService.class);
            httpService.send(wfRequest, HttpResponseBodyValueType.JSONDATA)
                    .onSuccess( httpResponse -> {

                        final String response = httpResponse.getResponseBody().getJsonObject().asString();
                        logService.debug(response);
                        logService.debug("success1");
                        httpService.send(stateChangeReq, HttpResponseBodyValueType.JSONDATA).onSuccess(httpResponse2 -> {
                            final String response2 = httpResponse2.getResponseBody().getJsonObject().asString();
                            logService.debug(httpResponse2.getResponseBody().getJsonObject().asString());
                            logService.debug("success2");
                        }).ignoreError().execute();
                    } )
                    .ignoreError().execute();

             */

        });

    }
}
