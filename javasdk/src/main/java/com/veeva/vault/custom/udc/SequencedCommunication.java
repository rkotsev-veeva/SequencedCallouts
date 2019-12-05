package com.veeva.vault.custom.udc;

import com.veeva.vault.sdk.api.core.ServiceLocator;
import com.veeva.vault.sdk.api.core.UserDefinedClassInfo;
import com.veeva.vault.sdk.api.http.HttpRequest;
import com.veeva.vault.sdk.api.http.HttpResponse;
import com.veeva.vault.sdk.api.http.HttpResponseBodyValueType;
import com.veeva.vault.sdk.api.http.HttpService;
import com.veeva.vault.sdk.api.json.JsonData;

import java.util.Optional;

/**
 * Just like a list in C
 *
 * @Author Radoslav Kotsev
 */
@UserDefinedClassInfo()
public class SequencedCommunication {

    private HttpRequest request;
    private Optional<SequencedCommunication> next;

    public SequencedCommunication(final HttpRequest request) {
        this.request = request;
        next = Optional.empty();
    }

    public SequencedCommunication setNext(final SequencedCommunication next) {
        this.next = Optional.ofNullable(next);
        return this;
    }

    public void execute() {
        final HttpService httpService = ServiceLocator.locate(HttpService.class);
        httpService.send(request, HttpResponseBodyValueType.JSONDATA)
                .onSuccess(httpResponse -> {
                    doOnSuccess(httpResponse);
                    if(next.isPresent()) {
                        next.get().execute();
                    }

                }).ignoreError().execute();
    }

    protected void doOnSuccess(final HttpResponse<JsonData> httpResponse) {
        //Implement me
    }

}
